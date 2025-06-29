package pl.szczodrzynski.tracker.ui.screen.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Composable
@Preview
private fun Preview() {
	SportTrackTheme {
		LogoHeader()
	}
}

@Composable
fun LogoHeader(modifier: Modifier = Modifier) {
	Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
		Image(
			CommunityMaterial.Icon3.cmd_run_fast,
			modifier = Modifier.size(96.dp),
			colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
		)
		Text(
			text = stringResource(R.string.app_name),
			modifier = Modifier.padding(top = 24.dp),
			style = MaterialTheme.typography.headlineMedium,
		)
	}
}
