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
import kotlinx.coroutines.withContext
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

	private var trainingRun: TrainingRun? = null
	private val trainingRunSplits: MutableList<TrainingRunSplit> = mutableListOf()
	private var trainingRunTimeout: Job? = null

	sealed interface State {
		data object Idle : State
		data class InProgress(
			val trainingRun: TrainingRun,
			val splits: List<TrainingRunSplit>,
			val athlete: Athlete?,
			val lastResult: TrackerResult,
			val finishTimeout: Int,
		) : State
	}

	private val _state = MutableStateFlow<State>(State.Idle)
	val runState = _state.asStateFlow()

	var totalDistance = MutableStateFlow(10000)
	var sensorDistance = MutableStateFlow(listOf(10, 10, 10))
	var finishTimeout = MutableStateFlow(20000)
	var athlete = MutableStateFlow<Athlete?>(null)

	suspend fun saveResult(result: TrackerResult) = withContext(Dispatchers.IO) {
		Timber.d("Received result: $result")
		val training = training.value ?: return@withContext

		val startCommand =
			result.mode == TrackerConfig.Mode.START_ON_SIGNAL && result.type == TrackerResult.Type.ON_YOUR_MARKS
				|| result.mode == TrackerConfig.Mode.FLYING_START && result.type == TrackerResult.Type.START
		val createNewRun = trainingRun == null || startCommand

		// create a new run if necessary
		if (createNewRun) {
			appDb.trainingRunDao.setAllFinished()
			val newRun = TrainingRun(
				trainingId = training.id,
				// TODO use string resources
				title = "Bieg na ${totalDistance.value / 100} m",
				totalDistance = totalDistance.value,
				sensorDistance = sensorDistance.value,
				isFlyingTest = result.mode == TrackerConfig.Mode.FLYING_START,
				athleteId = athlete.value?.id,
				isFinished = false,
			)
			Timber.d("Creating run: $newRun")
			val runId = appDb.trainingRunDao.insert(newRun).toInt()
			trainingRun = newRun.copy(id = runId)
			trainingRunSplits.clear()
		}
		// cancel any previous timeouts
		trainingRunTimeout?.cancel()

		// under normal circumstances, this cannot be null at this point
		val trainingRun = trainingRun ?: return@withContext

		// insert the split if available already
		if (result.millis != null) {
			when (result.type) {
				TrackerResult.Type.SPLIT -> TrainingRunSplit.Type.SPLIT
				TrackerResult.Type.REACTION_BTN -> TrainingRunSplit.Type.REACTION_BTN
				TrackerResult.Type.REACTION_OPT -> TrainingRunSplit.Type.REACTION_OPT
				else -> null
			}?.let { splitType ->
				val split = TrainingRunSplit(
					trainingRunId = trainingRun.id,
					timestamp = result.millis,
					type = splitType,
				)
				Timber.d("Creating split: $split")
				appDb.trainingRunSplitDao.insert(split)
				trainingRunSplits.add(split)
			}
		}

		// set a timeout if at least one split is already saved
		val hasActualSplit = trainingRunSplits.any { it.type == TrainingRunSplit.Type.SPLIT }
		if (hasActualSplit && finishTimeout.value != 0) {
			trainingRunTimeout = launchTimeout()
		}

		_state.update {
			State.InProgress(
				trainingRun = trainingRun,
				splits = trainingRunSplits,
				athlete = athlete.value,
				lastResult = result,
				finishTimeout = finishTimeout.value,
			)
		}
	}

	private fun launchTimeout() = launch(Dispatchers.IO) {
		delay(finishTimeout.value.toLong())
		Timber.d("Finishing run $trainingRun")
		finishRun()
	}

	private suspend fun finishRun() {
		// save the finished run to database
		trainingRun?.copy(isFinished = true)?.let {
			appDb.trainingRunDao.update(it)
		}
		// remove empty runs
		appDb.trainingRunDao.cleanupEmpty()
		// clear run data
		trainingRun = null
		trainingRunSplits.clear()
		// reset or restart the device
		if (trackerConfig.value.mode == TrackerConfig.Mode.FLYING_START)
			sendCommand(TrackerCommand.start())
		else
			sendCommand(TrackerCommand.reset())
		// update the state
		_state.update { State.Idle }
		// cancel last, as finishRun() is ran from that same job
		trainingRunTimeout?.cancel()
	}

	fun finishCurrentRun() = launch(Dispatchers.IO) {
		finishRun()
	}
}
