package pl.szczodrzynski.tracker.data.entity.joins

import androidx.room.Embedded
import androidx.room.Relation
import pl.szczodrzynski.tracker.data.entity.Training
import pl.szczodrzynski.tracker.data.entity.TrainingComment
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.TrainingWeather
import java.time.Instant

data class TrainingFull(
	@Embedded
	val training: Training,

	@Relation(
		entity = TrainingRun::class,
		parentColumn = "id",
		entityColumn = "trainingId",
	)
	val runList: List<TrainingRunFull>,

	@Relation(
		parentColumn = "id",
		entityColumn = "trainingId",
	)
	val commentList: List<TrainingComment>,

	@Relation(
		parentColumn = "id",
		entityColumn = "trainingId",
	)
	val weatherList: List<TrainingWeather>,
) {

	fun getTimeline(): List<Pair<Instant, Any>> =
		runList.filter { it.splits.isNotEmpty() && it.run.isFinished }.map { it.run.dateTime to it } +
			commentList.map { it.dateTime to it } +
			weatherList.map { it.dateTime to it }
}
