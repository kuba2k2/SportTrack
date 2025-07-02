package pl.szczodrzynski.tracker.data.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okio.IOException
import kotlin.coroutines.resumeWithException

suspend fun Call.executeAsync(): Response =
	suspendCancellableCoroutine { continuation ->
		continuation.invokeOnCancellation {
			this.cancel()
		}
		this.enqueue(
			object : Callback {
				override fun onFailure(
					call: Call,
					e: IOException,
				) {
					continuation.resumeWithException(e)
				}

				override fun onResponse(
					call: Call,
					response: Response,
				) {
					continuation.resume(response) { _, value, _ ->
						value.closeQuietly()
					}
				}
			},
		)
	}
