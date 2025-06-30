package pl.szczodrzynski.tracker.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import pl.szczodrzynski.tracker.service.TrackerService
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	private val mainVm: MainViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)
		setContent {
			SportTrackTheme {
				MainRoot(isPreview = false, mainVm = mainVm)
			}
		}
	}

	override fun onStart() {
		super.onStart()
		startService(Intent(this, TrackerService::class.java))
		bindService(Intent(this, TrackerService::class.java), mainVm, Context.BIND_AUTO_CREATE)
	}

	override fun onStop() {
		super.onStop()
		unbindService(mainVm)
	}
}

