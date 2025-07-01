package pl.szczodrzynski.tracker.ui.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun DeviceChooser(
	deviceName: String?,
	enabled: Boolean = true,
	onChooseDevice: () -> Unit = {},
) {
	if (deviceName == null) {
		Button(
			onClick = {
				onChooseDevice()
			},
			shapes = ButtonDefaults.shapes(),
			enabled = enabled,
		) {
			Text(stringResource(R.string.home_choose_device))
		}
		return
	}

	SplitButtonLayout(
		leadingButton = {
			SplitButtonDefaults.TonalLeadingButton(
				onClick = {
					onChooseDevice()
				},
				shapes = SplitButtonDefaults.leadingButtonShapesFor(SplitButtonDefaults.MediumContainerHeight),
				enabled = enabled,
			) {
				Image(
					asset = CommunityMaterial.Icon.cmd_bluetooth,
					modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize),
					colorFilter = ColorFilter.tint(LocalContentColor.current)
				)
				Spacer(Modifier.size(ButtonDefaults.IconSpacing))
				Text(deviceName)
			}
		},
		trailingButton = {
			SplitButtonDefaults.TonalTrailingButton(
				checked = false,
				onCheckedChange = {
					if (it)
						onChooseDevice()
				},
				shapes = SplitButtonDefaults.trailingButtonShapesFor(SplitButtonDefaults.MediumContainerHeight),
				enabled = enabled,
			) {
				val rotation: Float by animateFloatAsState(targetValue = if (false) 180f else 0f)
				Image(
					asset = CommunityMaterial.Icon3.cmd_menu_down,
					modifier = Modifier
						.size(SplitButtonDefaults.LeadingIconSize)
						.graphicsLayer {
							this.rotationZ = rotation
						},
					colorFilter = ColorFilter.tint(LocalContentColor.current)
				)
			}
		},
	)
}
