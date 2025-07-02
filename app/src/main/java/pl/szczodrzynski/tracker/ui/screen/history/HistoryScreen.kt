package pl.szczodrzynski.tracker.ui.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
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
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.data.entity.Training
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.components.FullscreenLoadingIndicator
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import pl.szczodrzynski.tracker.ui.screen.training.TrainingScreen

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		HistoryList(
			listOf(
				Training(title = "Training 4"),
				Training(title = "Training 3"),
				Training(title = "Training 2"),
				Training(title = "Training 1"),
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
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
				style = MaterialTheme.typography.headlineMedium,
			)
		}

		items(trainingList, key = { it.id }) { training ->
			Card(
				onClick = {
					onClick(training.id)
				},
				modifier = Modifier
					.fillParentMaxWidth()
					.padding(horizontal = 8.dp),
			) {
				Text(
					training.title,
					modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
					style = MaterialTheme.typography.titleLarge,
				)
			}
		}
	}
}
