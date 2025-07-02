package pl.szczodrzynski.tracker.ui.screen.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import pl.szczodrzynski.tracker.ui.components.Iconics
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
		Iconics(
			icon = CommunityMaterial.Icon3.cmd_run_fast,
			size = 96.dp,
			color = MaterialTheme.colorScheme.tertiary,
		)
		Text(
			text = stringResource(R.string.app_name),
			modifier = Modifier.padding(top = 24.dp),
			style = MaterialTheme.typography.headlineMedium,
		)
	}
}
