package pl.szczodrzynski.tracker.ui.screen.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import kotlinx.coroutines.flow.update
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.data.entity.TrainingComment
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.TrainingRunSplit
import pl.szczodrzynski.tracker.data.entity.joins.TrainingRunFull
import pl.szczodrzynski.tracker.manager.TrackerManager
import pl.szczodrzynski.tracker.service.data.ConnectionState
import pl.szczodrzynski.tracker.service.data.TrackerCommand
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.components.EditTextDialog
import pl.szczodrzynski.tracker.ui.components.FullscreenLoadingIndicator
import pl.szczodrzynski.tracker.ui.components.IconTextRow
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import pl.szczodrzynski.tracker.ui.screen.training.components.TrainingController
import pl.szczodrzynski.tracker.ui.screen.training.components.TrainingMap
import pl.szczodrzynski.tracker.ui.screen.training.metadata.TrainingMetadataUpdater
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		TrainingScreen()
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun TrainingScreen(
	trainingId: Int? = null,
	forceNew: Boolean = false,
	vm: TrainingViewModel = hiltViewModel(),
) {
	val mainVm = LocalMainViewModel.current
	val state by vm.state.collectAsStateWithLifecycle()
	val runState by vm.manager.runState.collectAsStateWithLifecycle()
	val connectionState by mainVm.connectionState.collectAsStateWithLifecycle()
	val trackerConfig by vm.manager.trackerConfig.collectAsStateWithLifecycle()
	val weatherLoading by vm.weatherLoading.collectAsStateWithLifecycle()

	val time = LocalTime.now()
	val defaultTitle = stringResource(
		R.string.training_default_title,
		time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
	)
	LaunchedEffect(trainingId, forceNew) {
		vm.loadTraining(
			defaultTitle = defaultTitle,
			trainingId = trainingId,
			forceNew = forceNew,
		)
	}

	val trainingFull = when (val localState = state) {
		is TrainingViewModel.State.Loading -> {
			FullscreenLoadingIndicator()
			return
		}

		is TrainingViewModel.State.InProgress -> localState.training
		is TrainingViewModel.State.Finished -> localState.training
	}
	val training = trainingFull.training
	val timeline = trainingFull.getTimeline().sortedByDescending { it.first }

	var locationInProgress by remember { mutableStateOf(false) }
	var locationRetryCount by remember { mutableIntStateOf(0) }

	var titleDialogShown by remember { mutableStateOf(false) }
	var trainingRunDialogItem by remember { mutableStateOf<TrainingRunFull?>(null) }
	var trainingCommentDialogItem by remember { mutableStateOf<TrainingComment?>(null) }

	if (state is TrainingViewModel.State.InProgress) {
		TrainingMetadataUpdater(
			vm = vm,
			retryCount = locationRetryCount,
			onLocationProgress = {
				locationInProgress = it
			},
		)
	}

	if (titleDialogShown) {
		EditTextDialog(
			initialText = training.title,
			onDismiss = { value ->
				titleDialogShown = false
				value ?: return@EditTextDialog
				vm.saveTitle(value)
			},
			title = stringResource(R.string.training_edit_title_title),
		)
	}

	trainingRunDialogItem?.let { dialogItem ->
		TrainingRunDialog(
			trainingRun = dialogItem.run,
			splits = dialogItem.splits,
			athlete = dialogItem.athlete,
			onDismiss = {
				trainingRunDialogItem = null
			},
			onDelete = { item ->
				when (item) {
					is TrainingRun -> {
						vm.deleteRun(item)
						trainingRunDialogItem = null
					}

					is TrainingRunSplit -> {
						val newRun = dialogItem.copy(splits = dialogItem.splits.filterNot { it == item })
						vm.deleteSplit(item)
						trainingRunDialogItem = newRun
					}
				}
			},
			onDescription = { value ->
				val newRun = dialogItem.copy(run = dialogItem.run.copy(description = value))
				vm.saveRun(newRun.run)
				trainingRunDialogItem = newRun
			},
		)
	}

	trainingCommentDialogItem?.let {
		EditTextDialog(
			initialText = it.comment,
			onDismiss = { value ->
				trainingCommentDialogItem = null
				value ?: return@EditTextDialog
				vm.saveComment(it, value)
			},
			title = stringResource(R.string.training_edit_comment_title),
		)
	}

	LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
		item(key = "title") {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Column(
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.padding(top = 16.dp)
						.weight(1.0f),
				) {
					Text(
						training.title,
						style = MaterialTheme.typography.headlineMedium,
					)
					IconTextRow(
						icon = CommunityMaterial.Icon.cmd_calendar_outline,
						text = training.dateTime.atZone(ZoneId.systemDefault())
							.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
					)
					IconTextRow(
						icon = CommunityMaterial.Icon3.cmd_map_marker_outline,
						text = training.locationName ?: stringResource(R.string.history_no_location),
					)
				}

				IconButton(
					onClick = {
						titleDialogShown = true
					},
					modifier = Modifier.padding(end = 16.dp),
				) {
					Iconics(CommunityMaterial.Icon3.cmd_pencil_outline)
				}
			}
		}

		item(key = "map") {
			TrainingMap(
				training = training,
				isLoading = locationInProgress,
				onRetry = {
					locationRetryCount++
				},
			)
		}

		item(key = "results") {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(
					stringResource(R.string.training_timeline),
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.padding(top = 16.dp)
						.weight(1.0f),
					style = MaterialTheme.typography.headlineSmall,
				)
				if (state is TrainingViewModel.State.InProgress) {
					IconButton(
						onClick = {
							vm.fetchWeather()
						},
						enabled = !weatherLoading && training.locationLat != null,
						modifier = Modifier.padding(end = 16.dp),
					) {
						Iconics(CommunityMaterial.Icon3.cmd_weather_partly_cloudy)
					}
					IconButton(
						onClick = {
							trainingCommentDialogItem = TrainingComment(trainingId = training.id, comment = "")
						},
						modifier = Modifier.padding(end = 16.dp),
					) {
						Iconics(CommunityMaterial.Icon.cmd_comment_text_outline)
					}
				}
			}
		}

		items(timeline, key = { it.first }) { (_, item) ->
			TrainingTimelineCard(
				item = item,
				onClick = {
					when (item) {
						is TrainingRunFull -> trainingRunDialogItem = item
						is TrainingComment -> trainingCommentDialogItem = item
					}
				},
			)
		}

		item(key = "spacer") {
			Spacer(modifier = Modifier.height(FloatingToolbarDefaults.ContainerSize + FloatingToolbarDefaults.ScreenOffset + 8.dp))
		}
	}

	if (state is TrainingViewModel.State.InProgress) {
		Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
			val isConnected = connectionState is ConnectionState.Connected
			val isRunActive = runState is TrackerManager.State.InProgress
			TrainingController(
				isConnected = isConnected,
				isRunActive = isRunActive,
				trackerConfig = trackerConfig,
				finishTimeoutFlow = vm.manager.finishTimeout,
				onFabClick = {
					when {
						!isConnected -> mainVm.navigate(NavTarget.Home)
						isRunActive -> mainVm.forceRunDialog.update { true }
						else -> vm.sendCommand(TrackerCommand.start())
					}
				},
				onCommand = vm::sendCommand,
			)
		}
	}
}
