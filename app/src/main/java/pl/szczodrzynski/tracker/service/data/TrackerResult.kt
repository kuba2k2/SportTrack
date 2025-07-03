package pl.szczodrzynski.tracker.service.data

import timber.log.Timber

data class TrackerResult(
	val mode: TrackerConfig.Mode,
	val type: Type,
	val millis: Int? = null,
) {
	enum class Type(val char: Char, val hasParam: Boolean = false) {
		ON_YOUR_MARKS('M'),
		READY('R'),
		START('S'),
		ERROR('E'), // unused - reaction test only
		DELAY('D', hasParam = true),
		SPLIT('L', hasParam = true),
		REACTION_BTN('B', hasParam = true),
		REACTION_OPT('O', hasParam = true),
	}

	companion object {
		fun parseResult(parts: List<String>): TrackerResult? {
			if (parts.size < 2)
				return null
			val mode = TrackerConfig.Mode.entries.firstOrNull { it.value == parts[0].toIntOrNull() }
			if (mode == null) {
				Timber.w("Unknown tracker mode '${parts[0]}'")
				return null
			}
			val type = Type.entries.firstOrNull { it.char == parts[1][0] }
			if (type == null) {
				Timber.w("Unknown result type '${parts[1]}'")
				return null
			}
			if (!type.hasParam)
				return TrackerResult(mode, type)
			if (parts.size < 3)
				return null
			val millis = parts[2].toIntOrNull() ?: return null
			return TrackerResult(mode, type, millis)
		}
	}
}
