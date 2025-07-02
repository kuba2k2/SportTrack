package pl.szczodrzynski.tracker.ui.screen.home

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.service.Utils
import pl.szczodrzynski.tracker.service.data.ConnectionState
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.components.SensorErrorSnackbar
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		HomeScreen()
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun HomeScreen(
	vm: HomeViewModel = hiltViewModel(),
) {
	val mainVm = LocalMainViewModel.current
	val scrollState = rememberScrollState()
	val serviceState by mainVm.serviceState.collectAsStateWithLifecycle()
	val connectionState by mainVm.connectionState.collectAsStateWithLifecycle()

	val trackerConfig by vm.manager.trackerConfig.collectAsStateWithLifecycle()
	val training by vm.manager.training.collectAsStateWithLifecycle()

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
			.verticalScroll(scrollState)
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

		val version = trackerConfig.version
		val temperature = trackerConfig.temperature
		if (connectionState is ConnectionState.Connected)
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
		else
			return@Column
		if (version == null || temperature == null)
			return@Column

		val time = LocalTime.now()
		val defaultTitle = stringResource(
			R.string.training_default_title,
			time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
		)
		Text(
			training?.title
				?: stringResource(R.string.home_training_new_title),
			modifier = Modifier.padding(top = 48.dp, bottom = 8.dp),
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.headlineMedium,
		)

		val size = ButtonDefaults.LargeContainerHeight
		Button(
			onClick = {
				if (training == null)
					vm.createTraining(defaultTitle)
				mainVm.navigate(NavTarget.Training)
			},
			shapes = ButtonDefaults.shapesFor(size),
			modifier = Modifier
				.padding(top = 16.dp)
				.height(size),
			contentPadding = ButtonDefaults.contentPaddingFor(size),
		) {
			Iconics(CommunityMaterial.Icon3.cmd_run, size = ButtonDefaults.iconSizeFor(size))
			Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
			Text(
				stringResource(
					if (training == null)
						R.string.home_training_new_button
					else
						R.string.home_training_continue_button
				),
				style = ButtonDefaults.textStyleFor(size),
			)
		}

		if (training == null)
			return@Column
		TextButton(
			modifier = Modifier.padding(top = 8.dp),
			onClick = {
				vm.createTraining(defaultTitle)
				mainVm.navigate(NavTarget.Training)
			},
			shapes = ButtonDefaults.shapes(),
		) {
			Iconics(CommunityMaterial.Icon3.cmd_plus, size = ButtonDefaults.IconSize)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(stringResource(R.string.home_training_create_new_button))
		}
	}

	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
		SensorErrorSnackbar(
			trackerConfig = trackerConfig,
		)
	}
}
