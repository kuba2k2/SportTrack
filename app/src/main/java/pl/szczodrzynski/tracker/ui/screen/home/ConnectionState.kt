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
import pl.szczodrzynski.tracker.service.data.ConnectionState
import pl.szczodrzynski.tracker.service.data.ServiceState
import pl.szczodrzynski.tracker.service.data.TrackerDevice
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview

private class PreviewProvider :
	PreviewParameterProvider<Pair<ServiceState, ConnectionState>> {
	override val values by lazy {
		val device = TrackerDevice(
			"Device 1",
			"",
			TrackerDevice.State.BONDED
		)
		sequenceOf(
			ServiceState.Disconnected to ConnectionState.NoBluetoothSupport,
			ServiceState.Connected to ConnectionState.NoBluetoothSupport,
			ServiceState.Connected to ConnectionState.NoPermissions,
			ServiceState.Connected to ConnectionState.BluetoothNotEnabled,
			ServiceState.Connected to ConnectionState.Disconnected(null),
			ServiceState.Connected to ConnectionState.Disconnected(device),
			ServiceState.Connected to ConnectionState.Connecting(device),
			ServiceState.Connected to ConnectionState.Connected(device),
		)
	}
}

@Preview
@Composable
private fun PreviewServiceDisconnected(
	@PreviewParameter(PreviewProvider::class)
	states: Pair<ServiceState, ConnectionState>,
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
	serviceState: ServiceState,
	connectionState: ConnectionState,
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

	if (serviceState != ServiceState.Connected)
		return
	if (connectionState == ConnectionState.NoBluetoothSupport)
		return

	val deviceName = when (connectionState) {
		is ConnectionState.Disconnected -> connectionState.device?.name
		is ConnectionState.Connecting -> connectionState.device.name
		is ConnectionState.Connected -> connectionState.device.name
		else -> null
	}

	when (connectionState) {
		is ConnectionState.Disconnected,
		is ConnectionState.Connecting,
		is ConnectionState.Connected,
			-> {
			DeviceChooser(
				deviceName,
				enabled = connectionState is ConnectionState.Disconnected,
				onChooseDevice = onChooseDevice,
			)
		}

		else -> {}
	}

	val size = ButtonDefaults.MediumContainerHeight
	val enabled =
		connectionState !is ConnectionState.Connecting
			&& (connectionState !is ConnectionState.Disconnected || deviceName != null)

	Button(
		onClick = {
			when (connectionState) {
				ConnectionState.NoPermissions -> onRequestPermission()
				ConnectionState.BluetoothNotEnabled -> onEnableBluetooth()
				is ConnectionState.Disconnected -> onConnect()
				is ConnectionState.Connected -> onDisconnect()
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
			ConnectionState.NoPermissions -> {
				Text(stringResource(R.string.home_request_permission), style = style)
			}

			ConnectionState.BluetoothNotEnabled -> {
				Text(stringResource(R.string.home_enable_bluetooth), style = style)
			}

			is ConnectionState.Disconnected,
			is ConnectionState.Connecting,
				-> {
				Image(
					asset = CommunityMaterial.Icon.cmd_connection,
					modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
					colorFilter = ColorFilter.tint(LocalContentColor.current)
				)
				Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
				Text(stringResource(R.string.home_connect), style = style)
			}

			is ConnectionState.Connected -> {
				Image(
					asset = CommunityMaterial.Icon.cmd_close,
					modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
					colorFilter = ColorFilter.tint(LocalContentColor.current)
				)
				Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
				Text(stringResource(R.string.home_disconnect), style = style)
			}

			is ConnectionState.Error -> {}
			else -> {}
		}
	}
}

private fun getIcon(serviceState: ServiceState, connectionState: ConnectionState): IIcon =
	when (serviceState) {
		ServiceState.Disconnected -> CommunityMaterial.Icon2.cmd_lan_disconnect
		ServiceState.Connected -> when (connectionState) {
			ConnectionState.NoBluetoothSupport -> CommunityMaterial.Icon.cmd_alert_outline
			ConnectionState.NoPermissions -> CommunityMaterial.Icon3.cmd_security
			ConnectionState.BluetoothNotEnabled -> CommunityMaterial.Icon.cmd_bluetooth_off
			is ConnectionState.Disconnected -> CommunityMaterial.Icon.cmd_connection
			is ConnectionState.Connecting -> CommunityMaterial.Icon.cmd_bluetooth_audio
			is ConnectionState.Connected -> CommunityMaterial.Icon.cmd_bluetooth_connect
			is ConnectionState.Error -> CommunityMaterial.Icon.cmd_alert_outline
		}
	}

private fun getText(serviceState: ServiceState, connectionState: ConnectionState): Int =
	when (serviceState) {
		ServiceState.Disconnected -> R.string.service_disconnected
		ServiceState.Connected -> when (connectionState) {
			ConnectionState.NoBluetoothSupport -> R.string.connection_no_bluetooth_support
			ConnectionState.NoPermissions -> R.string.connection_no_permissions
			ConnectionState.BluetoothNotEnabled -> R.string.connection_bluetooth_not_enabled
			is ConnectionState.Disconnected -> R.string.connection_disconnected
			is ConnectionState.Connecting -> R.string.connection_connecting
			is ConnectionState.Connected -> R.string.connection_connected
			is ConnectionState.Error -> R.string.connection_error
		}
	}
