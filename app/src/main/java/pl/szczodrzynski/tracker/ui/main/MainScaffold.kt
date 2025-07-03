package pl.szczodrzynski.tracker.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import kotlinx.coroutines.flow.update
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.manager.SyncManager
import pl.szczodrzynski.tracker.manager.TrackerManager
import pl.szczodrzynski.tracker.ui.NavTarget
import pl.szczodrzynski.tracker.ui.NavTarget.Companion.setPopUpTo
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.components.navTypeMap
import pl.szczodrzynski.tracker.ui.screen.history.HistoryScreen
import pl.szczodrzynski.tracker.ui.screen.home.HomeScreen
import pl.szczodrzynski.tracker.ui.screen.login.LoginScreen
import pl.szczodrzynski.tracker.ui.screen.training.TrainingRunDialog
import pl.szczodrzynski.tracker.ui.screen.training.TrainingScreen

@Composable
@Preview
private fun Preview() {
	SportTrackPreview {
		MainScaffold()
	}
}

private val navigationBarItems: List<NavTarget> = listOf(
	NavTarget.Home,
	NavTarget.Training(forceNew = false),
	NavTarget.History(trainingId = null),
	NavTarget.Profile,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
fun MainScaffold() {
	val mainVm = LocalMainViewModel.current
	val inspectionMode = LocalInspectionMode.current
	val navController = rememberNavController()
	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

	LaunchedEffect(Unit) {
		// mainVm.sync.startSync()
		mainVm.nextRoute.collect {
			navController.navigate(it) {
				setPopUpTo(it)
			}
		}
	}

	val syncState by mainVm.sync.state.collectAsStateWithLifecycle()

	val currentBackStackEntry by navController.currentBackStackEntryAsState()
	val navTarget = currentBackStackEntry
		?.let(NavTarget::deserialize)
		?: mainVm.initialRoute

	val training by mainVm.manager.training.collectAsStateWithLifecycle()
	val runState by mainVm.manager.runState.collectAsStateWithLifecycle()
	val forceRunDialog by mainVm.forceRunDialog.collectAsStateWithLifecycle()

	(runState as? TrackerManager.State.InProgress)?.let { state ->
		val isHiddenByMode = state.trainingRun.isFlyingTest && state.splits.isEmpty()
		if (isHiddenByMode && !forceRunDialog)
			return@let
		TrainingRunDialog(
			trainingRun = state.trainingRun,
			splits = state.splits,
			athlete = state.athlete,
			lastResult = state.lastResult,
			finishTimeout = state.finishTimeout,
			isForcedShow = isHiddenByMode,
			onDismiss = {
				mainVm.forceRunDialog.update { false }
				if (!isHiddenByMode)
					mainVm.manager.finishRun()
			},
		)
	}

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
						Iconics(CommunityMaterial.Icon.cmd_arrow_left)
					}
				},
				actions = {
					Box(contentAlignment = Alignment.Center) {
						when (val photoUrl = mainVm.currentUser?.photoUrl) {
							null -> FilledTonalIconButton(
								onClick = {
									mainVm.navigate(NavTarget.Profile)
								},
							) {
								Iconics(CommunityMaterial.Icon.cmd_account_circle_outline)
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

						if (syncState is SyncManager.State.UploadSuccess || syncState is SyncManager.State.Downloading) {
							CircularProgressIndicator(
								modifier = Modifier.size(IconButtonDefaults.smallContainerSize()),
							)
						}
					}
				},
				scrollBehavior = scrollBehavior,
			)
		},
		bottomBar = {
			if (!navTarget.inNavBar)
				return@Scaffold
			NavigationBar {
				navigationBarItems.forEach { target ->
					NavigationBarItem(
						selected = navTarget::class.java == target::class.java,
						onClick = {
							mainVm.navigate(target)
						},
						icon = {
							Iconics(target.icon)
						},
						enabled = target !is NavTarget.Training || training != null,
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

			composable<NavTarget.Training>(typeMap = navTypeMap) {
				TrainingScreen(forceNew = (navTarget as? NavTarget.Training)?.forceNew ?: false)
			}

			composable<NavTarget.History>(typeMap = navTypeMap) {
				HistoryScreen(trainingId = (navTarget as? NavTarget.History)?.trainingId)
			}

			composable<NavTarget.Profile> {
				LoginScreen(isProfile = true)
			}
		}
	}
}
