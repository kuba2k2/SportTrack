package pl.szczodrzynski.tracker.ui.screen.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.szczodrzynski.tracker.data.db.AppDb
import pl.szczodrzynski.tracker.manager.TrackerManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
	val manager: TrackerManager,
	private val appDb: AppDb,
) : ViewModel()
