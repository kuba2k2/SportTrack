package pl.szczodrzynski.tracker.ui.screen.training

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.TrainingRunSplit
import pl.szczodrzynski.tracker.data.entity.joins.TrainingRunFull
import pl.szczodrzynski.tracker.service.data.TrackerConfig
import pl.szczodrzynski.tracker.service.data.TrackerResult
import pl.szczodrzynski.tracker.ui.components.EditTextDialog
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.components.roundTime
import pl.szczodrzynski.tracker.ui.components.roundTimeUp
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview

@Preview
@Composable
private fun PreviewStartOnSignal() {
	SportTrackPreview {
		val trainingRun = TrainingRunFull(
			run = TrainingRun(
				trainingId = 0,
				title = "",
				totalDistance = 0,
				sensorDistance = listOf(),
				isFlyingTest = false,
			),
			splits = listOf(
				TrainingRunSplit(trainingRunId = 0, timestamp = 1234),
				TrainingRunSplit(trainingRunId = 0, timestamp = 2678),
				TrainingRunSplit(trainingRunId = 0, timestamp = 4096),
				TrainingRunSplit(trainingRunId = 0, timestamp = 5800),
			),
			athlete = null,
		)
		TrainingRunDialog(
			trainingRun = trainingRun,
			lastResult = TrackerResult(
				mode = TrackerConfig.Mode.START_ON_SIGNAL,
				type = TrackerResult.Type.ON_YOUR_MARKS,
				millis = 7000,
			),
			finishTimeout = 20000,
		)
	}
}

@Preview
@Composable
private fun PreviewFlyingStart() {
	SportTrackPreview {
		val trainingRun = TrainingRunFull(
			run = TrainingRun(
				trainingId = 0,
				title = "",
				totalDistance = 0,
				sensorDistance = listOf(),
				isFlyingTest = true,
			),
			splits = listOf(
				TrainingRunSplit(trainingRunId = 0, timestamp = 10000),
				TrainingRunSplit(trainingRunId = 0, timestamp = 11234),
				TrainingRunSplit(trainingRunId = 0, timestamp = 12678),
				TrainingRunSplit(trainingRunId = 0, timestamp = 14096),
				TrainingRunSplit(trainingRunId = 0, timestamp = 15800),
			),
			athlete = null,
		)
		TrainingRunDialog(
			trainingRun = trainingRun,
			lastResult = TrackerResult(
				mode = TrackerConfig.Mode.START_ON_SIGNAL,
				type = TrackerResult.Type.ON_YOUR_MARKS,
				millis = 7000,
			),
			finishTimeout = 20000,
		)
	}
}

@Composable
fun TrainingRunDialog(
	trainingRun: TrainingRunFull,
	lastResult: TrackerResult?,
	finishTimeout: Int,
	onDismiss: () -> Unit = {},
	onDescription: (value: String) -> Unit = {},
) {
	val progressTimeout =
		finishTimeout.takeIf { it != 0 && trainingRun.splits.isNotEmpty() }
			?: lastResult?.millis
	val progressValue = remember { Animatable(0.0f) }

	if (!trainingRun.run.isFinished && progressTimeout != null)
		LaunchedEffect(lastResult, trainingRun) {
			progressValue.snapTo(0.0f)
			progressValue.animateTo(
				targetValue = 1.0f,
				animationSpec = tween(
					durationMillis = progressTimeout,
					easing = LinearEasing,
				),
			)
		}

	AlertDialog(
		onDismissRequest = {
			if (trainingRun.run.isFinished)
				onDismiss()
		},
		confirmButton = {
			TextButton(
				onClick = onDismiss,
			) {
				if (trainingRun.run.isFinished)
					Text(stringResource(R.string.training_run_close))
				else
					Text(stringResource(R.string.training_run_finish))
			}
		},
		icon = {
			Iconics(CommunityMaterial.Icon3.cmd_run_fast)
		},
		title = {
			when (trainingRun.run.isFlyingTest) {
				false -> Text(stringResource(R.string.training_config_start_on_signal))
				true -> Text(stringResource(R.string.training_config_flying_start))
			}
		},
		text = {
			LazyColumn {
				trainingRunDialogContent(
					trainingRun = trainingRun,
					lastResult = lastResult,
					progressTimeout = progressTimeout,
					progressValue = progressValue,
					onDescription = onDescription,
				)
			}
		},
	)
}

