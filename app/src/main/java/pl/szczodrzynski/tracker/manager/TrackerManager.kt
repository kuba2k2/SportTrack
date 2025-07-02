package pl.szczodrzynski.tracker.manager

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.szczodrzynski.tracker.data.db.AppDb
import pl.szczodrzynski.tracker.data.entity.Athlete
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.TrainingRunSplit
import pl.szczodrzynski.tracker.service.data.TrackerCommand
import pl.szczodrzynski.tracker.service.data.TrackerConfig
import pl.szczodrzynski.tracker.service.data.TrackerResult
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class TrackerManager @Inject constructor(
	private val appDb: AppDb,
) : CoroutineScope {

	override val coroutineContext = Job() + Dispatchers.IO

	private val connection = TrackerConnection(::saveResult)
	val trackerConfig = connection.trackerConfig

	val training = appDb.trainingDao.getLatest()
		.map { list ->
			list.firstOrNull {
				it.dateTime.plus(6, ChronoUnit.HOURS) > Instant.now()
			}
		}
		.stateIn(scope = this, started = SharingStarted.Eagerly, initialValue = null)

	fun start(socket: BluetoothSocket) = connection.start(socket)
	fun stop() = connection.stop()
	suspend fun sendCommand(command: TrackerCommand) = connection.sendCommand(command)

	private val _lastResult = MutableStateFlow<TrackerResult?>(null)
	val lastResult = _lastResult.asStateFlow()

	private var trainingRun: TrainingRun? = null
	private var trainingRunHasSplits = false
	private var trainingRunTimeout: Job? = null

	private var startedAutomatically = false
	var isStarted = false
		private set

	var totalDistance = 10000
	var sensorDistance = listOf(10, 10, 10)
	var finishTimeout = 20000
	var athlete: Athlete? = null

	fun saveResult(result: TrackerResult) = launch {
		Timber.d("Received result: $result")
		val training = training.value ?: return@launch

		val startCommand =
			result.mode == TrackerConfig.Mode.START_ON_SIGNAL && result.type == TrackerResult.Type.ON_YOUR_MARKS
				|| result.mode == TrackerConfig.Mode.FLYING_START && result.type == TrackerResult.Type.START
		val createNewRun = trainingRun == null || startCommand

		if (!startCommand || !startedAutomatically)
			isStarted = true
		startedAutomatically = false

		// create a new run if necessary
		if (createNewRun) {
			appDb.trainingRunDao.setAllFinished()
			val newRun = TrainingRun(
				trainingId = training.id,
				title = "",
				totalDistance = totalDistance,
				sensorDistance = sensorDistance,
				isFlyingTest = result.mode == TrackerConfig.Mode.FLYING_START,
				athleteId = athlete?.id,
				isFinished = false,
			)
			Timber.d("Creating run: $newRun")
			val runId = appDb.trainingRunDao.insert(newRun).toInt()
			trainingRun = newRun.copy(id = runId)
			trainingRunHasSplits = false
		}
		// cancel any previous timeouts
		trainingRunTimeout?.cancel()

		// insert the split if available already
		val runId = trainingRun?.id
		if (result.millis != null && runId != null) {
			val split = TrainingRunSplit(
				trainingRunId = runId,
				timestamp = result.millis,
			)
			Timber.d("Creating split: $split")
			appDb.trainingRunSplitDao.insert(split)
			trainingRunHasSplits = true
		}

		// set a timeout if at least one split is already saved
		if (trainingRunHasSplits) {
			trainingRunTimeout = launch(Dispatchers.IO) {
				delay(finishTimeout.toLong())
				Timber.d("Finishing run $trainingRun")
				finishRun()
			}
		}

		_lastResult.update { result }
	}

	suspend fun finishRun() {
		trainingRun?.copy(isFinished = true)?.let {
			appDb.trainingRunDao.update(it)
		}
		trainingRun = null
		trainingRunHasSplits = false
		isStarted = false
		if (trackerConfig.value.mode == TrackerConfig.Mode.FLYING_START) {
			startedAutomatically = true
			sendCommand(TrackerCommand.start())
		} else {
			startedAutomatically = false
			sendCommand(TrackerCommand.reset())
		}
		// remove empty runs
		appDb.trainingRunDao.cleanupEmpty()
		// cancel last, as finishRun() is ran from that same job
		trainingRunTimeout?.cancel()
	}
}
