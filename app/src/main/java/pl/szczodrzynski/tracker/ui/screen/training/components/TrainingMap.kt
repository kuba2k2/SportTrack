package pl.szczodrzynski.tracker.ui.screen.training.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.data.entity.Training
import pl.szczodrzynski.tracker.ui.components.OpenStreetMap
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		TrainingMap(training = Training(title = ""), isLoading = true)
	}
}

@Preview
@Composable
private fun PreviewNoLocation() {
	SportTrackPreview {
		TrainingMap(training = Training(title = ""), isLoading = false)
	}
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNight() {
	SportTrackPreview {
		TrainingMap(training = Training(title = ""), isLoading = true)
	}
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightNoLocation() {
	SportTrackPreview {
		TrainingMap(training = Training(title = ""), isLoading = false)
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun TrainingMap(
	training: Training,
	isLoading: Boolean,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.clip(RoundedCornerShape(16.dp))
			.background(MaterialTheme.colorScheme.surfaceContainerHigh),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		when {
			training.locationLat != null && training.locationLon != null -> {
				OpenStreetMap(
					lat = training.locationLat,
					lon = training.locationLon,
					scale = 1.0,
				)
			}

			isLoading -> {
				LoadingIndicator()
			}

			else -> {
				Image(
					CommunityMaterial.Icon3.cmd_map_marker_off_outline,
					modifier = Modifier
						.size(64.dp)
						.graphicsLayer {
							alpha = 0.5f
						},
					colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
				)
				Text(
					stringResource(R.string.training_no_location),
					modifier = Modifier
						.padding(top = 16.dp)
						.graphicsLayer {
							alpha = 0.5f
						},
					style = MaterialTheme.typography.titleMedium,
				)
			}
		}
	}
}
