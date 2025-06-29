package pl.szczodrzynski.tracker.ui.screen.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Composable
@Preview
private fun Preview() {
	SportTrackTheme {
		AccountInfo(name = "User Name", email = "user@example.com")
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun AccountInfo(
	name: String,
	email: String,
	modifier: Modifier = Modifier,
) {
	val mainVm = LocalMainViewModel.current

	Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
		Text(
			text = stringResource(R.string.login_success_title, name),
			style = MaterialTheme.typography.titleMedium,
		)
		Text(
			text = email,
			style = MaterialTheme.typography.titleSmall,
		)

		Button(
			onClick = {
				mainVm.navigate(NavTarget.Home)
			},
			shapes = ButtonDefaults.shapes(),
			modifier = Modifier.padding(top = 16.dp),
		) {
			Text(stringResource(R.string.login_success_button))
		}
	}
}
