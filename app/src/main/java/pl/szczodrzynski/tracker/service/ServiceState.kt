package pl.szczodrzynski.tracker.service

sealed interface ServiceState {
	data object Disconnected : ServiceState
	data object Connected : ServiceState
}
