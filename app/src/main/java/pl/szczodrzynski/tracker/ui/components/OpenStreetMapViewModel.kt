package pl.szczodrzynski.tracker.ui.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.scrollTo
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import pl.szczodrzynski.tracker.data.network.executeAsync
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.sin

@HiltViewModel
class OpenStreetMapViewModel @Inject constructor(
	okHttp: OkHttpClient,
	okHttpCache: Cache,
) : ViewModel() {

	private val okHttpCached = okHttp.newBuilder()
		.cache(okHttpCache)
		.build()

	private val tileStreamProvider = TileStreamProvider { row, col, zoom ->
		try {
			Timber.d("Requesting tile $row,$col at zoom $zoom")
			val request = Request.Builder()
				.url("https://tile.openstreetmap.org/$zoom/$col/$row.png")
				.build()
			val call = okHttpCached.newCall(request)
			val response = call.executeAsync()
			Timber.d("Response: $response")
			response.body?.byteStream()
		} catch (e: Exception) {
			Timber.w(e, "Failed to request tile $col,$row")
			null
		}
	}

	val mapState = MapState(
		levelCount = 19,
		fullWidth = 262_144 * 256,
		fullHeight = 262_144 * 256,
		workerCount = 4,
		initialValuesBuilder = {},
	).apply {
		addLayer(tileStreamProvider)
	}

	suspend fun scrollToCoordinates(lat: Double, lon: Double, scale: Double) {
		val clampedLat = lat.coerceIn(-85.05112878, 85.05112878)

		val x = (lon + 180.0) / 360.0
		val sinLat = sin(clampedLat * PI / 180.0)
		val y = 0.5 - ln((1 + sinLat) / (1 - sinLat)) / (4 * PI)

		mapState.scrollTo(x, y, scale)
	}
}
