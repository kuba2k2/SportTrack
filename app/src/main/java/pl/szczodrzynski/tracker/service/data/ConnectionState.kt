package pl.szczodrzynski.tracker.service.data

sealed interface ConnectionState {
	data object NoBluetoothSupport : ConnectionState
	data object NoPermissions : ConnectionState
	data object BluetoothNotEnabled : ConnectionState
	data class Disconnected(val device: TrackerDevice?, val error: Throwable? = null) : ConnectionState
	data class Connecting(val device: TrackerDevice) : ConnectionState
	data class Connected(val device: TrackerDevice) : ConnectionState
}
