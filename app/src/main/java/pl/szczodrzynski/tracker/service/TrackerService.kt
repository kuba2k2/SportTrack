package pl.szczodrzynski.tracker.service

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import androidx.core.content.IntentCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.szczodrzynski.tracker.service.Utils.hasBluetoothPermissions
import pl.szczodrzynski.tracker.service.data.ConnectionState
import pl.szczodrzynski.tracker.service.data.TrackerDevice
import timber.log.Timber
import java.io.IOException
import java.util.UUID

class TrackerService : TrackerServiceBase() {
	companion object {
		private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
	}

	private var trackerDevice: TrackerDevice? = null
		set(value) {
			field = value
			if (value == null)
				return
			prefs.edit {
				putString("address", value.address)
			}
		}

	private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.NoBluetoothSupport)

	override fun onBind(intent: Intent) = TrackerServiceBinder()

	@SuppressLint("MissingPermission")
	private fun setDevice() {
		if (trackerDevice != null)
			return
		val address = prefs.getString("address", null)
		trackerDevice = bluetoothAdapter?.bondedDevices?.firstOrNull {
			it.address == address
		}?.let(::TrackerDevice)
	}

	@SuppressLint("MissingPermission")
	override fun updateState() {
		_connectionState.update {
			val state = when {
				bluetoothAdapter == null -> ConnectionState.NoBluetoothSupport
				!hasBluetoothPermissions() -> ConnectionState.NoPermissions
				bluetoothAdapter?.isEnabled != true -> ConnectionState.BluetoothNotEnabled
				else -> {
					setDevice()
					val device = trackerDevice
					val socket = bluetoothSocket
					val exception = bluetoothException
					when {
						device == null -> ConnectionState.Disconnected(null)
						exception != null -> ConnectionState.Disconnected(device, exception)
						socket == null -> ConnectionState.Disconnected(device)
						!socket.isConnected -> ConnectionState.Connecting(device)
						else -> ConnectionState.Connected(device)
					}
				}
			}
			if (state is ConnectionState.Connecting || state is ConnectionState.Connected)
				foregroundStart()
			else
				foregroundStop()
			return@update state
		}
	}

	@SuppressLint("MissingPermission")
	inner class TrackerServiceBinder : Binder() {
		private val foundDevices = mutableSetOf<TrackerDevice>()

		val connectionState = _connectionState.asStateFlow()

		fun updateState() = this@TrackerService.updateState()

		fun getBluetoothDevices(scan: Boolean): Flow<Set<TrackerDevice>> {
			val adapter = bluetoothAdapter
				?: return emptyFlow()
			if (PermissionChecker.checkSelfPermission(
					this@TrackerService,
					Manifest.permission.BLUETOOTH_CONNECT
				) != PermissionChecker.PERMISSION_GRANTED
			) {
				Timber.w("Bluetooth permission not granted")
				return emptyFlow()
			}
			if (!adapter.isEnabled) {
				Timber.w("Bluetooth adapter not enabled")
				return emptyFlow()
			}

			val bondedDevices = adapter.bondedDevices.map(::TrackerDevice)
			if (!scan)
				return flowOf((bondedDevices + foundDevices).distinctBy { it.address }.toSet())
			foundDevices.clear()

			adapter.cancelDiscovery()

			return callbackFlow {
				trySend((bondedDevices + foundDevices).distinctBy { it.address }.toSet())

				val receiver = object : BroadcastReceiver() {
					@SuppressLint("MissingPermission")
					override fun onReceive(context: Context, intent: Intent) {
						when (intent.action) {
							BluetoothDevice.ACTION_FOUND -> {
								val device = IntentCompat.getParcelableExtra(
									intent,
									BluetoothDevice.EXTRA_DEVICE,
									BluetoothDevice::class.java
								) ?: return
								if (device.name == null)
									return
								Timber.d("Found device ${device.name} - ${device.address}")
								val trackerDevice = TrackerDevice(device)
								foundDevices.add(trackerDevice)
								trySend((bondedDevices + foundDevices).distinctBy { it.address }.toSet())
							}

							BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
								Timber.d("Discovery finished")
								close()
							}
						}
					}
				}

				Timber.d("Starting Bluetooth discovery")
				val filter = IntentFilter().apply {
					addAction(BluetoothDevice.ACTION_FOUND)
					addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
				}
				registerReceiver(receiver, filter)
				if (!adapter.startDiscovery()) {
					Timber.w("Discovery failed")
					close()
				}

				awaitClose {
					Timber.d("Flow is closing")
					adapter.cancelDiscovery()
					unregisterReceiver(receiver)
				}
			}
		}

		fun setTrackerDevice(device: TrackerDevice) {
			// cannot change device if it's not in disconnected state
			if (connectionState.value !is ConnectionState.Disconnected)
				return
			trackerDevice = device
			bluetoothException = null
			updateState()
		}

		fun connectDevice() = launch {
			val device = trackerDevice?.bluetoothDevice ?: return@launch
			bluetoothAdapter?.cancelDiscovery()

			val socket = try {
				Timber.e("Connecting to $device")
				val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)!!
				// store socket in service
				bluetoothSocket = socket
				bluetoothException = null
				updateState()
				// connect to the socket
				socket.connect()
				socket
			} catch (e: IOException) {
				Timber.e(e, "Failed to create Bluetooth socket")
				// set the last error
				bluetoothException = e
				// close and remove the failed socket
				bluetoothSocket?.close()
				bluetoothSocket = null
				updateState()
				return@launch
			}

			// connection is successful here
			Timber.d("Connected successfully, socket = $socket")
			updateState()
		}

		fun disconnectDevice() {
			bluetoothAdapter?.cancelDiscovery()
			bluetoothSocket?.close()
			bluetoothSocket = null
			bluetoothException = null
			updateState()
		}
	}
}
