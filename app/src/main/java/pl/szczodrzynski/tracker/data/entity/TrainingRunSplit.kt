package pl.szczodrzynski.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
	tableName = "trainingRunSplit",
	indices = [Index("startDistance", "endDistance", unique = true)],
)
data class TrainingRunSplit(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val trainingRunId: Int,

	val startDistance: Int,
	val endDistance: Int,
	val timestamp: Int,
)
