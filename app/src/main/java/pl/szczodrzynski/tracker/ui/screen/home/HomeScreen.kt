package pl.szczodrzynski.tracker.ui.screen.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.szczodrzynski.tracker.service.Utils
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

	val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
		if (result.isNotEmpty() && result.all { it.value }) {
			mainVm.binder?.updateState()
		}
	}

	Column(
		modifier = Modifier
			.padding(horizontal = 16.dp)
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		ConnectionState(
			serviceState, connectionState,
			onRequestPermission = {
				launcher.launch(Utils.getBluetoothPermissions().toTypedArray())
			},
			onEnableBluetooth = {},
			onChooseDevice = {},
			onConnect = {},
			onDisconnect = {},
		)
	}
}
