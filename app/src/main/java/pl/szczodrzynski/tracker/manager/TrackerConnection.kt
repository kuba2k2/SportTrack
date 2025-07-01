package pl.szczodrzynski.tracker.manager

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.szczodrzynski.tracker.service.data.TrackerCommand
import pl.szczodrzynski.tracker.service.data.TrackerConfig
import timber.log.Timber
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class TrackerConnection : CoroutineScope {

	override val coroutineContext = Job() + Dispatchers.IO

	private var connectionJob: Job? = null
	private val pendingCommands = ConcurrentHashMap<TrackerCommand.Type, CompletableDeferred<Boolean>>()
	private var writer: BufferedWriter? = null

	private val _trackerConfig = MutableStateFlow(TrackerConfig())
	val trackerConfig = _trackerConfig.asStateFlow()

	fun start(socket: BluetoothSocket): Job {
		val reader = socket.inputStream.bufferedReader()
		writer = socket.outputStream.bufferedWriter()
		val job = launch(Dispatchers.IO) {
			processData(reader)
		}
		connectionJob = job
		return job
	}

	private fun processData(reader: BufferedReader) {
		try {
			Timber.d("Starting tracker manager")

			// process incoming lines
			while (isActive) {
				val line = reader.readLine()?.trim() ?: throw IOException("EOF")
				if (line.isBlank())
					continue
				Timber.v("-> RX: '$line'")
				when {
					// command ACK response
					line.startsWith("OK.") && line.length == 4 -> {
						val commandType = TrackerCommand.parseCommandType(line[3]) ?: continue
						pendingCommands.remove(commandType)?.complete(true)
					}

					// configuration/runtime value
					line.length >= 3 && line[1] == '=' -> {
						val commandType = TrackerCommand.parseCommandType(line[0]) ?: continue
						val value = line.substring(2)
						// update and emit the new device config
						try {
							val newConfig = trackerConfig.value.update(commandType, value)
							_trackerConfig.update { newConfig }
							Timber.d("Configuration updated: $newConfig")
						} catch (e: Exception) {
							Timber.w(e, "Failed to parse line '$line'")
						}
					}

					// current run results
					line.startsWith("RUN;") -> {

					}
				}
			}
		} catch (e: Exception) {
			Timber.e(e, "Connection interrupted")
			for ((_, deferred) in pendingCommands) {
				// use complete() instead of completeExceptionally(), so that suspend callers don't crash
				deferred.complete(true)
			}
			pendingCommands.clear()
			connectionJob?.cancel("Connection interrupted", e)
		}
	}

	fun stop() {
		Timber.d("Stopping tracker connection")
		for ((_, deferred) in pendingCommands) {
			deferred.cancel()
		}
		pendingCommands.clear()
		connectionJob?.cancel(CancellationException())
		writer = null
		_trackerConfig.update { TrackerConfig() }
	}

	suspend fun sendCommand(command: TrackerCommand) {
		Timber.d("Sending command: $command")
		val deferred = CompletableDeferred<Boolean>()
		pendingCommands[command.type] = deferred

		val line = if (command.value == null)
			"${command.type.char}"
		else
			"${command.type.char}=${command.value}"
		Timber.v("<- TX: '$line'")
		withContext(Dispatchers.IO) {
			writer?.write(line + "\n")
			writer?.flush()
		}

		deferred.await()
		Timber.d("Command completed: $command")
	}
}
