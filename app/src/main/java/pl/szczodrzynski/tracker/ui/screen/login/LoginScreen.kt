package pl.szczodrzynski.tracker.ui.screen.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.screen.login.components.AccountInfo
import pl.szczodrzynski.tracker.ui.screen.login.components.CredentialsForm
import pl.szczodrzynski.tracker.ui.screen.login.components.LogoHeader
import pl.szczodrzynski.tracker.ui.screen.login.components.SocialLoginButtons
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Preview
@Composable
private fun PreviewLogin() {
	SportTrackTheme {
		LoginScreen(isRegister = false)
	}
}

@Preview
@Composable
private fun PreviewRegister() {
	SportTrackTheme {
		LoginScreen(isRegister = true)
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LoginScreen(
	isRegister: Boolean,
	vm: LoginViewModel = hiltViewModel(),
) {
	val mainVm = LocalMainViewModel.current
	val context = LocalContext.current
	val state by vm.state.collectAsStateWithLifecycle()

	val enabled = state !is LoginViewModel.State.Loading

	Column(
		modifier = Modifier.padding(horizontal = 16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		LogoHeader(
			modifier = Modifier
				.padding(top = 72.dp, bottom = 24.dp)
				.fillMaxWidth(),
		)

		if (state is LoginViewModel.State.Success) {
			(state as? LoginViewModel.State.Success)?.user?.let {
				AccountInfo(
					name = it.displayName ?: "",
					email = it.email ?: "",
				)
			}
			return@Column
		}

		Text(
			text = if (isRegister)
				stringResource(R.string.login_register_title_long)
			else
				stringResource(R.string.login_title_long),
			style = MaterialTheme.typography.titleMedium,
		)

		val loginError = state as? LoginViewModel.State.Error
		CredentialsForm(
			nameState = if (isRegister) vm.nameState else null,
			emailState = vm.emailState,
			passwordState = vm.passwordState,
			enabled = enabled,
			error = loginError?.messageRes?.let { stringResource(it) } ?: loginError?.e?.toString(),
		)

		val buttonHeight = ButtonDefaults.MediumContainerHeight
		val credentialsEntered = (!isRegister || vm.nameState.text.isNotBlank())
			&& vm.emailState.text.isNotBlank()
			&& vm.passwordState.text.isNotBlank()
		Button(
			onClick = {
				if (isRegister)
					vm.performRegisterWithEmail()
				else
					vm.performLoginWithEmail()
			},
			shapes = ButtonDefaults.shapes(),
			modifier = Modifier
				.heightIn(buttonHeight)
				.padding(top = 24.dp),
			enabled = enabled && credentialsEntered,
			contentPadding = ButtonDefaults.contentPaddingFor(buttonHeight),
		) {
			Text(
				if (isRegister)
					stringResource(R.string.register_button)
				else
					stringResource(R.string.login_button),
				style = ButtonDefaults.textStyleFor(buttonHeight)
			)
		}

		SocialLoginButtons(
			onGoogleClick = {
				vm.performLoginWithGoogle(context)
			},
			onFacebookClick = {},
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 16.dp),
			enabled = enabled,
		)

		if (isRegister)
			return@Column

		FilledTonalButton(
			onClick = {
				mainVm.navigate(NavTarget.Register)
			},
			shapes = ButtonDefaults.shapes(),
			enabled = enabled,
		) {
			Image(
				CommunityMaterial.Icon.cmd_account_plus_outline,
				modifier = Modifier.size(ButtonDefaults.IconSize),
				colorFilter = ColorFilter.tint(LocalContentColor.current),
			)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(stringResource(R.string.login_sign_up_button))
		}

		TextButton(
			modifier = Modifier.padding(top = 16.dp),
			onClick = {
				mainVm.navigate(NavTarget.Home)
			},
			shapes = ButtonDefaults.shapes(),
			enabled = enabled,
		) {
			Image(
				CommunityMaterial.Icon.cmd_account_off_outline,
				modifier = Modifier.size(ButtonDefaults.IconSize),
				colorFilter = ColorFilter.tint(LocalContentColor.current),
			)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(stringResource(R.string.login_guest_button))
		}
	}
}
