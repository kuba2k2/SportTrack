package pl.szczodrzynski.tracker.ui.screen.training.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.service.data.TrackerCommand
import pl.szczodrzynski.tracker.service.data.TrackerConfig
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.components.SensorErrorSnackbar
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import java.util.Locale

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
			TrainingController()
		}
	}
}

@Preview
@Composable
private fun PreviewDisconnected() {
	SportTrackPreview {
		Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
			TrainingController(isConnected = false)
		}
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ColumnScope.TrainingController(
	isConnected: Boolean = true,
	isRunActive: Boolean = false,
	trackerConfig: TrackerConfig = TrackerConfig(),
	finishTimeoutFlow: MutableStateFlow<Int> = MutableStateFlow(0),
	onFabClick: () -> Unit = {},
	onCommand: (command: TrackerCommand) -> Unit = {},
) {
	SensorErrorSnackbar(
		trackerConfig = trackerConfig,
		modifier = Modifier.offset(y = -FloatingToolbarDefaults.ScreenOffset),
	)

	Row(
		modifier = Modifier
			.align(Alignment.CenterHorizontally)
			.offset(y = -FloatingToolbarDefaults.ScreenOffset),
		verticalAlignment = Alignment.CenterVertically,
	) {
		HorizontalFloatingToolbar(
			expanded = false,
			colors = if (isConnected)
				FloatingToolbarDefaults.vibrantFloatingToolbarColors()
			else
				FloatingToolbarDefaults.standardFloatingToolbarColors(),
			content = {
				if (isConnected)
					ConfigBarItems(trackerConfig, finishTimeoutFlow, onCommand)
				else
					Text(
						stringResource(R.string.training_not_connected),
						modifier = Modifier.padding(horizontal = 16.dp),
						color = MaterialTheme.colorScheme.error,
						fontWeight = FontWeight.Bold,
						style = MaterialTheme.typography.titleLarge,
					)
			},
		)

		Spacer(modifier = Modifier.width(8.dp))
		FloatingActionButton(
			onClick = onFabClick,
			containerColor = when {
				!isConnected -> MaterialTheme.colorScheme.secondaryContainer
				isRunActive -> MaterialTheme.colorScheme.secondaryContainer
				else -> MaterialTheme.colorScheme.primary
			},
		) {
			Iconics(
				when {
					!isConnected -> CommunityMaterial.Icon.cmd_connection
					isRunActive -> CommunityMaterial.Icon3.cmd_open_in_new
					else -> CommunityMaterial.Icon3.cmd_play_outline
				}
			)
		}
	}
}

@Composable
private fun ConfigBarItems(
	trackerConfig: TrackerConfig,
	finishTimeoutFlow: MutableStateFlow<Int>,
	onConfigCommand: (command: TrackerCommand) -> Unit,
) {
	val buttonModifier = Modifier.width(64.dp)
	var modeMenuOpened by remember { mutableStateOf(false) }
	var settingsMenuOpened by remember { mutableStateOf(false) }
	var distanceDialogShown by remember { mutableStateOf(false) }
	var delayReadyDialogShown by remember { mutableStateOf(false) }
	var delayStartMinDialogShown by remember { mutableStateOf(false) }
	var delayStartMaxDialogShown by remember { mutableStateOf(false) }
	val finishTimeout by finishTimeoutFlow.collectAsStateWithLifecycle()

	val modes = listOf(
		Triple(
			TrackerConfig.Mode.START_ON_SIGNAL,
			R.string.training_config_start_on_signal,
			CommunityMaterial.Icon.cmd_bullhorn_outline
		),
		Triple(
			TrackerConfig.Mode.FLYING_START,
			R.string.training_config_flying_start,
			CommunityMaterial.Icon.cmd_clock_end
		),
		Triple(
			TrackerConfig.Mode.REACTION_TEST,
			R.string.training_config_reaction_test,
			CommunityMaterial.Icon2.cmd_gesture_tap_button
		),
	)

	FilledIconButton(
		onClick = {
			modeMenuOpened = true
		},
		modifier = buttonModifier,
	) {
		Iconics(modes.first { it.first == trackerConfig.mode }.third)
	}

	DropdownMenu(
		expanded = modeMenuOpened,
		onDismissRequest = {
			modeMenuOpened = false
		},
	) {
		for ((mode, stringRes, icon) in modes) {
			DropdownMenuItem(
				text = { Text(stringResource(stringRes)) },
				onClick = {
					modeMenuOpened = false
					onConfigCommand(TrackerCommand.mode(mode))
				},
				leadingIcon = {
					Iconics(icon)
				},
				enabled = mode != TrackerConfig.Mode.REACTION_TEST,
			)
		}
	}

	IconButton(
		onClick = {
			distanceDialogShown = true
		},
		modifier = buttonModifier,
		enabled = false,
	) {
		Iconics(CommunityMaterial.Icon3.cmd_ruler)
	}

	IconButton(
		onClick = {
			settingsMenuOpened = true
		},
		modifier = buttonModifier,
	) {
		Iconics(CommunityMaterial.Icon.cmd_cog_outline)
	}

	DropdownMenu(
		expanded = settingsMenuOpened,
		onDismissRequest = {
			settingsMenuOpened = false
		},
	) {
		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.training_config_delay_ready, trackerConfig.delayReady / 1000))
			},
			onClick = {
				settingsMenuOpened = false
				delayReadyDialogShown = true
			},
			leadingIcon = {
				Iconics(CommunityMaterial.Icon3.cmd_timer_refresh_outline)
			},
		)
		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.training_config_delay_start_min, trackerConfig.delayStartMin / 1000.0f))
			},
			onClick = {
				settingsMenuOpened = false
				delayStartMinDialogShown = true
			},
			leadingIcon = {
				Iconics(CommunityMaterial.Icon3.cmd_sort_clock_descending_outline)
			},
		)
		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.training_config_delay_start_max, trackerConfig.delayStartMax / 1000.0f))
			},
			onClick = {
				settingsMenuOpened = false
				delayStartMaxDialogShown = true
			},
			leadingIcon = {
				Iconics(CommunityMaterial.Icon3.cmd_sort_clock_ascending_outline)
			},
		)
		DropdownMenuItem(
			text = {
				when (trackerConfig.sensorBeep) {
					true -> Text(stringResource(R.string.training_config_sensor_beep_on))
					false -> Text(stringResource(R.string.training_config_sensor_beep_off))
				}
			},
			onClick = {
				onConfigCommand(TrackerCommand.sensorBeep(!trackerConfig.sensorBeep))
			},
			leadingIcon = {
				Iconics(
					when (trackerConfig.sensorBeep) {
						true -> CommunityMaterial.Icon3.cmd_volume_high
						false -> CommunityMaterial.Icon3.cmd_volume_off
					}
				)
			},
		)
		DropdownMenuItem(
			text = {
				when (finishTimeout) {
					0 -> Text(stringResource(R.string.training_config_auto_finish_off))
					else -> Text(stringResource(R.string.training_config_auto_finish, finishTimeout / 1000))
				}
			},
			onClick = {
				finishTimeoutFlow.update {
					when (finishTimeout) {
						0 -> 20000
						else -> 0
					}
				}
			},
			leadingIcon = {
				Iconics(
					when (finishTimeout) {
						0 -> CommunityMaterial.Icon2.cmd_flag_off_outline
						else -> CommunityMaterial.Icon2.cmd_flag_checkered
					}
				)
			},
		)
	}

	if (delayReadyDialogShown || delayStartMinDialogShown || delayStartMaxDialogShown) {
		val command = when {
			delayReadyDialogShown -> TrackerCommand.Type.DELAY_READY
			delayStartMinDialogShown -> TrackerCommand.Type.DELAY_START_MIN
			else -> TrackerCommand.Type.DELAY_START_MAX
		}
		val delayMs = when {
			delayReadyDialogShown -> trackerConfig.delayReady
			delayStartMinDialogShown -> trackerConfig.delayStartMin
			else -> trackerConfig.delayStartMax
		}
		DelayConfigDialog(
			command = command,
			delayMs = delayMs,
			onDismiss = {
				delayReadyDialogShown = false
				delayStartMinDialogShown = false
				delayStartMaxDialogShown = false
			},
			onUpdate = { newDelayMs ->
				onConfigCommand(TrackerCommand(command, newDelayMs.toString()))
			},
		)
	}
}

