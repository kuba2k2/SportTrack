package pl.szczodrzynski.tracker.ui.main

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.szczodrzynski.tracker.manager.TrackerManager
import pl.szczodrzynski.tracker.service.TrackerService
import pl.szczodrzynski.tracker.service.data.ConnectionState
import pl.szczodrzynski.tracker.service.data.ServiceState
import pl.szczodrzynski.tracker.ui.NavTarget
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	val manager: TrackerManager,
) : ViewModel(), ServiceConnection {

	var initialRoute: NavTarget = NavTarget.Login
	var nextRoute = MutableSharedFlow<NavTarget>(replay = 1)
		private set

	var currentUser: FirebaseUser? by mutableStateOf(null)

	var binder: TrackerService.TrackerServiceBinder? = null
		private set
	private var serviceJob: Job? = null

	private val _serviceState = MutableStateFlow<ServiceState>(ServiceState.Disconnected)
	val serviceState = _serviceState.asStateFlow()

	private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.NoBluetoothSupport)
	val connectionState = _connectionState.asStateFlow()

	init {
		try {
			val auth = Firebase.auth
			currentUser = auth.currentUser
			auth.addAuthStateListener {
				currentUser = it.currentUser
			}
		} catch (e: IllegalStateException) {
			// ignore FirebaseApp not initialized (likely in @Preview)
		}
		if (currentUser != null)
			initialRoute = NavTarget.Home
	}

	fun navigate(route: NavTarget) {
		Timber.d("Navigating to $route")
		nextRoute.tryEmit(route)
	}

	override fun onServiceConnected(className: ComponentName, service: IBinder) {
		Timber.d("Service $className connected")
		binder = service as? TrackerService.TrackerServiceBinder ?: return
		// create a job to forward state flows from Service to MainViewModel
		serviceJob = viewModelScope.launch {
			_connectionState.emitAll(binder?.connectionState ?: return@launch)
		}
		_serviceState.update { ServiceState.Connected }
	}

	override fun onServiceDisconnected(className: ComponentName) {
		Timber.d("Service $className disconnected")
		_serviceState.update { ServiceState.Disconnected }
		serviceJob?.cancel()
		binder = null
	}
}

val LocalMainViewModel = staticCompositionLocalOf<MainViewModel> {
	error("MainViewModel not provided")
}
