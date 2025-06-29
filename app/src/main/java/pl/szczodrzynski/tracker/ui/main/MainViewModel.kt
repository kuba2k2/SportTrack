package pl.szczodrzynski.tracker.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import pl.szczodrzynski.tracker.ui.NavTarget
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
	companion object {
		private const val TAG = "MainViewModel"
	}

	var initialRoute: NavTarget = NavTarget.Login
	var nextRoute = MutableSharedFlow<NavTarget>(replay = 1)
		private set

	var currentUser: FirebaseUser? by mutableStateOf(null)

	init {
		val auth = Firebase.auth
		currentUser = auth.currentUser
		if (currentUser != null)
			initialRoute = NavTarget.Home

		auth.addAuthStateListener {
			currentUser = it.currentUser
		}
	}

	fun navigate(route: NavTarget) {
		nextRoute.tryEmit(route)
	}
}

val LocalMainViewModel = staticCompositionLocalOf<MainViewModel> {
	error("MainViewModel not provided")
}
