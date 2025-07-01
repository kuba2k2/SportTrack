package pl.szczodrzynski.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import pl.szczodrzynski.tracker.data.entity.serializer.InstantSerializer
import java.time.Instant

@Serializable
@Entity(
	tableName = "trainingComment",
	indices = [Index("dateTime", unique = true)],
)
data class TrainingComment(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val trainingId: Int,

	@Serializable(with = InstantSerializer::class)
	val dateTime: Instant = Instant.now(),

	val comment: String,
)
