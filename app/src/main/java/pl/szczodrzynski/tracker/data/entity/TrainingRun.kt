package pl.szczodrzynski.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import pl.szczodrzynski.tracker.data.entity.serializer.ZonedDateTimeSerializer
import java.time.ZonedDateTime

@Serializable
@Entity(
	tableName = "trainingRun",
	indices = [Index("dateTime", unique = true)],
)
data class TrainingRun(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val trainingId: Int,

	@Serializable(with = ZonedDateTimeSerializer::class)
	val dateTime: ZonedDateTime?,

	val title: String,
	val description: String?,

	val totalDistance: Int,
	val isFlyingTest: Boolean,
	val athleteId: Int?,
)
