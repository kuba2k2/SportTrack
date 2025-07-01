package pl.szczodrzynski.tracker.service

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.szczodrzynski.tracker.service.data.TrackerCommand
import pl.szczodrzynski.tracker.service.data.TrackerConfig
import pl.szczodrzynski.tracker.service.data.TrackerDevice
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class TrackerConnection(
	private val device: TrackerDevice,
	private val socket: BluetoothSocket,
	private val onConfigChanged: (TrackerConfig) -> Unit = {},
) : CoroutineScope {

	override val coroutineContext = Job() + Dispatchers.IO

	private val reader = socket.inputStream.bufferedReader()
	private val writer = socket.outputStream.bufferedWriter()

	private val pendingCommands = ConcurrentHashMap<TrackerCommand.Type, CompletableDeferred<Boolean>>()

	private var trackerConfig = TrackerConfig()

	fun start() = launch {
		try {
			Timber.d("Starting tracker connection with $device")
			// emit the initial config
			onConfigChanged(trackerConfig)

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
							trackerConfig = trackerConfig.update(commandType, value)
							onConfigChanged(trackerConfig)
							Timber.d("Configuration updated: $trackerConfig")
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
			for ((_, deferred) in pendingCommands) {
				// use complete() instead of completeExceptionally(), so that suspend callers don't crash
				deferred.complete(true)
			}
			pendingCommands.clear()
			cancel("Connection interrupted", e)
		}
	}

	fun stop() {
		Timber.d("Stopping tracker connection with $device")
		for ((_, deferred) in pendingCommands) {
			deferred.cancel()
		}
		pendingCommands.clear()
		cancel(CancellationException())
	}

	fun writeCommand(command: TrackerCommand) {
		val line = if (command.value == null)
			"${command.type.char}"
		else
			"${command.type.char}=${command.value}"
		Timber.v("<- TX: '$line'")
		writer.write(line + "\n")
		writer.flush()
	}

	suspend fun sendCommand(command: TrackerCommand) {
		Timber.d("Sending command: $command")
		val deferred = CompletableDeferred<Boolean>()
		pendingCommands[command.type] = deferred

		writeCommand(command)
		deferred.await()
		Timber.d("Command completed: $command")
	}
}
