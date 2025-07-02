package pl.szczodrzynski.tracker.ui.screen.training

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import timber.log.Timber

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		TrainingScreen()
	}
}

@Composable
fun TrainingScreen(
	vm: TrainingViewModel = hiltViewModel(),
) {
	val state by vm.state.collectAsStateWithLifecycle()
	val context = LocalContext.current

	val training = when (val localState = state) {
		is TrainingViewModel.State.Loading -> {
			TrainingLoading(vm)
			return
		}

		is TrainingViewModel.State.InProgress -> localState.training
		is TrainingViewModel.State.Finished -> localState.training
	}

	var locationInProgress by remember { mutableStateOf(false) }
	var locationPermissions by remember { mutableStateOf(mapOf<String, Boolean>()) }
	var locationWasEnabled by remember { mutableIntStateOf(0) }

	val permissionLauncher = rememberLauncherForActivityResult(RequestMultiplePermissions()) {
		locationInProgress = false
		locationPermissions = it
	}
	val locationLauncher = rememberLauncherForActivityResult(StartIntentSenderForResult()) {
		locationInProgress = false
		if (it.resultCode == RESULT_OK)
			locationWasEnabled++
	}

	// retry locating if the permissions change or device location is enabled
	LaunchedEffect(locationPermissions, locationWasEnabled) {
		vm.updateTrainingMetadata(
			context = context,
			onPermissionRequired = { permissions ->
				Timber.d("Requesting location permissions")
				permissionLauncher.launch(permissions.toTypedArray())
			},
			onLocationDisabled = { e ->
				Timber.d("Requesting location enabling")
				e.status.startResolutionForResult(locationLauncher)
			},
			onProgress = { inProgress ->
				locationInProgress = inProgress
			},
		)
	}

	Column(modifier = Modifier.fillMaxSize()) {
		if (locationInProgress) {
			CircularProgressIndicator()
		}

		Text("Training: $training")
	}
}
