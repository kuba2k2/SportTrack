package pl.szczodrzynski.tracker.ui.screen.training

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import pl.szczodrzynski.tracker.ui.screen.training.metadata.TrainingMetadataUpdater

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

	val training = when (val localState = state) {
		is TrainingViewModel.State.Loading -> {
			TrainingLoading(vm)
			return
		}

		is TrainingViewModel.State.InProgress -> localState.training
		is TrainingViewModel.State.Finished -> localState.training
	}

	var locationInProgress by remember { mutableStateOf(false) }

	if (state is TrainingViewModel.State.InProgress) {
		TrainingMetadataUpdater(
			vm = vm,
			onLocationProgress = {
				locationInProgress = it
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
