package pl.szczodrzynski.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.joins.TrainingRunFull

@Dao
interface TrainingRunDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(trainingRun: TrainingRun): Long

	@Update
	suspend fun update(trainingRun: TrainingRun)

	@Delete
	suspend fun delete(trainingRun: TrainingRun)

	@Query("UPDATE trainingRun SET isFinished = 1")
	suspend fun setAllFinished()

	@Query("SELECT * FROM trainingRun WHERE isFinished = 0 ORDER BY dateTime DESC LIMIT 1")
	fun getCurrentFull(): Flow<TrainingRunFull?>

	@Query("DELETE FROM trainingRun WHERE NOT EXISTS (SELECT 1 FROM trainingRunSplit WHERE trainingRunSplit.trainingRunId = trainingRun.id)")
	suspend fun cleanupEmpty()
}
