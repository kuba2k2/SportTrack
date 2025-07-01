package pl.szczodrzynski.tracker.data.entity.serializer

import androidx.room.TypeConverter
import java.time.ZonedDateTime

class ZonedDateTimeConverter {

	@TypeConverter
	fun toZonedDateTime(value: String?) = value?.let(ZonedDateTime::parse)

	@TypeConverter
	fun fromZonedDateTime(value: ZonedDateTime?) = value?.toString()
}
