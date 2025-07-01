package pl.szczodrzynski.tracker.service.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

data class TrackerDevice(
	val name: String,
	val address: String,
	var state: State,
	var bluetoothDevice: BluetoothDevice? = null,
) {
	enum class State {
		NONE,
		BONDING,
		BONDED,
		CONNECTING,
		CONNECTED,
	}

	@SuppressLint("MissingPermission")
	constructor(device: BluetoothDevice) :
		this(
			name = device.name,
			address = device.address,
			state = when (device.bondState) {
				BluetoothDevice.BOND_BONDING -> State.BONDING
				BluetoothDevice.BOND_BONDED -> State.BONDED
				else -> State.NONE
			},
			bluetoothDevice = device,
		)
}
