package pl.szczodrzynski.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import pl.szczodrzynski.tracker.data.entity.serializer.InstantSerializer
import java.time.Instant

@Serializable
@Entity(
	tableName = "trainingWeather",
	indices = [Index("dateTime", unique = true)],
)
data class TrainingWeather(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val trainingId: Int,

	@Serializable(with = InstantSerializer::class)
	val dateTime: Instant = Instant.now(),

	val weather: String? = null,
	val precipitation: String? = null,

	val localTemperature: Float? = null,
	val temperature: Float? = null,
	val apparentTemperature: Float? = null,
	val humidity: Int? = null,
	val windDirection: String? = null,
	val windSpeed: Float? = null,
	val pressure: Int? = null,
)

