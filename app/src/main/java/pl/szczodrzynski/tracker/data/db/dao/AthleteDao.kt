package pl.szczodrzynski.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.szczodrzynski.tracker.data.entity.Athlete

@Dao
interface AthleteDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(athlete: Athlete)

	@Update
	suspend fun update(athlete: Athlete)

	@Delete
	suspend fun delete(athlete: Athlete)

	@Query("SELECT * FROM athlete")
	fun getAll(): Flow<List<Athlete>>
}
