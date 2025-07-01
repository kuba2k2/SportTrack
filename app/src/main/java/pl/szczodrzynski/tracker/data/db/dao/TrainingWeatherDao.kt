package pl.szczodrzynski.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import pl.szczodrzynski.tracker.data.entity.TrainingWeather

@Dao
interface TrainingWeatherDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(trainingWeather: TrainingWeather)

	@Update
	suspend fun update(trainingWeather: TrainingWeather)

	@Delete
	suspend fun delete(trainingWeather: TrainingWeather)
}
