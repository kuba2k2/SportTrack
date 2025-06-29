package pl.szczodrzynski.tracker.ui

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.toRoute
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import kotlinx.serialization.Serializable
import pl.szczodrzynski.tracker.R

@Serializable
sealed class NavTarget(
	val titleRes: Int,
	val icon: IIcon,
) {
	@Serializable
	data object Empty : NavTarget(R.string.app_name, CommunityMaterial.Icon3.cmd_run_fast)

	@Serializable
	data object Login : NavTarget(R.string.login_title, CommunityMaterial.Icon.cmd_account_check_outline)

	@Serializable
	data object Register : NavTarget(R.string.login_register_title, CommunityMaterial.Icon.cmd_account_check_outline)

	@Serializable
	data object Home : NavTarget(R.string.home_title, CommunityMaterial.Icon2.cmd_home_outline)

	companion object {
		fun deserialize(navBackStackEntry: NavBackStackEntry) = when (navBackStackEntry.destination.route) {
			Empty::class.java.canonicalName -> navBackStackEntry.toRoute<Empty>()
			Login::class.java.canonicalName -> navBackStackEntry.toRoute<Login>()
			Register::class.java.canonicalName -> navBackStackEntry.toRoute<Register>()
			Home::class.java.canonicalName -> navBackStackEntry.toRoute<Home>()
			else -> null
		}

		fun NavOptionsBuilder.setPopUpTo(navTarget: NavTarget) = when (navTarget) {
			is Home -> popUpTo(0) { inclusive = true }
			else -> null
		}
	}
}
