package pl.szczodrzynski.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
	tableName = "trainingRunSplit",
	indices = [Index("trainingRunId", "timestamp", unique = true)],
)
data class TrainingRunSplit(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val trainingRunId: Int,

	val timestamp: Int,
	val type: Type = Type.SPLIT,
) {
	enum class Type {
		SPLIT,
		REACTION_BTN,
		REACTION_OPT,
	}
}
