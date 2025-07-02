package pl.szczodrzynski.tracker.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.szczodrzynski.tracker.data.db.AppDb
import pl.szczodrzynski.tracker.data.entity.Training
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
	private val appDb: AppDb,
) : ViewModel() {

	sealed interface State {
		data object Loading : State
		data class InList(val trainingList: List<Training>) : State
		data class InTraining(val trainingId: Int) : State
	}

	private val _state = MutableStateFlow<State>(State.Loading)
	val state = _state.asStateFlow()

	fun loadList() = viewModelScope.launch {
		_state.update { State.Loading }
		appDb.trainingDao.getLatest().collect { trainingList ->
			_state.update { State.InList(trainingList) }
		}
	}

	fun loadTraining(trainingId: Int) {
		_state.update { State.InTraining(trainingId) }
	}
}
