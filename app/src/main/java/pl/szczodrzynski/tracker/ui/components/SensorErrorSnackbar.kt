package pl.szczodrzynski.tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.service.data.TrackerConfig
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		SensorErrorSnackbar(TrackerConfig(error = true))
	}
}

@Composable
fun SensorErrorSnackbar(
	trackerConfig: TrackerConfig,
	modifier: Modifier = Modifier,
) {
	AnimatedVisibility(visible = trackerConfig.error || LocalInspectionMode.current) {
		Snackbar(
			modifier = modifier.padding(12.dp),
			containerColor = MaterialTheme.colorScheme.error,
			contentColor = MaterialTheme.colorScheme.onError,
			content = {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Iconics(CommunityMaterial.Icon.cmd_alert_outline)
					Text(stringResource(R.string.training_sensor_error), modifier = Modifier.padding(start = 8.dp))
				}
			},
		)
	}
}
