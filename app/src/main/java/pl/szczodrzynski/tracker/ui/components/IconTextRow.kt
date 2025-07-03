package pl.szczodrzynski.tracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.typeface.IIcon

@Composable
fun IconTextRow(
	icon: IIcon,
	text: String?,
	modifier: Modifier = Modifier,
	extraText: String? = null,
	maxLines: Int? = 1,
) {
	text ?: return
	Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
		Iconics(icon, size = 16.dp)

		Text(
			text = text,
			modifier = Modifier
				.padding(start = 4.dp)
				.weight(1.0f),
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = maxLines ?: Int.MAX_VALUE,
		)

		if (extraText != null)
			Text(
				text = extraText,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
	}
}

@Composable
fun MultiIconTextRow(
	vararg values: Pair<IIcon, String?>,
	modifier: Modifier = Modifier,
) {
	Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
		values.filter { it.second != null }.forEach { (icon, text) ->
			text ?: return@forEach
			Iconics(icon, size = 16.dp)
			Text(
				text = text,
				modifier = Modifier.padding(start = 4.dp, end = 8.dp),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
	}
}

@Composable
fun TitleIconTextRow(
	modifier: Modifier = Modifier,
	icon: IIcon? = null,
	text: String? = null,
	extraText: String? = null,
	maxLines: Int? = 1,
) {
	text ?: return
	Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
		if (icon != null)
			Iconics(icon = icon, size = 24.dp)

		Text(
			text,
			modifier = Modifier
				.weight(1.0f)
				.padding(start = if (icon != null) 8.dp else 0.dp),
			style = MaterialTheme.typography.titleLarge,
			maxLines = maxLines ?: Int.MAX_VALUE,
		)

		if (extraText != null)
			Text(
				text = extraText,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
	}
}
