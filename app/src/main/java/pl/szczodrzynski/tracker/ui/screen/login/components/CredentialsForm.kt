package pl.szczodrzynski.tracker.ui.screen.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
		Column(
			modifier = Modifier.padding(horizontal = 16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			CredentialsForm()
		}
	}
}

@Composable
internal fun CredentialsForm(
	nameState: TextFieldState? = null,
	emailState: TextFieldState = TextFieldState(),
	passwordState: TextFieldState = TextFieldState(),
	enabled: Boolean = true,
	error: String? = null,
) {
	val textFieldModifier = Modifier
		.padding(top = 8.dp)
		.fillMaxWidth()

	if (nameState != null) {
		OutlinedTextField(
			state = nameState,
			modifier = textFieldModifier,
			enabled = enabled,
			label = { Text(stringResource(R.string.login_name_label)) },
			leadingIcon = {
				Iconics(CommunityMaterial.Icon.cmd_account_outline)
			},
		)
	}

	OutlinedTextField(
		state = emailState,
		modifier = textFieldModifier,
		enabled = enabled,
		label = { Text(stringResource(R.string.login_email_label)) },
		leadingIcon = {
			Iconics(CommunityMaterial.Icon.cmd_at)
		},
		supportingText = { if (error != null) Text(error) },
		isError = error != null,
	)

	var showPassword by rememberSaveable { mutableStateOf(false) }
	OutlinedSecureTextField(
		state = passwordState,
		modifier = textFieldModifier,
		enabled = enabled,
		label = { Text(stringResource(R.string.login_password_label)) },
		leadingIcon = {
			Iconics(CommunityMaterial.Icon2.cmd_lock_outline)
		},
		trailingIcon = {
			IconButton(onClick = {
				showPassword = !showPassword
			}) {
				Iconics(
					icon = if (showPassword)
						CommunityMaterial.Icon.cmd_eye_off_outline
					else
						CommunityMaterial.Icon.cmd_eye_outline,
					contentDescription = if (showPassword)
						stringResource(R.string.login_hide_password)
					else
						stringResource(R.string.login_show_password),
				)
			}
		},
		textObfuscationMode = if (showPassword)
			TextObfuscationMode.Visible
		else
			TextObfuscationMode.RevealLastTyped,
	)
}
