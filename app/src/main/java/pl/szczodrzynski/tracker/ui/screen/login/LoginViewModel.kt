package pl.szczodrzynski.tracker.ui.screen.login

import android.content.Context
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.szczodrzynski.tracker.R
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel(), OnCompleteListener<AuthResult> {
	companion object {
		private const val TAG = "LoginViewModel"
	}

	sealed interface State {
		data object Idle : State
		data object Loading : State
		data class Error(val e: Exception?, val messageRes: Int? = null) : State
		data class Success(val user: FirebaseUser) : State
	}

	private val _state = MutableStateFlow<State>(State.Idle)
	val state = _state.asStateFlow()

	private val auth by lazy { Firebase.auth }

	val nameState = TextFieldState()
	val emailState = TextFieldState()
	val passwordState = TextFieldState()

	init {
		auth.currentUser?.let { user ->
			_state.update { State.Success(user) }
		}
	}

	fun performLoginWithEmail() = viewModelScope.launch {
		_state.update { State.Loading }
		try {
			auth.signInWithEmailAndPassword(
				emailState.text.toString(),
				passwordState.text.toString(),
			).addOnCompleteListener(this@LoginViewModel)
		} catch (e: FirebaseAuthException) {
			_state.update { getError(e) }
		}
	}

	fun performRegisterWithEmail() = viewModelScope.launch {
		_state.update { State.Loading }
		try {
			auth.createUserWithEmailAndPassword(
				emailState.text.toString(),
				passwordState.text.toString(),
			).addOnCompleteListener(this@LoginViewModel)
		} catch (e: FirebaseAuthException) {
			_state.update { getError(e) }
		}
	}

	fun performLoginWithGoogle(context: Context) = viewModelScope.launch {
		_state.update { State.Loading }
		val serverClientId = "316943619596-ju2qgln58e5dnoqp0lpoae4mrkq3sgu9.apps.googleusercontent.com"
		val googleIdOption = GetGoogleIdOption.Builder()
			.setServerClientId(serverClientId)
			.setFilterByAuthorizedAccounts(false)
			.build()
		val credentialRequest = GetCredentialRequest.Builder()
			.addCredentialOption(googleIdOption)
			.build()
		val credentialManager = CredentialManager.create(context.applicationContext)

//		credentialManager.clearCredentialState(ClearCredentialStateRequest())

		val credentialResponse = try {
			credentialManager.getCredential(context, credentialRequest)
		} catch (e: GetCredentialException) {
			_state.update { getError(e) }
			return@launch
		}

		val credential = credentialResponse.credential
		if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
			_state.update { State.Error(null, R.string.login_error_unknown) }
			return@launch
		}

		val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
		val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)

		try {
			auth.signInWithCredential(authCredential).addOnCompleteListener(this@LoginViewModel)
		} catch (e: FirebaseAuthException) {
			_state.update { getError(e) }
		}
	}

	override fun onComplete(task: Task<AuthResult>) {
		val user = auth.currentUser
		if (!task.isSuccessful || user == null) {
			Log.d(TAG, "Sign in failed: ${task.exception}")
			_state.update { getError(task.exception) }
			return
		}

		if (nameState.text.isNotBlank()) {
			// after registration completes, set the display name
			val request = UserProfileChangeRequest.Builder()
				.setDisplayName(nameState.text.toString())
				.build()
			user.updateProfile(request).addOnCompleteListener {
				Log.d(TAG, "Sign in successful")
				_state.update { State.Success(user) }
			}
		} else {
			Log.d(TAG, "Sign in successful")
			_state.update { State.Success(user) }
		}
	}

	private fun getError(e: Exception?): State.Error {
		e?.printStackTrace()
		return when (e) {
			null -> State.Error(null, R.string.login_error_unknown)
			is GetCredentialException -> State.Error(e, R.string.login_error_credential_manager)
			is FirebaseAuthInvalidCredentialsException -> State.Error(e, R.string.login_error_invalid_credentials)
			is FirebaseAuthInvalidUserException -> State.Error(e, R.string.login_error_invalid_user)
			is FirebaseAuthUserCollisionException -> State.Error(e, R.string.login_error_user_collision)
			else -> State.Error(e)
		}
	}
}
