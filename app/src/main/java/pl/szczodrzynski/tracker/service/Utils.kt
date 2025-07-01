package pl.szczodrzynski.tracker.service

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.content.PermissionChecker
import timber.log.Timber

object Utils {

	fun getBluetoothPermissions() =
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			listOf(
				Manifest.permission.BLUETOOTH_CONNECT,
				Manifest.permission.BLUETOOTH_SCAN,
			)
		else
			listOf(
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.BLUETOOTH,
				Manifest.permission.BLUETOOTH_ADMIN,
			)

	fun Context.hasBluetoothPermissions(): Boolean {
		for (permission in getBluetoothPermissions()) {
			if (PermissionChecker.checkSelfPermission(this, permission) != PermissionChecker.PERMISSION_GRANTED) {
				Timber.d("Permission $permission is not granted")
				return false
			}
		}
		return true
	}
}
