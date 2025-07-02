package pl.szczodrzynski.tracker.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Composable
fun SportTrackPreview(content: @Composable () -> Unit) {
	SportTrackTheme {
		Box(modifier = Modifier.fillMaxSize()) {
			content()
		}
	}
}
