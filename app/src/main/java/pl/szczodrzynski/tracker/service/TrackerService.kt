package pl.szczodrzynski.tracker.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.IntentCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.edit
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.service.Utils.hasBluetoothPermissions
import timber.log.Timber

class TrackerService : Service(), CoroutineScope {

	override val coroutineContext = Job() + Dispatchers.IO
	private lateinit var prefs: SharedPreferences

	private var bluetoothManager: BluetoothManager? = null
	private var bluetoothAdapter: BluetoothAdapter? = null

	private var trackerDevice: TrackerDevice? = null
		set(value) {
			prefs.edit {
				putString("address", value?.address)
			}
			field = value
		}

	private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.NoBluetoothSupport)

	@SuppressLint("MissingPermission")
	override fun onCreate() {
		super.onCreate()
		Timber.d("Creating $this")

		prefs = getSharedPreferences("tracker_params", MODE_PRIVATE)
		broadcastReceiver.register()

		bluetoothManager = getSystemService()
		bluetoothAdapter = bluetoothManager?.adapter
		updateState()

		// create a notification channel
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(
				"TrackerService",
				getString(R.string.service_notification_title),
				NotificationManager.IMPORTANCE_LOW
			)
			getSystemService<NotificationManager>()?.createNotificationChannel(channel)
		}
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_NOT_STICKY
	override fun onBind(intent: Intent) = TrackerServiceBinder()

	override fun onDestroy() {
		super.onDestroy()
		Timber.d("Destroying $this")
		broadcastReceiver.unregister()
	}

	private fun foregroundStart() {
		val notification = NotificationCompat.Builder(this, "TrackerService")
			.setContentTitle(getString(R.string.service_notification_title))
			.setContentText(getString(R.string.service_notification_text))
			.setSmallIcon(R.drawable.ic_service)
			.build()

		ServiceCompat.startForeground(
			this,
			System.currentTimeMillis().toInt(),
			notification,
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
				ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
			else
				0,
		)
	}

	private fun foregroundStop() {
		ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
	}

	private val broadcastReceiver = object : BroadcastReceiver() {
		fun register() {
			val filter = IntentFilter().apply {
				addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
				addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
			}
			registerReceiver(this, filter)
		}

		fun unregister() {
			unregisterReceiver(this)
		}

		override fun onReceive(context: Context, intent: Intent) {
			when (intent.action) {
				BluetoothAdapter.ACTION_STATE_CHANGED -> updateState()
			}
		}
	}

	@SuppressLint("MissingPermission")
	private fun updateState() {
		_connectionState.update {
			when {
				bluetoothAdapter == null -> ConnectionState.NoBluetoothSupport
				!hasBluetoothPermissions() -> ConnectionState.NoPermissions
				bluetoothAdapter?.isEnabled != true -> ConnectionState.BluetoothNotEnabled
				else -> {
					if (trackerDevice == null) {
						val address = prefs.getString("address", null)
						trackerDevice = bluetoothAdapter?.bondedDevices?.firstOrNull {
							it.address == address
						}?.let(::TrackerDevice)
					}
					ConnectionState.Disconnected(trackerDevice)
				}
			}
		}
	}

	inner class TrackerServiceBinder : Binder() {
		val connectionState = _connectionState.asStateFlow()

		fun updateState() = this@TrackerService.updateState()

		fun getBluetoothDevices(scan: Boolean): Flow<Set<TrackerDevice>> {
			val adapter = bluetoothAdapter
				?: return emptyFlow()
			if (PermissionChecker.checkSelfPermission(
					this@TrackerService,
					Manifest.permission.BLUETOOTH_CONNECT
				) != PermissionChecker.PERMISSION_GRANTED
			)
				return emptyFlow()
			if (!adapter.isEnabled)
				return emptyFlow()

			val devices = adapter.bondedDevices.map(::TrackerDevice).toMutableSet()
			if (!scan)
				return flowOf(devices.toSet())

			adapter.cancelDiscovery()

			return callbackFlow {
				trySend(devices.toSet())

				val receiver = object : BroadcastReceiver() {
					override fun onReceive(context: Context, intent: Intent) {
						when (intent.action) {
							BluetoothDevice.ACTION_FOUND -> {
								val device = IntentCompat.getParcelableExtra(
									intent,
									BluetoothDevice.ACTION_FOUND,
									BluetoothDevice::class.java
								) ?: return
								devices.add(TrackerDevice(device))
								trySend(devices.toSet())
							}

							BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
								close()
							}
						}
					}
				}

				val filter = IntentFilter().apply {
					addAction(BluetoothDevice.ACTION_FOUND)
					addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
				}
				registerReceiver(receiver, filter)
				if (!adapter.startDiscovery())
					close()

				awaitClose {
					adapter.cancelDiscovery()
					unregisterReceiver(receiver)
				}
			}
		}

		fun setTrackerDevice(device: TrackerDevice) {
			trackerDevice = device
			updateState()
		}
	}
}
