package pl.szczodrzynski.tracker.ui.screen.home

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import kotlinx.coroutines.flow.onCompletion
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.service.TrackerDevice
import pl.szczodrzynski.tracker.ui.main.LocalMainViewModel
import pl.szczodrzynski.tracker.ui.main.SportTrackPreview
import timber.log.Timber

@Preview
@Composable
private fun Preview() {
	SportTrackPreview {
		DeviceDialog()
	}
}

@Composable
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun DeviceDialog(
	onChooseDevice: (device: TrackerDevice) -> Unit = {},
	onDismiss: () -> Unit = {},
) {
	val mainVm = LocalMainViewModel.current
	val inspectionMode = LocalInspectionMode.current

	var scan by remember { mutableStateOf(false) }
	var bondedList by remember { mutableStateOf(listOf<TrackerDevice?>()) }
	var foundList by remember { mutableStateOf(listOf<TrackerDevice?>()) }

	LaunchedEffect(scan) {
		if (inspectionMode) {
			bondedList = (1..15).map {
				TrackerDevice("Device $it", "$it", TrackerDevice.State.BONDED)
			}
			if (scan)
				foundList = listOf(
					TrackerDevice("New Device 4", "4", TrackerDevice.State.NONE),
				)
			return@LaunchedEffect
		}

		mainVm.binder?.getBluetoothDevices(scan)
			?.onCompletion {
				scan = false
			}
			?.collect { devices ->
				bondedList = devices.filter { it.state == TrackerDevice.State.BONDED }
					.sortedBy { it.name }
					.takeIf { it.isNotEmpty() }
					?: listOf(null)
				foundList = devices.filter { it.state != TrackerDevice.State.BONDED }
					.sortedBy { it.name }
					.takeIf { it.isNotEmpty() }
					?: listOf(null)
			}
	}

	Timber.d("Bonded list = $bondedList")
	Timber.d("Found list = $foundList")

	AlertDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
				Text(stringResource(R.string.home_choose_device_cancel))
			}
		},
		icon = {
			Image(
				CommunityMaterial.Icon.cmd_bluetooth_connect,
				colorFilter = ColorFilter.tint(LocalContentColor.current),
			)
		},
		title = {
			Text(stringResource(R.string.home_choose_device))
		},
		text = {
			DevicesList(
				bondedList = bondedList,
				foundList = foundList,
				isScanning = scan,
				onDeviceClick = onChooseDevice,
				onScanClick = {
					scan = true
				},
			)
		},
	)
}

@Composable
private fun DevicesList(
	bondedList: List<TrackerDevice?>,
	foundList: List<TrackerDevice?>,
	isScanning: Boolean,
	onDeviceClick: (device: TrackerDevice) -> Unit,
	onScanClick: () -> Unit,
) {
	LazyColumn {
		item {
			Text(
				stringResource(R.string.home_choose_device_bonded),
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.labelMedium,
			)
		}
		items(bondedList, key = { it?.address ?: "empty1" }) {
			DeviceItem(device = it, onDeviceClick = onDeviceClick)
		}

		item {
			Text(
				stringResource(R.string.home_choose_device_found),
				modifier = Modifier.padding(top = 8.dp),
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.labelMedium,
			)
		}
		items(foundList, key = { it?.address ?: "empty2" }) {
			DeviceItem(device = it, onDeviceClick = onDeviceClick)
		}

		item {
			Row(
				modifier = Modifier
					.padding(top = 8.dp)
					.fillParentMaxWidth(),
				horizontalArrangement = Arrangement.Center,
			) {
				if (isScanning) {
					LinearProgressIndicator()
					return@Row
				}
				Button(
					onClick = onScanClick,
				) {
					Text(stringResource(R.string.home_choose_device_search))
				}
			}
		}
	}
}

@Composable
private fun LazyItemScope.DeviceItem(
	device: TrackerDevice?,
	onDeviceClick: (device: TrackerDevice) -> Unit,
) {
	if (device == null) {
		Text(
			stringResource(R.string.home_choose_device_empty),
			modifier = Modifier
				.fillParentMaxWidth()
				.padding(vertical = 8.dp),
			textAlign = TextAlign.Center,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
		return
	}

	ListItem(
		modifier = Modifier
			.animateItem()
			.clickable {
				onDeviceClick(device)
			},
		headlineContent = {
			Text(device.name)
		},
		trailingContent = {
			if (device.state == TrackerDevice.State.BONDED)
				Image(
					CommunityMaterial.Icon2.cmd_link,
					colorFilter = ColorFilter.tint(LocalContentColor.current),
				)
		},
		colors = ListItemDefaults.colors(
			containerColor = Color.Transparent,
		)
	)
}
