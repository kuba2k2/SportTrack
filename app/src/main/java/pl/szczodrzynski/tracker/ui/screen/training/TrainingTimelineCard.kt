package pl.szczodrzynski.tracker.ui.screen.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.data.entity.TrainingComment
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.TrainingRunSplit
import pl.szczodrzynski.tracker.data.entity.TrainingWeather
import pl.szczodrzynski.tracker.data.entity.joins.TrainingRunFull
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.components.roundTime
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		val timeline = listOf(
			TrainingRunFull(
				run = TrainingRun(
					trainingId = 0,
					title = "",
					totalDistance = 0,
					sensorDistance = listOf(),
					isFlyingTest = true,
				),
				splits = listOf(
					TrainingRunSplit(trainingRunId = 0, timestamp = 10000),
					TrainingRunSplit(trainingRunId = 0, timestamp = 11234),
					TrainingRunSplit(trainingRunId = 0, timestamp = 12678),
					TrainingRunSplit(trainingRunId = 0, timestamp = 14096),
					TrainingRunSplit(trainingRunId = 0, timestamp = 15800),
				),
				athlete = null,
			),
			TrainingComment(
				trainingId = 0,
				comment = "This is a comment",
			),
			TrainingWeather(
				trainingId = 0,
				weather = "Sunny",
				localTemperature = 27.0f,
				temperature = 25.3f,
				apparentTemperature = 29.7f,
				humidity = 50,
				windDirection = "NE",
				windSpeed = 4.4f,
				pressure = 1021,
			)
		)
		LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
			items(timeline) { item ->
				TrainingTimelineCard(item)
			}
		}
	}
}

@Composable
fun TrainingTimelineCard(
	item: Any,
	onClick: () -> Unit = {},
) {
	Card(
		onClick = onClick,
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
	) {
		Column(
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
		) {
			when (item) {
				is TrainingRunFull -> TrainingRunCard(item)
				is TrainingComment -> TrainingCommentCard(item)
			}
		}
	}
}

@Composable
private fun TrainingRunCard(
	trainingRunFull: TrainingRunFull,
) {
	val localDateTime = trainingRunFull.run.dateTime.atZone(ZoneId.systemDefault())

	Row(verticalAlignment = Alignment.CenterVertically) {
		Iconics(
			if (trainingRunFull.run.isFlyingTest)
				CommunityMaterial.Icon.cmd_clock_end
			else
				CommunityMaterial.Icon.cmd_bullhorn_outline,
			size = 24.dp,
		)
		Text(
			if (trainingRunFull.run.isFlyingTest)
				stringResource(R.string.training_config_flying_start)
			else
				stringResource(R.string.training_config_start_on_signal),
			modifier = Modifier
				.weight(1.0f)
				.padding(start = 4.dp),
			style = MaterialTheme.typography.titleLarge,
		)
		Text(
			localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
	}
	Row(verticalAlignment = Alignment.CenterVertically) {
		var splitCount = trainingRunFull.splits?.count() ?: 1
		if (trainingRunFull.run.isFlyingTest)
			splitCount--
		Iconics(CommunityMaterial.Icon3.cmd_timer_outline, size = 16.dp)
		Text(
			stringResource(
				R.string.training_timeline_result_count,
				splitCount,
				trainingRunFull.getTotalTime().toFloat().roundTime(),
			),
			modifier = Modifier
				.padding(start = 4.dp)
				.weight(1.0f),
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 1,
		)
	}
	Row(verticalAlignment = Alignment.CenterVertically) {
		Iconics(CommunityMaterial.Icon3.cmd_text, size = 16.dp)
		Text(
			trainingRunFull.run.description ?: stringResource(R.string.history_no_description),
			modifier = Modifier
				.padding(start = 4.dp)
				.weight(1.0f),
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 3,
		)
	}
}

@Composable
private fun TrainingCommentCard(
	trainingComment: TrainingComment,
) {
	val localDateTime = trainingComment.dateTime.atZone(ZoneId.systemDefault())

	Row(verticalAlignment = Alignment.CenterVertically) {
		Iconics(CommunityMaterial.Icon3.cmd_text, size = 16.dp)
		Text(
			trainingComment.comment,
			modifier = Modifier
				.padding(start = 4.dp)
				.weight(1.0f),
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
		Text(
			localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
	}
}
