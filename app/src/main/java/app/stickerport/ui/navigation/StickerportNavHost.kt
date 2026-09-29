package app.stickerport.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import app.stickerport.ui.screens.home.HomeScreen
import app.stickerport.ui.screens.home.HomeUiState
import app.stickerport.ui.screens.home.SkeletonRoute
import app.stickerport.ui.screens.library.LibraryScreen
import app.stickerport.ui.screens.library.LibraryUiState
import app.stickerport.ui.screens.onboarding.OnboardingScreen
import app.stickerport.ui.screens.onboarding.OnboardingUiState
import app.stickerport.ui.screens.preview.PreviewScreen
import app.stickerport.ui.screens.preview.PreviewUiState
import app.stickerport.ui.screens.progress.ProgressScreen
import app.stickerport.ui.screens.progress.ProgressUiState
import app.stickerport.ui.screens.result.PackDetailScreen
import app.stickerport.ui.screens.result.PackDetailUiState
import app.stickerport.ui.screens.result.ResultScreen
import app.stickerport.ui.screens.result.ResultUiState
import app.stickerport.ui.screens.settings.SettingsScreen
import app.stickerport.ui.screens.settings.SettingsUiState

/**
 * The whole navigation graph.
 *
 * Every destination is registered by **type**, not by route string, so there is exactly one place
 * to add a screen and no string template to keep in sync with a Kotlin class. Arguments are read
 * with [toRoute] and are therefore type-checked at the call site.
 *
 * ## Where the state lives right now
 * Each screen is passed a `remember`-held `UiState` and event lambdas. That is deliberate: T0.3 is
 * about the *graph*, and T0.4 is the task that adds Hilt, so this is the last commit where wiring
 * a ViewModel is not yet expected. From T4.1 each `composable<T>` block grows
 * `val viewModel: XViewModel = hiltViewModel()` and drops its `remember`.
 *
 * ## `launchSingleTop`
 * Applied to transitions between top-level destinations so that repeatedly tapping "Library" (or
 * "Settings") from different screens does not grow the back stack without bound. T4.x, when the
 * Home overflow menu replaces the top-bar actions, is also where these decide whether a real
 * `popUpTo(startDestination)` single-top is wanted instead.
 */
@Composable
fun StickerportNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier,
    ) {
        // ---- Root ----

        composable<Home> {
            // rememberSaveable, not a ViewModel: see the note above.
            var linkText by rememberSaveable { mutableStateOf("") }

            HomeScreen(
                state = HomeUiState(
                    linkText = linkText,
                    // No Room yet (T0.4), so there is nothing real to show here.
                    recents = emptyList(),
                    hasBotToken = false,
                ),
                onLinkTextChange = { linkText = it },
                // T1.1 owns the §8.1 parser; T0.3 hands the raw text through after a naive
                // last-segment strip, which is enough to prove arguments reach the destination.
                onSubmitLink = { raw ->
                    navController.navigate(Preview(setName = raw.substringAfterLast('/')))
                },
                onOpenLibrary = { navController.navigate(Library) { launchSingleTop = true } },
                onOpenSettings = { navController.navigate(Settings) { launchSingleTop = true } },
                onOpenOnboarding = { navController.navigate(Onboarding) },
                onNavigateToRoute = { route ->
                    navController.navigate(
                        when (route) {
                            SkeletonRoute.PREVIEW -> Preview(setName = SAMPLE_SET_NAME)
                            SkeletonRoute.PROGRESS -> Progress(importId = SAMPLE_IMPORT_ID)
                            SkeletonRoute.RESULT -> Result(importId = SAMPLE_IMPORT_ID)
                            SkeletonRoute.PACK_DETAIL -> PackDetail(packId = SAMPLE_PACK_ID)
                        },
                    )
                },
            )
        }

        // ---- Children ----

        composable<Onboarding> {
            OnboardingScreen(
                state = OnboardingUiState(whatsappInstalled = null),
                onContinue = {
                    // Onboarding is only reachable from Home, so popping lands back there with a
                    // fresh Home rather than a re-composed one.
                    if (!navController.popBackStack()) {
                        navController.navigate(Home) { launchSingleTop = true }
                    }
                },
            )
        }

        composable<Library> {
            LibraryScreen(state = LibraryUiState())
        }

        composable<Settings> {
            SettingsScreen(state = SettingsUiState())
        }

        // ---- Argument-carrying destinations ----
        // `entry.toRoute<T>()` is the only place arguments are read, and it is type-checked, so a
        // renamed property or a wrong type is a compile error rather than a runtime crash.

        composable<Preview> { entry ->
            val route: Preview = entry.toRoute()
            PreviewScreen(
                state = PreviewUiState(
                    setName = route.setName,
                    title = route.setName,
                ),
            )
        }

        composable<Progress> { entry ->
            val route: Progress = entry.toRoute()
            ProgressScreen(
                state = ProgressUiState(importId = route.importId, total = SAMPLE_STICKER_COUNT),
            )
        }

        composable<Result> { entry ->
            val route: Result = entry.toRoute()
            ResultScreen(
                state = ResultUiState(importId = route.importId, packCount = SAMPLE_PACK_COUNT),
            )
        }

        composable<PackDetail> { entry ->
            val route: PackDetail = entry.toRoute()
            PackDetailScreen(
                state = PackDetailUiState(
                    packId = route.packId,
                    name = SAMPLE_SET_NAME,
                    stickerCount = SAMPLE_STICKER_COUNT,
                ),
            )
        }
    }
}

/** Sample arguments, so every route is reachable by hand before any data exists. */
private const val SAMPLE_SET_NAME = "PackByPack_Animals"
private const val SAMPLE_IMPORT_ID = 1L
private const val SAMPLE_PACK_ID = "3f1c9a2e-6b54-4d18-9f77-0c2b5a8e41d3"
private const val SAMPLE_STICKER_COUNT = 48
private const val SAMPLE_PACK_COUNT = 2
