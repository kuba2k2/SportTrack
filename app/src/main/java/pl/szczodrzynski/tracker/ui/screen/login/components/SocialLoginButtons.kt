package pl.szczodrzynski.tracker.ui.screen.login.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Preview
@Composable
private fun Preview() {
	SportTrackTheme {
		SocialLoginButtons(onGoogleClick = {}, onFacebookClick = {})
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun SocialLoginButtons(
	onGoogleClick: () -> Unit,
	onFacebookClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	FlowRow(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(
			ButtonGroupDefaults.ConnectedSpaceBetween,
			alignment = Alignment.CenterHorizontally,
		),
	) {
		FilledTonalButton(
			onClick = onGoogleClick,
			shapes = ButtonShapes(
				ButtonGroupDefaults.connectedLeadingButtonShape,
				ButtonGroupDefaults.connectedLeadingButtonPressShape,
			),
			enabled = enabled,
		) {
			Iconics(CommunityMaterial.Icon2.cmd_google, size = ButtonDefaults.IconSize)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(stringResource(R.string.login_google_button))
		}
		FilledTonalButton(
			onClick = onFacebookClick,
			shapes = ButtonShapes(
				ButtonGroupDefaults.connectedTrailingButtonShape,
				ButtonGroupDefaults.connectedTrailingButtonPressShape,
			),
			enabled = false,
		) {
			Iconics(CommunityMaterial.Icon2.cmd_facebook, size = ButtonDefaults.IconSize)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(stringResource(R.string.login_facebook_button))
		}
	}
}
