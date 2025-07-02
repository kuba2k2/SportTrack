package pl.szczodrzynski.tracker.ui.screen.training

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import pl.szczodrzynski.tracker.ui.screen.training.components.TrainingMap
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

	val trainingFull = when (val localState = state) {
		is TrainingViewModel.State.Loading -> {
			TrainingLoading(vm)
			return
		}

		is TrainingViewModel.State.InProgress -> localState.training
		is TrainingViewModel.State.Finished -> localState.training
	}
	val training = trainingFull.training

	var locationInProgress by remember { mutableStateOf(false) }
	var locationRetryCount by remember { mutableIntStateOf(0) }

	if (state is TrainingViewModel.State.InProgress) {
		TrainingMetadataUpdater(
			vm = vm,
			retryCount = locationRetryCount,
			onLocationProgress = {
				locationInProgress = it
			},
		)
	}

	Column(modifier = Modifier.fillMaxSize()) {
		Text(
			training.title,
			modifier = Modifier
				.padding(horizontal = 16.dp, vertical = 16.dp),
			style = MaterialTheme.typography.headlineMedium,
		)

		TrainingMap(
			training = training,
			isLoading = locationInProgress,
			modifier = Modifier
				.padding(horizontal = 16.dp)
				.fillMaxWidth()
				.height(200.dp),
			onRetry = {
				locationRetryCount++
			},
		)

		Text("Training: $training")
	}
}
