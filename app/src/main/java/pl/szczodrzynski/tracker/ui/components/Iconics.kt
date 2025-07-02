package pl.szczodrzynski.tracker.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.IIcon

@Composable
fun Iconics(
	icon: IIcon,
	modifier: Modifier = Modifier,
	size: Dp = 24.dp,
	color: Color = LocalContentColor.current,
	contentDescription: String? = null,
) {
	Image(
		icon,
		modifier = Modifier
			.size(size)
			.then(modifier),
		contentDescription = contentDescription,
		colorFilter = ColorFilter.tint(color),
	)
}
