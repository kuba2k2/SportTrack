package pl.szczodrzynski.tracker.data.entity.joins

import androidx.room.Embedded
import androidx.room.Relation
import pl.szczodrzynski.tracker.data.entity.Athlete
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.TrainingRunSplit

data class TrainingRunFull(
	@Embedded
	val run: TrainingRun,

	@Relation(
		parentColumn = "id",
		entityColumn = "trainingRunId",
	)
	val splits: List<TrainingRunSplit>?,

	@Relation(
		parentColumn = "athleteId",
		entityColumn = "id",
	)
	val athlete: Athlete?,
)
