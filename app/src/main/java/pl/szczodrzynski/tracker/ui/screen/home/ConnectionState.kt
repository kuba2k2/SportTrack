package pl.szczodrzynski.tracker.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.service.TrackerService
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview

private class PreviewProvider :
	PreviewParameterProvider<Pair<TrackerService.ServiceState, TrackerService.ConnectionState>> {
	override val values = sequenceOf(
		TrackerService.ServiceState.Disconnected to TrackerService.ConnectionState.NoBluetoothSupport,
		TrackerService.ServiceState.Connected to TrackerService.ConnectionState.NoBluetoothSupport,
		TrackerService.ServiceState.Connected to TrackerService.ConnectionState.NoPermissions,
		TrackerService.ServiceState.Connected to TrackerService.ConnectionState.BluetoothNotEnabled,
		TrackerService.ServiceState.Connected to TrackerService.ConnectionState.Disconnected(null),
		TrackerService.ServiceState.Connected to TrackerService.ConnectionState.Disconnected("SerialPort"),
		TrackerService.ServiceState.Connected to TrackerService.ConnectionState.Connecting("SerialPort"),
		TrackerService.ServiceState.Connected to TrackerService.ConnectionState.Connected("SerialPort"),
	)
}

@Preview
@Composable
private fun PreviewServiceDisconnected(
	@PreviewParameter(PreviewProvider::class)
	states: Pair<TrackerService.ServiceState, TrackerService.ConnectionState>,
) {
	SportTrackPreview {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
		) {
			ConnectionState(states.first, states.second)
		}
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ConnectionState(
	serviceState: TrackerService.ServiceState,
	connectionState: TrackerService.ConnectionState,
	onRequestPermission: () -> Unit = {},
	onEnableBluetooth: () -> Unit = {},
	onChooseDevice: () -> Unit = {},
	onConnect: () -> Unit = {},
	onDisconnect: () -> Unit = {},
) {
	Image(
		asset = getIcon(serviceState, connectionState),
		modifier = Modifier.size(96.dp),
		colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
	)
	Text(
		text = stringResource(getText(serviceState, connectionState)),
		modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
		textAlign = TextAlign.Center,
		style = MaterialTheme.typography.headlineSmall,
	)

	if (serviceState != TrackerService.ServiceState.Connected)
		return
	if (connectionState == TrackerService.ConnectionState.NoBluetoothSupport)
		return

	val deviceName = when (connectionState) {
		is TrackerService.ConnectionState.Disconnected -> connectionState.deviceName
		is TrackerService.ConnectionState.Connecting -> connectionState.deviceName
		is TrackerService.ConnectionState.Connected -> connectionState.deviceName
		else -> null
	}

	when (connectionState) {
		is TrackerService.ConnectionState.Disconnected,
		is TrackerService.ConnectionState.Connecting,
		is TrackerService.ConnectionState.Connected,
			-> {
			DeviceChooser(
				deviceName,
				enabled = connectionState is TrackerService.ConnectionState.Disconnected,
				onChooseDevice = onChooseDevice,
			)
		}

		else -> {}
	}

	val size = ButtonDefaults.MediumContainerHeight
	val enabled =
		connectionState !is TrackerService.ConnectionState.Connecting
			&& (connectionState !is TrackerService.ConnectionState.Disconnected || deviceName != null)

	Button(
		onClick = {
			when (connectionState) {
				TrackerService.ConnectionState.NoPermissions -> onRequestPermission()
				TrackerService.ConnectionState.BluetoothNotEnabled -> onEnableBluetooth()
				is TrackerService.ConnectionState.Disconnected -> onConnect()
				is TrackerService.ConnectionState.Connected -> onDisconnect()
				else -> {}
			}
		},
		shapes = ButtonDefaults.shapesFor(size),
		enabled = enabled,
		modifier = Modifier
			.padding(top = 16.dp)
			.height(size),
		contentPadding = ButtonDefaults.contentPaddingFor(size),
	) {
		val style = ButtonDefaults.textStyleFor(size)
		when (connectionState) {
			TrackerService.ConnectionState.NoPermissions -> {
				Text(stringResource(R.string.home_request_permission), style = style)
			}

			TrackerService.ConnectionState.BluetoothNotEnabled -> {
				Text(stringResource(R.string.home_enable_bluetooth), style = style)
			}

			is TrackerService.ConnectionState.Disconnected,
			is TrackerService.ConnectionState.Connecting,
				-> {
				Image(
					asset = CommunityMaterial.Icon.cmd_connection,
					modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
					colorFilter = ColorFilter.tint(LocalContentColor.current)
				)
				Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
				Text(stringResource(R.string.home_connect), style = style)
			}

			is TrackerService.ConnectionState.Connected -> {
				Image(
					asset = CommunityMaterial.Icon.cmd_close,
					modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
					colorFilter = ColorFilter.tint(LocalContentColor.current)
				)
				Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
				Text(stringResource(R.string.home_disconnect), style = style)
			}

			is TrackerService.ConnectionState.Error -> {}
			else -> {}
		}
	}
}

private fun getIcon(serviceState: TrackerService.ServiceState, connectionState: TrackerService.ConnectionState): IIcon =
	when (serviceState) {
		TrackerService.ServiceState.Disconnected -> CommunityMaterial.Icon2.cmd_lan_disconnect
		TrackerService.ServiceState.Connected -> when (connectionState) {
			TrackerService.ConnectionState.NoBluetoothSupport -> CommunityMaterial.Icon.cmd_alert_outline
			TrackerService.ConnectionState.NoPermissions -> CommunityMaterial.Icon3.cmd_security
			TrackerService.ConnectionState.BluetoothNotEnabled -> CommunityMaterial.Icon.cmd_bluetooth_off
			is TrackerService.ConnectionState.Disconnected -> CommunityMaterial.Icon.cmd_connection
			is TrackerService.ConnectionState.Connecting -> CommunityMaterial.Icon.cmd_bluetooth_audio
			is TrackerService.ConnectionState.Connected -> CommunityMaterial.Icon.cmd_bluetooth_connect
			is TrackerService.ConnectionState.Error -> CommunityMaterial.Icon.cmd_alert_outline
		}
	}

private fun getText(serviceState: TrackerService.ServiceState, connectionState: TrackerService.ConnectionState): Int =
	when (serviceState) {
		TrackerService.ServiceState.Disconnected -> R.string.service_disconnected
		TrackerService.ServiceState.Connected -> when (connectionState) {
			TrackerService.ConnectionState.NoBluetoothSupport -> R.string.connection_no_bluetooth_support
			TrackerService.ConnectionState.NoPermissions -> R.string.connection_no_permissions
			TrackerService.ConnectionState.BluetoothNotEnabled -> R.string.connection_bluetooth_not_enabled
			is TrackerService.ConnectionState.Disconnected -> R.string.connection_disconnected
			is TrackerService.ConnectionState.Connecting -> R.string.connection_connecting
			is TrackerService.ConnectionState.Connected -> R.string.connection_connected
			is TrackerService.ConnectionState.Error -> R.string.connection_error
		}
	}
