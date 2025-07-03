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
	val inNavBar: Boolean = false,
) {
	@Serializable
	data object Empty : NavTarget(R.string.app_name, CommunityMaterial.Icon3.cmd_run_fast)

	@Serializable
	data object Login : NavTarget(R.string.login_title, CommunityMaterial.Icon.cmd_account_check_outline)

	@Serializable
	data object Register : NavTarget(R.string.login_register_title, CommunityMaterial.Icon.cmd_account_check_outline)

	@Serializable
	data object Home :
		NavTarget(R.string.home_title, CommunityMaterial.Icon.cmd_connection, inNavBar = true)

	@Serializable
	data class Training(val forceNew: Boolean) :
		NavTarget(R.string.training_title, CommunityMaterial.Icon3.cmd_run, inNavBar = true)

	@Serializable
	data class History(val trainingId: Int?) :
		NavTarget(R.string.history_title, CommunityMaterial.Icon.cmd_clipboard_text_clock_outline, inNavBar = true)

	@Serializable
	data object Profile :
		NavTarget(R.string.profile_title, CommunityMaterial.Icon.cmd_account_outline, inNavBar = true)

	companion object {
		fun deserialize(navBackStackEntry: NavBackStackEntry) =
			when (navBackStackEntry.destination.route?.substringBefore('/')) {
				Empty::class.java.canonicalName -> navBackStackEntry.toRoute<Empty>()
				Login::class.java.canonicalName -> navBackStackEntry.toRoute<Login>()
				Register::class.java.canonicalName -> navBackStackEntry.toRoute<Register>()
				Home::class.java.canonicalName -> navBackStackEntry.toRoute<Home>()
				Training::class.java.canonicalName -> navBackStackEntry.toRoute<Training>()
				History::class.java.canonicalName -> navBackStackEntry.toRoute<History>()
				Profile::class.java.canonicalName -> navBackStackEntry.toRoute<Profile>()
				else -> null
			}

		fun NavOptionsBuilder.setPopUpTo(navTarget: NavTarget) = when (navTarget) {
			is Home -> popUpTo(0) { inclusive = true }
			is Training -> popUpTo(Home) { inclusive = false }
			is History -> if (navTarget.trainingId == null) popUpTo(Home) { inclusive = false } else null
			is Profile -> popUpTo(Home) { inclusive = false }
			else -> null
		}
	}
}
