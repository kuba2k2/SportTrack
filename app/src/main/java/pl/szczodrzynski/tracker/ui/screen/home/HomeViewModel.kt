package pl.szczodrzynski.tracker.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.szczodrzynski.tracker.data.db.AppDb
import pl.szczodrzynski.tracker.data.entity.Training
import pl.szczodrzynski.tracker.manager.TrackerManager
import pl.szczodrzynski.tracker.service.data.TrackerCommand
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
	val manager: TrackerManager,
	private val appDb: AppDb,
) : ViewModel() {

	fun createTraining(title: String) = viewModelScope.launch {
		manager.sendCommand(TrackerCommand.reset())
		val training = Training(
			title = title,
		)
		appDb.trainingDao.insert(training)
	}
}
