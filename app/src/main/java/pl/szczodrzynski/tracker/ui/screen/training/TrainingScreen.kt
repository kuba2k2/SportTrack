package pl.szczodrzynski.tracker.ui.screen.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
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
import pl.szczodrzynski.tracker.service.data.ConnectionState
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import pl.szczodrzynski.tracker.ui.screen.training.components.TrainingController
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
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun TrainingScreen(
	trainingId: Int? = null,
	vm: TrainingViewModel = hiltViewModel(),
) {
	val mainVm = LocalMainViewModel.current
	val state by vm.state.collectAsStateWithLifecycle()
	val connectionState by mainVm.connectionState.collectAsStateWithLifecycle()
	val trackerConfig by vm.manager.trackerConfig.collectAsStateWithLifecycle()

	val trainingFull = when (val localState = state) {
		is TrainingViewModel.State.Loading -> {
			TrainingLoading(vm, trainingId)
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

	LazyColumn(modifier = Modifier.fillMaxSize()) {
		item(key = "title") {
			Text(
				training.title,
				modifier = Modifier
					.padding(horizontal = 16.dp)
					.padding(top = 16.dp),
				style = MaterialTheme.typography.headlineMedium,
			)
		}

		item(key = "map") {
			TrainingMap(
				training = training,
				isLoading = locationInProgress,
				onRetry = {
					locationRetryCount++
				},
			)
		}

		item(key = "dbg1") {
			Text("Training: $training")
			if (vm.manager.isStarted)
				Text("Runs: ${trainingFull.runList}")
		}

		item(key = "spacer") {
			Spacer(modifier = Modifier.height(FloatingToolbarDefaults.ContainerSize + FloatingToolbarDefaults.ScreenOffset + 16.dp))
		}
	}

	if (state is TrainingViewModel.State.InProgress) {
		Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
			TrainingController(
				isConnected = connectionState is ConnectionState.Connected,
				trackerConfig = trackerConfig,
				finishTimeoutFlow = vm.manager.finishTimeout,
				onConnectClick = {
					mainVm.navigate(NavTarget.Home)
				},
				onCommand = vm::sendCommand,
			)
		}
	}
}
