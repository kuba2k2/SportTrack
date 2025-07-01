package pl.szczodrzynski.tracker.service.data

import timber.log.Timber

data class TrackerCommand(
	val type: Type,
	val value: String? = null,
) {
	enum class Type(val char: Char) {
		// runtime values
		VERSION(';'),
		TEMPERATURE('*'),

		// configuration values
		MODE('@'),
		DELAY_READY('&'),
		DELAY_START_MIN('('),
		DELAY_START_MAX(')'),
		SENSOR_BEEP('{'),

		// commands
		RESET('='),
		START('+'),
	}

	companion object {
		fun version() = TrackerCommand(Type.VERSION)
		fun temperature() = TrackerCommand(Type.TEMPERATURE)
		fun mode(mode: TrackerConfig.Mode) = TrackerCommand(Type.MODE, mode.value.toString())
		fun delayReady(delayMs: Int) = TrackerCommand(Type.DELAY_READY, delayMs.toString())
		fun delayStartMin(delayMs: Int) = TrackerCommand(Type.DELAY_START_MIN, delayMs.toString())
		fun delayStartMax(delayMs: Int) = TrackerCommand(Type.DELAY_START_MAX, delayMs.toString())
		fun sensorBeep(enabled: Boolean) = TrackerCommand(Type.SENSOR_BEEP, if (enabled) "1" else "0")
		fun reset() = TrackerCommand(Type.RESET)
		fun start() = TrackerCommand(Type.START)

		fun parseCommandType(char: Char): Type? {
			val commandType = Type.entries.firstOrNull { it.char == char }
			if (commandType == null)
				Timber.w("Unknown command type '$char'")
			return commandType
		}
	}
}
