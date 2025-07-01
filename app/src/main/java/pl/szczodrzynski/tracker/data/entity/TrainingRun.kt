package pl.szczodrzynski.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import pl.szczodrzynski.tracker.data.entity.serializer.InstantSerializer
import java.time.Instant

@Serializable
@Entity(
	tableName = "trainingRun",
	indices = [Index("dateTime", unique = true)],
)
data class TrainingRun(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val trainingId: Int,

	@Serializable(with = InstantSerializer::class)
	val dateTime: Instant = Instant.now(),

	val title: String,
	val description: String? = null,

	val totalDistance: Int,
	val isFlyingTest: Boolean,
	val athleteId: Int? = null,
)
