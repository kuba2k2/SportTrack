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
	val dateTime: Instant,

	val weather: String?,
	val precipitation: String?,

	val localTemperature: Float?,
	val temperature: Float?,
	val humidity: Int?,
	val windDirection: String?,
	val windSpeed: Int?,
	val pressure: Int?,

	val realFeelTemperature: Float?,
	val realFeelTemperatureShade: Float?,
)

