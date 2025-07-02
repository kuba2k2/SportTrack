package pl.szczodrzynski.tracker.ui.screen.training.metadata

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import pl.szczodrzynski.tracker.ui.screen.training.TrainingViewModel
import timber.log.Timber

@Composable
fun TrainingMetadataUpdater(
	vm: TrainingViewModel,
	onLocationProgress: (inProgress: Boolean) -> Unit,
) {
	val context = LocalContext.current

	var locationPermissions by remember { mutableStateOf(mapOf<String, Boolean>()) }
	var locationWasEnabled by remember { mutableIntStateOf(0) }

	val permissionLauncher = rememberLauncherForActivityResult(RequestMultiplePermissions()) {
		onLocationProgress(false)
		locationPermissions = it
	}
	val locationLauncher = rememberLauncherForActivityResult(StartIntentSenderForResult()) {
		onLocationProgress(false)
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
			onProgress = onLocationProgress,
		)
	}
}
