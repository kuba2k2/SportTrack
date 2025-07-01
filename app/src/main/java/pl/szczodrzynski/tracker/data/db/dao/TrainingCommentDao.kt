package pl.szczodrzynski.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import pl.szczodrzynski.tracker.data.entity.TrainingComment

@Dao
interface TrainingCommentDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(trainingComment: TrainingComment)

	@Update
	suspend fun update(trainingComment: TrainingComment)

	@Delete
	suspend fun delete(trainingComment: TrainingComment)
}
