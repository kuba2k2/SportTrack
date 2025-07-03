package pl.szczodrzynski.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pl.szczodrzynski.tracker.data.entity.TrainingRunSplit

@Dao
interface TrainingRunSplitDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(trainingRunSplit: TrainingRunSplit)

	@Update
	suspend fun update(trainingRunSplit: TrainingRunSplit)

	@Delete
	suspend fun delete(trainingRunSplit: TrainingRunSplit)

	@Query("DELETE FROM trainingRunSplit WHERE trainingRunId = :trainingRunId")
	suspend fun deleteAll(trainingRunId: Int)
}
