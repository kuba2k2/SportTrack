package pl.szczodrzynski.tracker.ui.screen.home

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.service.Utils
import pl.szczodrzynski.tracker.service.data.ConnectionState
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		HomeScreen()
	}
}

@Composable
fun HomeScreen(
	vm: HomeViewModel = hiltViewModel(),
) {
	val mainVm = LocalMainViewModel.current
	val serviceState by mainVm.serviceState.collectAsStateWithLifecycle()
	val connectionState by mainVm.connectionState.collectAsStateWithLifecycle()
	val trackerConfig by mainVm.trackerConfig.collectAsStateWithLifecycle()

	val permissionLauncher = rememberLauncherForActivityResult(RequestMultiplePermissions()) {
		mainVm.binder?.updateState()
	}
	val bluetoothLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {}

	var deviceDialogOpen by remember { mutableStateOf(false) }

	if (deviceDialogOpen && connectionState is ConnectionState.Disconnected) {
		DeviceDialog(
			onChooseDevice = {
				mainVm.binder?.setTrackerDevice(it)
				deviceDialogOpen = false
			},
			onDismiss = {
				deviceDialogOpen = false
			},
		)
	}

	Column(
		modifier = Modifier
			.padding(horizontal = 16.dp)
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		ConnectionState(
			serviceState = serviceState,
			connectionState = connectionState,
			onRequestPermission = {
				permissionLauncher.launch(Utils.getBluetoothPermissions().toTypedArray())
			},
			onEnableBluetooth = {
				bluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
			},
			onChooseDevice = {
				deviceDialogOpen = true
			},
			onConnect = {
				mainVm.binder?.connectDevice()
			},
			onDisconnect = {
				mainVm.binder?.disconnectDevice()
			},
		)

		if (connectionState is ConnectionState.Connected) {
			val version = trackerConfig.version
			val temperature = trackerConfig.temperature
			Text(
				if (version != null && temperature != null)
					stringResource(R.string.home_device_firmware, version, temperature)
				else
					stringResource(R.string.home_device_checking),
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				textAlign = TextAlign.Center,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
	}
}
