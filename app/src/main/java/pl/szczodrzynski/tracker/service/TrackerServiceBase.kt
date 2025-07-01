package pl.szczodrzynski.tracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import pl.szczodrzynski.tracker.R
import timber.log.Timber

abstract class TrackerServiceBase : Service(), CoroutineScope {

	override val coroutineContext = Job() + Dispatchers.IO
	protected lateinit var prefs: SharedPreferences

	private var bluetoothManager: BluetoothManager? = null
	protected var bluetoothAdapter: BluetoothAdapter? = null
	protected var bluetoothSocket: BluetoothSocket? = null
	protected var bluetoothException: Exception? = null

	protected var isForeground = false
		private set

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

	override fun onDestroy() {
		super.onDestroy()
		Timber.d("Destroying $this")
		broadcastReceiver.unregister()
	}

	protected fun foregroundStart() {
		if (isForeground)
			return
		val notification = NotificationCompat.Builder(this, "TrackerService")
			.setContentTitle(getString(R.string.service_notification_title))
			.setContentText(getString(R.string.service_notification_text))
			.setSmallIcon(R.drawable.ic_service)
			.build()

		isForeground = true
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

	protected fun foregroundStop() {
		isForeground = false
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

	protected abstract fun updateState()
}
