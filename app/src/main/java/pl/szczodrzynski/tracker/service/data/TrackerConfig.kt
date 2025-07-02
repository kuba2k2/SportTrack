package pl.szczodrzynski.tracker.service.data

data class TrackerConfig(
	val version: String? = null,
	val temperature: Float? = null,
	val error: Boolean = false,
	val mode: Mode = Mode.START_ON_SIGNAL,
	val delayReady: Int = 15000,
	val delayStartMin: Int = 1500,
	val delayStartMax: Int = 2500,
	val sensorBeep: Boolean = false,
) {
	enum class Mode(val value: Int) {
		START_ON_SIGNAL(1),
		FLYING_START(2),
		REACTION_TEST(3),
	}

	@Throws(NumberFormatException::class, NoSuchElementException::class)
	fun update(type: TrackerCommand.Type, value: String): TrackerConfig = when (type) {
		TrackerCommand.Type.VERSION -> copy(version = value)
		TrackerCommand.Type.TEMPERATURE -> copy(temperature = value.toFloat())
		TrackerCommand.Type.ERROR -> copy(error = value == "1")
		TrackerCommand.Type.MODE -> copy(mode = Mode.entries.first { it.value.toString() == value })
		TrackerCommand.Type.DELAY_READY -> copy(delayReady = value.toInt())
		TrackerCommand.Type.DELAY_START_MIN -> copy(delayStartMin = value.toInt())
		TrackerCommand.Type.DELAY_START_MAX -> copy(delayStartMax = value.toInt())
		TrackerCommand.Type.SENSOR_BEEP -> copy(sensorBeep = value == "1")
		else -> this
	}
}
