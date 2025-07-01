package pl.szczodrzynski.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import pl.szczodrzynski.tracker.data.entity.TrainingRun

@Dao
interface TrainingRunDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(trainingRun: TrainingRun)

	@Update
	suspend fun update(trainingRun: TrainingRun)

	@Delete
	suspend fun delete(trainingRun: TrainingRun)
}
