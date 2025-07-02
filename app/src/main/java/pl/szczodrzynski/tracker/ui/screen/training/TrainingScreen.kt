package pl.szczodrzynski.tracker.ui.screen.training

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview

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

	// fetch training location if not already set
	var hasLocationPermission by remember { mutableStateOf(vm.checkLocationPermission(context)) }
	val launcher = rememberLauncherForActivityResult(RequestPermission()) {
		hasLocationPermission = it
	}
	LaunchedEffect(hasLocationPermission) {
		if (training.training.locationName != null)
			return@LaunchedEffect
		if (!hasLocationPermission) {
			launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
			return@LaunchedEffect
		}
		vm.fetchTrainingLocation()
	}

	Text("Training: $training")
}
