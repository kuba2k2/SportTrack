package pl.szczodrzynski.tracker.ui.screen.training.metadata

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TrainingMetadataManager {

	fun getLocationPermissions() =
		listOf(
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION,
		)

	fun checkLocationPermissions(context: Context) =
		getLocationPermissions().associateWith {
			ContextCompat.checkSelfPermission(context, it) == PermissionChecker.PERMISSION_GRANTED
		}

	@SuppressLint("MissingPermission")
	@Throws(ResolvableApiException::class)
	suspend fun fetchCurrentLocation(
		context: Context,
	) = withContext<Location?>(Dispatchers.IO) {
		if (!checkLocationPermissions(context).containsValue(true))
			return@withContext null

		val client = LocationServices.getFusedLocationProviderClient(context)
		val settingsClient = LocationServices.getSettingsClient(context)

		val currentLocationRequest = CurrentLocationRequest.Builder()
			.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
			.build()
		val settingsRequest = LocationSettingsRequest.Builder()
			.addLocationRequest(
				LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000).build()
			)
			.setAlwaysShow(true)
			.build()

		Timber.d("Checking location settings")
		suspendCoroutine { continuation ->
			settingsClient.checkLocationSettings(settingsRequest).addOnCompleteListener {
				if (it.isSuccessful) {
					val result = it.result
					Timber.d("Location settings received: $result")
					continuation.resume(null)
				} else {
					continuation.resumeWithException(it.exception ?: RuntimeException())
				}
			}
		}

		Timber.d("Fetching current location")
		return@withContext suspendCoroutine { continuation ->
			client.getCurrentLocation(currentLocationRequest, null).addOnCompleteListener {
				if (it.isSuccessful) {
					val result = it.result
					Timber.d("Location received: $result")
					continuation.resume(result ?: null)
				} else {
					Timber.d(it.exception, "Location failed")
					continuation.resume(null)
				}
			}
		}
	}

	suspend fun fetchLocationAddress(
		context: Context,
		latitude: Double,
		longitude: Double,
	) = withContext(Dispatchers.IO) {
		if (!Geocoder.isPresent()) {
			Timber.w("Geocoder is not present")
			return@withContext null
		}
		val geocoder = Geocoder(context)

		Timber.d("Fetching addresses for $latitude,$longitude")
		val addresses = when {
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> suspendCoroutine { continuation ->
				try {
					geocoder.getFromLocation(latitude, longitude, 1, continuation::resume)
				} catch (e: Exception) {
					Timber.e(e, "Geocoder failed")
					continuation.resume(null)
				}
			} ?: return@withContext null

			else -> {
				try {
					geocoder.getFromLocation(latitude, longitude, 1)
						?: return@withContext null
				} catch (e: Exception) {
					Timber.e(e, "Geocoder failed")
					return@withContext null
				}
			}
		}

		Timber.d("Addresses for location $latitude,$longitude: $addresses")

		return@withContext addresses.firstOrNull()
	}
}
