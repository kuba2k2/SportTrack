package pl.szczodrzynski.tracker.ui.screen.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Composable
@Preview
private fun Preview() {
	SportTrackTheme {
		AccountInfo(name = "User Name", email = "user@example.com")
	}
}

@Composable
internal fun AccountInfo(
	name: String,
	email: String,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
		Text(
			text = stringResource(R.string.login_success_title, name),
			style = MaterialTheme.typography.titleMedium,
		)
		Text(
			text = email,
			style = MaterialTheme.typography.titleSmall,
		)
	}
}
