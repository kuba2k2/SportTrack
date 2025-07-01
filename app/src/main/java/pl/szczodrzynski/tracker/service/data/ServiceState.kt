package pl.szczodrzynski.tracker.service.data

sealed interface ServiceState {
	data object Disconnected : ServiceState
	data object Connected : ServiceState
}
