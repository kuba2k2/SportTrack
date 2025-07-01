package pl.szczodrzynski.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import pl.szczodrzynski.tracker.data.entity.serializer.ZonedDateTimeSerializer
import java.time.ZonedDateTime

@Serializable
@Entity(
	tableName = "training",
	indices = [Index("dateTime", unique = true)],
)
data class Training(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,

	@Serializable(with = ZonedDateTimeSerializer::class)
	val dateTime: ZonedDateTime?,

	val title: String,
	val description: String?,

	val locationName: String?,
	val locationLat: Double?,
	val locationLon: Double?,
)
