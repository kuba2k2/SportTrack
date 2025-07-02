package pl.szczodrzynski.tracker.ui.components

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import kotlin.reflect.typeOf

class IIconNavType : NavType<IIcon>(isNullableAllowed = false) {

	override fun put(bundle: SavedState, key: String, value: IIcon) =
		bundle.putString(key, value.name)

	override fun get(bundle: SavedState, key: String) =
		bundle.getString(key)?.let(CommunityMaterial::getIcon)

	override fun parseValue(value: String) =
		CommunityMaterial.getIcon(value)
}

val navTypeMap = mapOf(
	typeOf<IIcon>() to IIconNavType(),
)
