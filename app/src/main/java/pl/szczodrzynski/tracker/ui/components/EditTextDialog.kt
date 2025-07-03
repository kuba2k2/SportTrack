package pl.szczodrzynski.tracker.ui.components

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.szczodrzynski.tracker.R

@Composable
fun EditTextDialog(
	initialText: String,
	onDismiss: (value: String?) -> Unit,
	title: String,
) {
	val state = rememberTextFieldState(initialText)

	AlertDialog(
		onDismissRequest = {
			onDismiss(null)
		},
		confirmButton = {
			TextButton(
				onClick = {
					onDismiss(state.text.toString())
				}
			) {
				Text(stringResource(R.string.training_config_save))
			}
		},
		dismissButton = {
			TextButton(
				onClick = {
					onDismiss(null)
				},
			) {
				Text(stringResource(R.string.training_config_cancel))
			}
		},
		title = {
			Text(title)
		},
		text = {
			TextField(
				state = state,
			)
		},
	)
}
