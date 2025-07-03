package pl.szczodrzynski.tracker.ui.screen.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
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
import pl.szczodrzynski.tracker.ui.components.IconTextRow
import pl.szczodrzynski.tracker.ui.components.MultiIconTextRow
import pl.szczodrzynski.tracker.ui.components.TitleIconTextRow
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
				is TrainingWeather -> TrainingWeatherCard(item)
			}
		}
	}
}

@Composable
private fun TrainingRunCard(
	trainingRunFull: TrainingRunFull,
) {
	val localDateTime = trainingRunFull.run.dateTime.atZone(ZoneId.systemDefault())

	TitleIconTextRow(
		icon = if (trainingRunFull.run.isFlyingTest)
			CommunityMaterial.Icon.cmd_clock_end
		else
			CommunityMaterial.Icon.cmd_bullhorn_outline,
		text = if (trainingRunFull.run.isFlyingTest)
			stringResource(R.string.training_config_flying_start)
		else
			stringResource(R.string.training_config_start_on_signal),
		extraText = localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
	)

	var splitCount = trainingRunFull.splits.count()
	if (trainingRunFull.run.isFlyingTest)
		splitCount--
	IconTextRow(
		icon = CommunityMaterial.Icon3.cmd_timer_outline,
		text = stringResource(
			R.string.training_timeline_result_count,
			splitCount,
			trainingRunFull.getTotalTime().toFloat().roundTime(),
		),
	)

	IconTextRow(
		icon = CommunityMaterial.Icon3.cmd_text,
		text = trainingRunFull.run.description ?: stringResource(R.string.history_no_description),
		maxLines = 2,
	)
}

@Composable
private fun TrainingCommentCard(
	trainingComment: TrainingComment,
) {
	val localDateTime = trainingComment.dateTime.atZone(ZoneId.systemDefault())

	TitleIconTextRow(
		icon = CommunityMaterial.Icon3.cmd_text,
		text = trainingComment.comment,
		extraText = localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
		maxLines = 2,
	)
}

@Composable
private fun TrainingWeatherCard(
	trainingWeather: TrainingWeather,
) {
	val localDateTime = trainingWeather.dateTime.atZone(ZoneId.systemDefault())

	TitleIconTextRow(
		icon = CommunityMaterial.Icon3.cmd_weather_partly_cloudy,
		text = listOfNotNull(
			trainingWeather.weather,
			trainingWeather.temperature?.let { stringResource(R.string.weather_temperature_format, it) },
		).joinToString(),
	)

	IconTextRow(
		icon = CommunityMaterial.Icon3.cmd_thermometer,
		text = trainingWeather.apparentTemperature?.let {
			stringResource(R.string.weather_temperature_apparent_format, it)
		},
		extraText = localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
	)

	MultiIconTextRow(
		CommunityMaterial.Icon3.cmd_weather_windy to trainingWeather.windSpeed?.let {
			"${trainingWeather.windSpeed} km/h ${trainingWeather.windDirection}"
		},
		CommunityMaterial.Icon.cmd_arrow_collapse to trainingWeather.pressure?.let {
			"${trainingWeather.pressure} hPa"
		},
	)

	MultiIconTextRow(
		CommunityMaterial.Icon3.cmd_weather_pouring to trainingWeather.precipitation,
		CommunityMaterial.Icon.cmd_cloud_percent_outline to trainingWeather.humidity?.let {
			"${trainingWeather.humidity}%"
		},
	)
}
