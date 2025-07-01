package pl.szczodrzynski.tracker.data.entity.serializer

import androidx.room.TypeConverter
import java.time.Instant

class InstantConverter {

	@TypeConverter
	fun toInstant(value: Long?) = value?.let(Instant::ofEpochMilli)

	@TypeConverter
	fun fromInstant(value: Instant?) = value?.toEpochMilli()
}
