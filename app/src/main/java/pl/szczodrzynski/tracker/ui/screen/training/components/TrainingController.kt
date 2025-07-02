package pl.szczodrzynski.tracker.ui.screen.training.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.service.data.TrackerCommand
import pl.szczodrzynski.tracker.service.data.TrackerConfig
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		Box(modifier = Modifier.fillMaxSize()) {
			TrainingController()
		}
	}
}

@Preview
@Composable
private fun PreviewDisconnected() {
	SportTrackPreview {
		Box(modifier = Modifier.fillMaxSize()) {
			TrainingController(isConnected = false)
		}
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun BoxScope.TrainingController(
	isConnected: Boolean = true,
	trackerConfig: TrackerConfig = TrackerConfig(),
	onConnectClick: () -> Unit = {},
	onConfigCommand: (command: TrackerCommand) -> Unit = {},
	onStartClick: () -> Unit = {},
) {
	Row(
		modifier = Modifier
			.align(Alignment.BottomCenter)
			.offset(y = -FloatingToolbarDefaults.ScreenOffset),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box {
			HorizontalFloatingToolbar(
				expanded = false,
				colors = if (isConnected)
					FloatingToolbarDefaults.vibrantFloatingToolbarColors()
				else
					FloatingToolbarDefaults.standardFloatingToolbarColors(),
				content = {
					if (isConnected)
						ConfigBarItems(trackerConfig, onConfigCommand)
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
		}

		Spacer(modifier = Modifier.width(8.dp))
		FloatingActionButton(
			onClick = {
				if (isConnected)
					onStartClick()
				else
					onConnectClick()
			},
			containerColor = if (isConnected)
				MaterialTheme.colorScheme.primary
			else
				MaterialTheme.colorScheme.secondaryContainer,
		) {
			Iconics(
				if (isConnected)
					CommunityMaterial.Icon3.cmd_play_outline
				else
					CommunityMaterial.Icon.cmd_connection
			)
		}
	}
}

@Composable
private fun ConfigBarItems(
	trackerConfig: TrackerConfig,
	onConfigCommand: (command: TrackerCommand) -> Unit,
) {
	val buttonModifier = Modifier.width(64.dp)
	var modeMenuOpened by remember { mutableStateOf(false) }
	var settingsMenuOpened by remember { mutableStateOf(false) }
	var distanceDialogShown by remember { mutableStateOf(false) }
	var delayReadyDialogShown by remember { mutableStateOf(false) }
	var delayStartMinDialogShown by remember { mutableStateOf(false) }
	var delayStartMaxDialogShown by remember { mutableStateOf(false) }

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
			)
		}
	}

	IconButton(
		onClick = {
			distanceDialogShown = true
		},
		modifier = buttonModifier,
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
				Text(stringResource(R.string.training_config_delay_ready, trackerConfig.delayReady))
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
				settingsMenuOpened = false
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
	}
}
