package pl.szczodrzynski.tracker.manager

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import pl.szczodrzynski.tracker.data.db.AppDb
import pl.szczodrzynski.tracker.service.data.TrackerCommand
import pl.szczodrzynski.tracker.service.data.TrackerConfig
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class TrackerManager @Inject constructor(
	private val appDb: AppDb,
) : CoroutineScope {

	override val coroutineContext = Job() + Dispatchers.IO

	private val connection = TrackerConnection()
	val trackerConfig = connection.trackerConfig

	val training = appDb.trainingDao.getLatest()
		.map { it.firstOrNull() }
		.takeWhile {
			it == null || it.dateTime.plus(6, ChronoUnit.HOURS) > Instant.now()
		}
		.stateIn(
			scope = this,
			started = SharingStarted.WhileSubscribed(1000L),
			initialValue = null
		)

	fun start(socket: BluetoothSocket) = connection.start(socket)
	fun stop() = connection.stop()
	suspend fun sendCommand(command: TrackerCommand) = connection.sendCommand(command)

	suspend fun configureMode(mode: TrackerConfig.Mode) = connection.sendCommand(TrackerCommand.mode(mode))
	suspend fun configureDelayReady(delayMs: Int) = connection.sendCommand(TrackerCommand.delayReady(delayMs))
	suspend fun configureDelayMin(delayMs: Int) = connection.sendCommand(TrackerCommand.delayStartMin(delayMs))
	suspend fun configureDelayMax(delayMs: Int) = connection.sendCommand(TrackerCommand.delayStartMax(delayMs))
	suspend fun configureSensorBeep(enabled: Boolean) = connection.sendCommand(TrackerCommand.sensorBeep(enabled))

	suspend fun resetRun() {}
	suspend fun startRun() {}
}
