package pl.szczodrzynski.tracker.ui.screen.training

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun TrainingLoading(
	vm: TrainingViewModel,
) {
	Box(Modifier.fillMaxSize()) {
		LoadingIndicator(
			modifier = Modifier.align(Alignment.Center),
		)
	}

	val training by vm.manager.training.collectAsStateWithLifecycle()
	LaunchedEffect(training) {
		training?.let {
			vm.loadTraining(it.id)
		}
	}
}
