package pl.szczodrzynski.tracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.szczodrzynski.tracker.data.db.dao.AthleteDao
import pl.szczodrzynski.tracker.data.db.dao.TrainingCommentDao
import pl.szczodrzynski.tracker.data.db.dao.TrainingDao
import pl.szczodrzynski.tracker.data.db.dao.TrainingRunDao
import pl.szczodrzynski.tracker.data.db.dao.TrainingRunSplitDao
import pl.szczodrzynski.tracker.data.db.dao.TrainingWeatherDao
import pl.szczodrzynski.tracker.data.entity.Athlete
import pl.szczodrzynski.tracker.data.entity.Training
import pl.szczodrzynski.tracker.data.entity.TrainingComment
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.TrainingRunSplit
import pl.szczodrzynski.tracker.data.entity.TrainingWeather
import pl.szczodrzynski.tracker.data.entity.serializer.InstantConverter

@Database(
	entities = [
		Athlete::class,
		Training::class,
		TrainingComment::class,
		TrainingRun::class,
		TrainingRunSplit::class,
		TrainingWeather::class,
	],
	version = 1,
)
@TypeConverters(
	InstantConverter::class,
)
abstract class AppDb : RoomDatabase() {

	abstract val athleteDao: AthleteDao
	abstract val trainingDao: TrainingDao
	abstract val trainingCommentDao: TrainingCommentDao
	abstract val trainingRunDao: TrainingRunDao
	abstract val trainingRunSplitDao: TrainingRunSplitDao
	abstract val trainingWeatherDao: TrainingWeatherDao
}