@Composable
private fun DelayConfigDialog(
	command: TrackerCommand.Type,
	delayMs: Int,
	onDismiss: () -> Unit,
	onUpdate: (newDelayMs: Int) -> Unit,
) {
	val initialDelay = delayMs / 1000.0f
	val initialValue = String.format(Locale.getDefault(), "%.1f", initialDelay)
	val regex = Regex("""^\d{0,4}([.,]\d?)?$""")

	val state = rememberTextFieldState(initialValue)

	AlertDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			TextButton(
				onClick = {
					val newDelay = state.text.toString()
						.replace(',', '.')
						.toFloatOrNull() ?: initialDelay
					onUpdate((newDelay * 1000.0).toInt())
					onDismiss()
				}
			) {
				Text(stringResource(R.string.training_config_save))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.training_config_cancel))
			}
		},
		title = {
			Text(stringResource(R.string.training_config_delay_dialog_title))
		},
		text = {
			when (command) {
				TrackerCommand.Type.DELAY_READY -> Text(stringResource(R.string.training_config_delay_ready_text))
				TrackerCommand.Type.DELAY_START_MIN -> Text(stringResource(R.string.training_config_delay_start_min_text))
				TrackerCommand.Type.DELAY_START_MAX -> Text(stringResource(R.string.training_config_delay_start_max_text))
				else -> return@AlertDialog
			}

			TextField(
				state = state,
				inputTransformation = {
					if (!this.toString().matches(regex))
						revertAllChanges()
				},
				keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
			)
		},
	)
}
