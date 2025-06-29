package pl.szczodrzynski.tracker.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import dagger.hilt.android.AndroidEntryPoint
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.NavTarget.Companion.setPopUpTo
import pl.szczodrzynski.tracker.ui.screen.home.HomeScreen
import pl.szczodrzynski.tracker.ui.screen.login.LoginScreen
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)
		setContent {
			SportTrackTheme {
				MainRoot()
			}
		}
	}
}

@Composable
@Preview
private fun Preview() {
	SportTrackTheme {
		MainRoot(isPreview = true)
	}
}

@Composable
fun MainRoot(
	isPreview: Boolean = false,
	vm: MainViewModel = viewModel(),
) {
	val navController = rememberNavController()

	LaunchedEffect(Unit) {
		vm.nextRoute.collect {
			navController.navigate(it) {
				setPopUpTo(it)
			}
		}
	}

	CompositionLocalProvider(LocalMainViewModel provides vm) {
		Main(isPreview, vm, navController)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Main(
	isPreview: Boolean = false,
	vm: MainViewModel,
	navController: NavHostController,
) {
	val topAppBarState = rememberTopAppBarState()
	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
	val scrollState = rememberScrollState()

	Scaffold(
		modifier = Modifier
			.fillMaxSize()
			.nestedScroll(scrollBehavior.nestedScrollConnection),
		topBar = {
			val currentBackStackEntry by navController.currentBackStackEntryAsState()
			val navTarget = currentBackStackEntry?.let(NavTarget::deserialize) ?: vm.initialRoute

			CenterAlignedTopAppBar(
				title = {
					Text(stringResource(navTarget.titleRes))
				},
				navigationIcon = {
					if (navController.previousBackStackEntry == null)
						return@CenterAlignedTopAppBar
					IconButton(onClick = {
						navController.navigateUp()
					}) {
						Image(
							CommunityMaterial.Icon.cmd_arrow_left,
							colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
						)
					}
				},
				scrollBehavior = scrollBehavior,
			)
		},
	) { innerPadding ->
		NavHost(
			navController,
			startDestination = if (isPreview) NavTarget.Empty else vm.initialRoute,
			modifier = Modifier
				.padding(innerPadding)
				.verticalScroll(scrollState)
				.imePadding(),
		) {
			composable<NavTarget.Empty> {
				Text(stringResource(R.string.app_name))
			}

			// cannot reliably preview composables with a HiltViewModel
			if (isPreview)
				return@NavHost

			composable<NavTarget.Login> { it ->
				Log.d("Main", it.toRoute<NavTarget.Login>().toString())
				LoginScreen(isRegister = false)
			}

			composable<NavTarget.Register> {
				LoginScreen(isRegister = true)
			}

			composable<NavTarget.Home> {
				HomeScreen()
			}
		}
	}
}