private fun LazyListScope.trainingRunDialogContent(
	trainingRun: TrainingRunFull,
	lastResult: TrackerResult?,
	progressTimeout: Int?,
	progressValue: Animatable<Float, AnimationVector1D>,
	onDescription: (value: String) -> Unit,
) {
	if (trainingRun.splits.isEmpty()) {
		item(key = "state") {
			val textRes = when (lastResult?.type) {
				TrackerResult.Type.ON_YOUR_MARKS -> R.string.training_run_state_on_your_marks
				TrackerResult.Type.READY -> R.string.training_run_state_ready
				TrackerResult.Type.START -> R.string.training_run_state_start
				else -> null
			}
			if (textRes == null)
				Text(
					stringResource(R.string.training_run_waiting_for_results),
					modifier = Modifier.fillMaxWidth(),
					textAlign = TextAlign.Center,
				)
			else
				Text(
					stringResource(textRes),
					modifier = Modifier.fillMaxWidth(),
					textAlign = TextAlign.Center,
					style = MaterialTheme.typography.headlineMedium,
				)
		}
	}

	val firstTimestamp = trainingRun.splits.firstOrNull()?.timestamp ?: 0
	val timestamps = trainingRun.splits.map {
		if (trainingRun.run.isFlyingTest)
			it.timestamp - firstTimestamp
		else
			it.timestamp
	}
	itemsIndexed(timestamps, key = { _, timestamp -> timestamp }) { index, timestamp ->
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text(
				text = "${index + 1}",
				modifier = Modifier
					.padding(vertical = 4.dp)
					.padding(end = 24.dp),
				textAlign = TextAlign.Center,
				style = MaterialTheme.typography.headlineMedium,
			)
			Text(
				text = stringResource(
					R.string.seconds_format,
					timestamp.toFloat().roundTimeUp(),
				),
				modifier = Modifier.weight(1.0f),
				style = MaterialTheme.typography.headlineSmall,
			)
			if (trainingRun.run.isFlyingTest && index > 1 || !trainingRun.run.isFlyingTest && index > 0)
				Text(
					text = stringResource(
						R.string.seconds_format_plus,
						(timestamp - timestamps[index - 1]).toFloat().roundTime(),
					),
					style = MaterialTheme.typography.titleLarge,
				)
		}
	}

	if (!trainingRun.run.isFinished && progressTimeout != null) {
		item(key = "progress") {
			LinearProgressIndicator(
				progress = { progressValue.value },
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
			)
			Text(
				text = stringResource(
					R.string.seconds_format_single,
					(progressTimeout * (1.0f - progressValue.value)).roundTimeUp(),
				),
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 4.dp),
				textAlign = TextAlign.Center,
				style = MaterialTheme.typography.titleMedium,
			)
		}
	}

	if (trainingRun.run.isFinished) {
		item(key = "description") {
			var dialogShown by remember { mutableStateOf(false) }

			if (dialogShown) {
				EditTextDialog(
					initialText = trainingRun.run.description ?: "",
					onDismiss = { value ->
						dialogShown = false
						value ?: return@EditTextDialog
						onDescription(value)
					},
					title = stringResource(R.string.training_edit_comment_title),
				)
			}

			Row(verticalAlignment = Alignment.CenterVertically) {
				Iconics(CommunityMaterial.Icon3.cmd_text, size = 16.dp)
				Text(
					trainingRun.run.description ?: stringResource(R.string.history_no_description),
					modifier = Modifier
						.padding(start = 4.dp)
						.weight(1.0f),
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				IconButton(
					onClick = {
						dialogShown = true
					},
				) {
					Iconics(CommunityMaterial.Icon.cmd_comment_text_outline)
				}
			}
		}
	}
}
