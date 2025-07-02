package pl.szczodrzynski.tracker.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.NavTarget.Companion.setPopUpTo
import pl.szczodrzynski.tracker.ui.screen.home.HomeScreen
import pl.szczodrzynski.tracker.ui.screen.login.LoginScreen

@Composable
@Preview
private fun Preview() {
	SportTrackPreview {
		MainScaffold()
	}
}

private val navigationBarItems = listOf(
	NavTarget.Home,
	NavTarget.Training,
	NavTarget.History,
	NavTarget.Profile,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
fun MainScaffold() {
	val mainVm = LocalMainViewModel.current
	val inspectionMode = LocalInspectionMode.current
	val navController = rememberNavController()
	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
	val scrollState = rememberScrollState()

	LaunchedEffect(Unit) {
		mainVm.nextRoute.collect {
			navController.navigate(it) {
				setPopUpTo(it)
			}
		}
	}

	val currentBackStackEntry by navController.currentBackStackEntryAsState()
	val navTarget = currentBackStackEntry
		?.let(NavTarget::deserialize)
		?: mainVm.initialRoute

	val training by mainVm.manager.training.collectAsStateWithLifecycle()

	Scaffold(
		modifier = Modifier
			.fillMaxSize()
			.nestedScroll(scrollBehavior.nestedScrollConnection),
		topBar = {
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
							colorFilter = ColorFilter.tint(LocalContentColor.current),
						)
					}
				},
				actions = {
					when (val photoUrl = mainVm.currentUser?.photoUrl) {
						null -> FilledTonalIconButton(
							onClick = {
								mainVm.navigate(NavTarget.Profile)
							},
						) {
							Image(
								CommunityMaterial.Icon.cmd_account_circle_outline,
								colorFilter = ColorFilter.tint(LocalContentColor.current),
							)
						}

						else -> AsyncImage(
							photoUrl.toString(),
							contentDescription = mainVm.currentUser?.displayName,
							contentScale = ContentScale.Crop,
							modifier = Modifier
								.padding(4.dp)
								.size(IconButtonDefaults.smallContainerSize())
								.clip(CircleShape)
								.clickable {
									mainVm.navigate(NavTarget.Profile)
								},
						)
					}
				},
				scrollBehavior = scrollBehavior,
			)
		},
		bottomBar = {
			if (navTarget !in navigationBarItems)
				return@Scaffold
			NavigationBar {
				navigationBarItems.forEach { target ->
					NavigationBarItem(
						selected = navTarget == target,
						onClick = {
							mainVm.navigate(target)
						},
						icon = {
							Image(
								target.icon,
								colorFilter = ColorFilter.tint(LocalContentColor.current),
							)
						},
						enabled = target != NavTarget.Training || training != null,
						label = {
							Text(stringResource(target.titleRes))
						},
					)
				}
			}
		},
	) { innerPadding ->
		NavHost(
			navController,
			startDestination = if (inspectionMode) NavTarget.Empty else mainVm.initialRoute,
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(scrollState)
				.padding(innerPadding)
				.imePadding(),
		) {
			composable<NavTarget.Empty> {
				Text(stringResource(R.string.app_name))
			}

			// cannot reliably preview composables with a HiltViewModel
			if (inspectionMode)
				return@NavHost

			composable<NavTarget.Login> {
				LoginScreen()
			}

			composable<NavTarget.Register> {
				LoginScreen(isRegister = true)
			}

			composable<NavTarget.Home> {
				HomeScreen()
			}

			composable<NavTarget.Training> {
				Text(stringResource(R.string.training_title))
			}

			composable<NavTarget.History> {
				Text(stringResource(R.string.history_title))
			}

			composable<NavTarget.Profile> {
				LoginScreen(isProfile = true)
			}
		}
	}
}
