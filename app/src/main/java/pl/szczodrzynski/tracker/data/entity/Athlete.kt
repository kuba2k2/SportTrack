package pl.szczodrzynski.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
	tableName = "athlete",
	indices = [Index("name", unique = true)],
)
data class Athlete(
	@PrimaryKey
	val id: Int = 0,
	val name: String,
)
