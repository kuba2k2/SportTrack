package pl.szczodrzynski.tracker.ui.screen.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Composable
@Preview
private fun Preview() {
	SportTrackTheme {
		AccountHeader()
	}
}

@Composable
fun AccountHeader(modifier: Modifier = Modifier) {
	val mainVm = LocalMainViewModel.current
	val user = mainVm.currentUser
	val photoUrl = user?.photoUrl

	Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
		if (photoUrl == null)
			Iconics(
				icon = CommunityMaterial.Icon.cmd_account_circle_outline,
				size = 96.dp,
				color = MaterialTheme.colorScheme.tertiary,
			)
		else
			AsyncImage(
				photoUrl.toString(),
				contentDescription = user.displayName,
				modifier = Modifier
					.size(96.dp)
					.clip(CircleShape),
			)
	}
}
