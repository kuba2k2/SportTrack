package pl.szczodrzynski.tracker.ui.screen.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.screen.login.components.AccountHeader
import pl.szczodrzynski.tracker.ui.screen.login.components.AccountInfo
import pl.szczodrzynski.tracker.ui.screen.login.components.CredentialsForm
import pl.szczodrzynski.tracker.ui.screen.login.components.LogoHeader
import pl.szczodrzynski.tracker.ui.screen.login.components.SocialLoginButtons
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Preview
@Composable
private fun PreviewLogin() {
	SportTrackTheme {
		LoginScreen()
	}
}

@Preview
@Composable
private fun PreviewRegister() {
	SportTrackTheme {
		LoginScreen(isRegister = true)
	}
}

@Preview
@Composable
private fun PreviewProfile() {
	SportTrackTheme {
		LoginScreen(isProfile = true)
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LoginScreen(
	isRegister: Boolean = false,
	isProfile: Boolean = false,
	vm: LoginViewModel = hiltViewModel(),
) {
	val mainVm = LocalMainViewModel.current
	val context = LocalContext.current
	val scrollState = rememberScrollState()
	val state by vm.state.collectAsStateWithLifecycle()
	val syncState by mainVm.sync.state.collectAsStateWithLifecycle()

	val enabled = state !is LoginViewModel.State.Loading
	val user = (state as? LoginViewModel.State.Success)?.user

	Column(
		modifier = Modifier
			.verticalScroll(scrollState)
			.padding(horizontal = 16.dp)
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		val headerModifier = Modifier
			.padding(top = 72.dp, bottom = 24.dp)
			.fillMaxWidth()
		if (isProfile && user != null)
			AccountHeader(modifier = headerModifier)
		else
			LogoHeader(modifier = headerModifier)

		if (user != null)
			AccountInfo(
				name = user.displayName ?: "",
				email = user.email ?: "",
				syncState = syncState,
				onUploadSync = {
					mainVm.sync.startSyncUpload()
				},
				onDownloadSync = {
					mainVm.sync.startSyncDownload()
				},
			)

		if (isProfile || user != null) {
			val size = ButtonDefaults.MediumContainerHeight
			Button(
				onClick = {
					when {
						isProfile && user != null -> vm.performLogout()
						isProfile -> mainVm.navigate(NavTarget.Login)
						else -> mainVm.navigate(NavTarget.Home)
					}
				},
				shapes = ButtonDefaults.shapesFor(size),
				modifier = Modifier
					.padding(top = 16.dp)
					.height(size),
				contentPadding = ButtonDefaults.contentPaddingFor(size),
			) {
				val style = ButtonDefaults.textStyleFor(size)
				Text(
					stringResource(
						when {
							isProfile && user != null -> R.string.login_logout_button
							isProfile -> R.string.login_title
							else -> R.string.login_success_button
						}
					),
					style = style,
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
			Iconics(CommunityMaterial.Icon.cmd_account_plus_outline, size = ButtonDefaults.IconSize)
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
			Iconics(CommunityMaterial.Icon.cmd_account_off_outline, size = ButtonDefaults.IconSize)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(stringResource(R.string.login_guest_button))
		}
	}
}
