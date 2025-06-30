package pl.szczodrzynski.tracker.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Composable
fun SportTrackPreview(content: @Composable () -> Unit) {
	SportTrackTheme {
		val mainVm: MainViewModel = viewModel()
		CompositionLocalProvider(LocalMainViewModel provides mainVm, content = content)
	}
}
