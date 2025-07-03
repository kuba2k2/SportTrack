package pl.szczodrzynski.tracker.ui.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.data.entity.Training
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.components.FullscreenLoadingIndicator
import pl.szczodrzynski.tracker.ui.components.IconTextRow
import pl.szczodrzynski.tracker.ui.components.TitleIconTextRow
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import pl.szczodrzynski.tracker.ui.screen.training.TrainingScreen
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		HistoryList(
			listOf(
				Training(id = 4, title = "Training 4"),
				Training(id = 3, title = "Training 3"),
				Training(id = 2, title = "Training 2"),
				Training(id = 1, title = "Training 1"),
			)
		)
	}
}

@Composable
fun HistoryScreen(
	trainingId: Int?,
	vm: HistoryViewModel = hiltViewModel(),
) {
	val mainVm = LocalMainViewModel.current
	val state by vm.state.collectAsStateWithLifecycle()

	LaunchedEffect(trainingId) {
		if (trainingId == null)
			vm.loadList()
		else
			vm.loadTraining(trainingId)
	}

	when (val stateLocal = state) {
		is HistoryViewModel.State.Loading -> {
			FullscreenLoadingIndicator()
			return
		}

		is HistoryViewModel.State.InList -> {
			HistoryList(stateLocal.trainingList) {
				mainVm.navigate(NavTarget.History(it))
			}
		}

		is HistoryViewModel.State.InTraining -> {
			TrainingScreen(trainingId = stateLocal.trainingId)
		}
	}
}

@Composable
private fun HistoryList(
	trainingList: List<Training>,
	onClick: (trainingId: Int) -> Unit = {},
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		item(key = "title") {
			Text(
				stringResource(R.string.history_title_long),
				modifier = Modifier
					.padding(horizontal = 16.dp)
					.padding(top = 16.dp, bottom = 8.dp),
				style = MaterialTheme.typography.headlineMedium,
			)
		}

		items(trainingList, key = { it.id }) { training ->
			val localDateTime = training.dateTime.atZone(ZoneId.systemDefault())
			Card(
				onClick = {
					onClick(training.id)
				},
				modifier = Modifier
					.fillParentMaxWidth()
					.padding(horizontal = 16.dp),
			) {
				Column(
					modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
				) {
					TitleIconTextRow(
						text = training.title,
						extraText = localDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
					)
					IconTextRow(
						icon = CommunityMaterial.Icon3.cmd_map_marker_outline,
						text = training.locationName ?: stringResource(R.string.history_no_location),
						extraText = localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
					)
					IconTextRow(
						icon = CommunityMaterial.Icon3.cmd_text,
						text = training.description ?: stringResource(R.string.history_no_description),
						maxLines = 2,
					)
				}
			}
		}

		item("spacer") {
			Spacer(modifier = Modifier.height(16.dp))
		}
	}
}
