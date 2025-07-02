package pl.szczodrzynski.tracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import ovh.plrapps.mapcompose.ui.MapUI

@Composable
fun OpenStreetMap(
	lat: Double,
	lon: Double,
	scale: Double,
	modifier: Modifier = Modifier,
	vm: OpenStreetMapViewModel = hiltViewModel(),
) {
	LaunchedEffect(Unit) {
		vm.scrollToCoordinates(lat, lon, scale)
	}

	MapUI(
		state = vm.mapState,
		modifier = modifier,
	)
}
