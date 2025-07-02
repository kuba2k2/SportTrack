package pl.szczodrzynski.tracker.ui.screen.training

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.szczodrzynski.tracker.ui.components.FullscreenLoadingIndicator

@Composable
fun TrainingLoading(
	vm: TrainingViewModel,
	trainingId: Int?,
) {
	FullscreenLoadingIndicator()

	val training by vm.manager.training.collectAsStateWithLifecycle()
	LaunchedEffect(trainingId) {
		vm.loadTraining(trainingId ?: training?.id ?: 0)
	}
}
