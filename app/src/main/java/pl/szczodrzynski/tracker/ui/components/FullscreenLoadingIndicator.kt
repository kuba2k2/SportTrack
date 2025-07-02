package pl.szczodrzynski.tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun FullscreenLoadingIndicator() {
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		LoadingIndicator()
	}
}
