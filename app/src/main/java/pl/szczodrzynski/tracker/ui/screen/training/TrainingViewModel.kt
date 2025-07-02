package pl.szczodrzynski.tracker.ui.screen.training

import android.Manifest
import android.content.Context
import androidx.core.content.PermissionChecker
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.szczodrzynski.tracker.data.db.AppDb
import pl.szczodrzynski.tracker.data.entity.joins.TrainingFull
import pl.szczodrzynski.tracker.manager.TrackerManager
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(
	val manager: TrackerManager,
	private val appDb: AppDb,
) : ViewModel() {

	sealed interface State {
		data object Loading : State
		data class InProgress(val training: TrainingFull) : State
		data class Finished(val training: TrainingFull) : State
	}

	private val _state = MutableStateFlow<State>(State.Loading)
	val state = _state.asStateFlow()

	private lateinit var training: TrainingFull

	fun loadTraining(id: Int) = viewModelScope.launch {
		if (state.value !is State.Loading)
			return@launch
		_state.update { State.Loading }
		appDb.trainingDao.getOneFull(id).collect { training ->
			this@TrainingViewModel.training = training
			if (training.training.id == manager.training.value?.id)
				_state.update { State.InProgress(training) }
			else
				_state.update { State.Finished(training) }
		}
	}

	fun checkLocationPermission(context: Context) =
		PermissionChecker.checkSelfPermission(
			context,
			Manifest.permission.ACCESS_FINE_LOCATION
		) == PermissionChecker.PERMISSION_GRANTED

	fun fetchTrainingLocation() = viewModelScope.launch {
		Timber.d("Fetching location for training $training")
	}
}
