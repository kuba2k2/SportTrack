package pl.szczodrzynski.tracker.ui.screen.training

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.unit.sp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.data.entity.Athlete
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.TrainingRunSplit
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
		val trainingRun = TrainingRun(
			trainingId = 0,
			title = "",
			totalDistance = 0,
			sensorDistance = listOf(),
			isFlyingTest = false,
		)
		val splits = listOf(
			TrainingRunSplit(trainingRunId = 0, timestamp = 119, type = TrainingRunSplit.Type.REACTION_BTN),
			TrainingRunSplit(trainingRunId = 0, timestamp = 1234),
			TrainingRunSplit(trainingRunId = 0, timestamp = 2678),
			TrainingRunSplit(trainingRunId = 0, timestamp = 4096),
			TrainingRunSplit(trainingRunId = 0, timestamp = 5800),
		)
		TrainingRunDialog(
			trainingRun = trainingRun,
			splits = splits,
			athlete = null,
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
		val trainingRun = TrainingRun(
			trainingId = 0,
			title = "",
			totalDistance = 0,
			sensorDistance = listOf(),
			isFlyingTest = true,
			isFinished = true,
		)
		val splits = listOf(
			TrainingRunSplit(trainingRunId = 0, timestamp = 10000),
			TrainingRunSplit(trainingRunId = 0, timestamp = 11234),
			TrainingRunSplit(trainingRunId = 0, timestamp = 12678),
			TrainingRunSplit(trainingRunId = 0, timestamp = 14096),
			TrainingRunSplit(trainingRunId = 0, timestamp = 15800),
		)
		TrainingRunDialog(
			trainingRun = trainingRun,
			splits = splits,
			athlete = null,
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
	trainingRun: TrainingRun,
	splits: List<TrainingRunSplit>,
	athlete: Athlete?,
	lastResult: TrackerResult? = null,
	finishTimeout: Int = 0,
	isForcedShow: Boolean = false,
	onDismiss: () -> Unit = {},
	onDelete: (item: Any) -> Unit = {},
	onDescription: (value: String) -> Unit = {},
) {
	val hasActualSplit = splits.any { it.type == TrainingRunSplit.Type.SPLIT }

	val progressTimeout = when {
		finishTimeout != 0 && hasActualSplit -> finishTimeout
		lastResult?.type == TrackerResult.Type.ON_YOUR_MARKS -> lastResult.millis
		lastResult?.type == TrackerResult.Type.READY -> lastResult.millis
		else -> null
	}
	val progressValue = remember { Animatable(0.0f) }

	if (!trainingRun.isFinished && progressTimeout != null) {
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
	}

	var deleteDialogItem by remember { mutableStateOf<Any?>(null) }
	deleteDialogItem?.let {
		AlertDialog(
			onDismissRequest = {
				deleteDialogItem = null
			},
			confirmButton = {
				TextButton(
					onClick = {
						onDelete(it)
						deleteDialogItem = null
					},
				) {
					Text(stringResource(R.string.delete))
				}
			},
			dismissButton = {
				TextButton(
					onClick = {
						deleteDialogItem = null
					},
				) {
					Text(stringResource(R.string.cancel))
				}
			},
			title = {
				when (it) {
					is TrainingRun -> Text(stringResource(R.string.training_run_delete_run_title))
					is TrainingRunSplit -> Text(stringResource(R.string.training_run_delete_split_title))
				}
			},
		)
	}

	AlertDialog(
		onDismissRequest = {
			if (trainingRun.isFinished || isForcedShow)
				onDismiss()
		},
		confirmButton = {
			TextButton(
				onClick = onDismiss,
			) {
				if (trainingRun.isFinished || isForcedShow)
					Text(stringResource(R.string.training_run_close))
				else
					Text(stringResource(R.string.training_run_finish))
			}
		},
		icon = {
			Iconics(CommunityMaterial.Icon3.cmd_run_fast)
		},
		title = {
			when (trainingRun.isFlyingTest) {
				false -> Text(stringResource(R.string.training_config_start_on_signal))
				true -> Text(stringResource(R.string.training_config_flying_start))
			}
		},
		text = {
			LazyColumn {
				trainingRunDialogContent(
					trainingRun = trainingRun,
					splits = splits,
					lastResult = lastResult,
					progressTimeout = progressTimeout,
					progressValue = progressValue,
					onDescription = onDescription,
					onDelete = {
						deleteDialogItem = it
					},
				)
			}
		},
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.trainingRunDialogContent(
	trainingRun: TrainingRun,
	splits: List<TrainingRunSplit>,
	lastResult: TrackerResult?,
	progressTimeout: Int?,
	progressValue: Animatable<Float, AnimationVector1D>,
	onDescription: (value: String) -> Unit,
	onDelete: (item: Any) -> Unit,
) {
	val hasActualSplit = splits.any { it.type == TrainingRunSplit.Type.SPLIT }

	if (!hasActualSplit) {
		item(key = "state") {
			val textRes = when (lastResult?.type) {
				TrackerResult.Type.ON_YOUR_MARKS -> R.string.training_run_state_on_your_marks
				TrackerResult.Type.READY -> R.string.training_run_state_ready
				TrackerResult.Type.START -> if (!trainingRun.isFlyingTest) R.string.training_run_state_start else null
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

	if (trainingRun.isFinished) {
		item(key = "deleteHelp") {
			Text(
				stringResource(R.string.training_run_click_to_delete),
				modifier = Modifier
					.fillMaxWidth()
					.padding(bottom = 16.dp),
			)
		}
	}

	val firstTimestamp = splits.firstOrNull()?.timestamp ?: 0
	val indexOffset = splits.count { it.type != TrainingRunSplit.Type.SPLIT }
	itemsIndexed(splits, key = { _, split -> split.timestamp }) { index, split ->
		val timestamp = if (trainingRun.isFlyingTest)
			split.timestamp - firstTimestamp
		else
			split.timestamp

		Row(
			modifier = Modifier.clickable(
				enabled = trainingRun.isFinished && split.type == TrainingRunSplit.Type.SPLIT,
			) {
				if (splits.count() == 1)
					onDelete(trainingRun)
				else
					onDelete(split)
			},
			verticalAlignment = Alignment.CenterVertically,
		) {
			val timeModifier = Modifier
				.weight(1.0f)
				.padding(start = 8.dp)
				.padding(vertical = 4.dp)
			if (split.type == TrainingRunSplit.Type.SPLIT) {
				Text(
					text = "${index + 1 - indexOffset}",
					modifier = Modifier.width(36.dp),
					textAlign = TextAlign.Center,
					style = MaterialTheme.typography.headlineMedium,
				)
				Text(
					text = stringResource(R.string.seconds_format, timestamp.toFloat().roundTimeUp()),
					modifier = timeModifier,
					style = MaterialTheme.typography.headlineSmall,
				)
				if (trainingRun.isFlyingTest && index > 1 || !trainingRun.isFlyingTest && index > 0) {
					val timeSinceLast = split.timestamp - splits[index - 1].timestamp
					Text(
						text = stringResource(R.string.seconds_format_plus, timeSinceLast.toFloat().roundTime()),
						style = MaterialTheme.typography.titleLarge,
					)
				}
			} else {
				Iconics(
					icon = CommunityMaterial.Icon2.cmd_hand_wave_outline,
					size = 36.dp,
					modifier = Modifier.padding(8.dp),
				)
				Text(
					text = when (split.type) {
						TrainingRunSplit.Type.REACTION_BTN -> stringResource(R.string.training_run_reaction_button)
						TrainingRunSplit.Type.REACTION_OPT -> stringResource(R.string.training_run_reaction_optical)
						else -> ""
					},
					style = MaterialTheme.typography.bodySmall,
					textAlign = TextAlign.Center,
					lineHeight = 12.sp,
				)
				Text(
					text = stringResource(R.string.milliseconds_format, timestamp),
					modifier = timeModifier,
					style = MaterialTheme.typography.headlineSmall,
				)
			}
		}
	}

	if (!trainingRun.isFinished && progressTimeout != null) {
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

	if (trainingRun.isFinished) {
		item(key = "description") {
			var dialogShown by remember { mutableStateOf(false) }

			if (dialogShown) {
				EditTextDialog(
					initialText = trainingRun.description ?: "",
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
					trainingRun.description ?: stringResource(R.string.history_no_description),
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

		item(key = "delete") {
			Row(
				modifier = Modifier.fillParentMaxWidth(),
				horizontalArrangement = Arrangement.Center,
			) {
				TextButton(
					onClick = {
						onDelete(trainingRun)
					},
					shapes = ButtonDefaults.shapes(),
				) {
					Iconics(CommunityMaterial.Icon.cmd_delete_outline, size = ButtonDefaults.IconSize)
					Spacer(Modifier.size(ButtonDefaults.IconSpacing))
					Text(stringResource(R.string.training_run_delete_run_button))
				}
			}
		}
	}
}
