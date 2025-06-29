package pl.szczodrzynski.tracker.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Preview
@Composable
private fun Preview() {
	SportTrackTheme {
		HomeScreen()
	}
}

@Composable
fun HomeScreen() {
	val mainVm = LocalMainViewModel.current

	Column(
		modifier = Modifier.padding(horizontal = 16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(stringResource(R.string.app_name))
		Text("User: ${mainVm.currentUser?.displayName}")
		Button(
			onClick = {
				Firebase.auth.signOut()
			},
			enabled = mainVm.currentUser != null,
		) {
			Text("Sign out")
		}
		Button(
			onClick = {
				mainVm.navigate(NavTarget.Login)
			},
			enabled = mainVm.currentUser == null,
		) {
			Text("Sign in")
		}
	}
}
