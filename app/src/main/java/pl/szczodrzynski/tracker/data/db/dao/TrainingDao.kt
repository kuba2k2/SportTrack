package pl.szczodrzynski.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.szczodrzynski.tracker.data.entity.Training
import pl.szczodrzynski.tracker.data.entity.joins.TrainingFull

@Dao
interface TrainingDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(training: Training)

	@Update
	suspend fun update(training: Training)

	@Delete
	suspend fun delete(training: Training)

	@Query("SELECT * FROM training")
	fun getAll(): Flow<List<Training>>

	@Query("SELECT * FROM training ORDER BY dateTime DESC")
	fun getLatest(): Flow<List<Training>>

	@Transaction
	@Query("SELECT * from training WHERE id = :id")
	fun getOneFull(id: Int): Flow<TrainingFull?>
}
