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
import pl.szczodrzynski.tracker.service.TrackerService
import pl.szczodrzynski.tracker.ui.NavTarget
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel(), ServiceConnection {

	var initialRoute: NavTarget = NavTarget.Login
	var nextRoute = MutableSharedFlow<NavTarget>(replay = 1)
		private set

	var currentUser: FirebaseUser? by mutableStateOf(null)

	private var serviceBinder: TrackerService.TrackerServiceBinder? = null
	private var serviceJob: Job? = null

	private val _serviceState =
		MutableStateFlow<TrackerService.ServiceState>(TrackerService.ServiceState.Disconnected)
	val serviceState = _serviceState.asStateFlow()

	private val _connectionState =
		MutableStateFlow<TrackerService.ConnectionState>(TrackerService.ConnectionState.NoBluetoothSupport)
	val connectionState = _connectionState.asStateFlow()

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

	override fun onServiceConnected(className: ComponentName, service: IBinder) {
		Timber.d("Service $className connected")
		serviceBinder = service as? TrackerService.TrackerServiceBinder ?: return
		serviceJob = viewModelScope.launch {
			_connectionState.emitAll(serviceBinder?.connectionState ?: return@launch)
		}
		_serviceState.update { TrackerService.ServiceState.Connected }
	}

	override fun onServiceDisconnected(className: ComponentName) {
		Timber.d("Service $className disconnected")
		_serviceState.update { TrackerService.ServiceState.Disconnected }
		serviceJob?.cancel()
		serviceBinder = null
	}
}

val LocalMainViewModel = staticCompositionLocalOf<MainViewModel> {
	error("MainViewModel not provided")
}
