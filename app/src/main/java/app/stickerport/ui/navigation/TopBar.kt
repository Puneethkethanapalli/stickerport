package app.stickerport.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController

/**
 * What the shared top bar should show for the current destination.
 *
 * One [Scaffold] owns the top bar for the whole app (T0.2 established that insets must be handled
 * in exactly one place), so the bar is *derived from the route* rather than declared by each
 * screen. Screens stay stateless: they render state and raise events, and the shell turns those
 * events into navigation.
 */
data class TopBarSpec(
    val title: String,
    /** Show a back arrow. False only for destinations that are roots of the back stack. */
    val showBack: Boolean,
    val actions: List<TopBarAction> = emptyList(),
)

/** A single icon button in the top bar's action row. */
data class TopBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
)

/**
 * Screen titles, kept in one place so the shell and the individual screens cannot disagree.
 *
 * These are user-visible strings with no localization yet (§19 ships English only), so they live
 * as plain constants. A real app would move them to `res/values/strings.xml`; the strings are
 * deliberately kept out of resources in the skeleton so that a grep for a route name or a screen
 * name finds it in exactly one file.
 */
object RouteTitles {
    const val HOME = "Stickerport"
    const val LIBRARY = "Library"
    const val SETTINGS = "Settings"
    const val PREVIEW = "Choose stickers"
    const val PROGRESS = "Converting"
    const val RESULT = "Your packs"
    const val PACK_DETAIL = "Pack"
    const val ONBOARDING = "Welcome"
}

/**
 * Resolve the top bar for [this] destination.
 *
 * Uses `hasRoute<T>()` type checks rather than matching the route *string*. String matching would
 * have to hard-code the serializer-generated pattern (for example
 * `app.stickerport.ui.navigation.Preview/{setName}`), which breaks silently whenever a route class
 * is renamed or moved to another package.
 *
 * Returns `null` for a destination that has no bar yet, rather than throwing — a newly added route
 * should render before anyone remembers to style it.
 */
fun NavDestination.topBarSpec(
    navController: NavHostController,
): TopBarSpec? = when {
    hasRoute<Home>() -> TopBarSpec(
        title = RouteTitles.HOME,
        showBack = false,
        actions = listOf(
            TopBarAction(
                icon = Icons.AutoMirrored.Filled.List,
                contentDescription = RouteTitles.LIBRARY,
                onClick = { navController.navigate(Library) },
            ),
            TopBarAction(
                icon = Icons.Filled.Settings,
                contentDescription = RouteTitles.SETTINGS,
                onClick = { navController.navigate(Settings) },
            ),
        ),
    )

    hasRoute<Onboarding>() -> TopBarSpec(
        title = RouteTitles.ONBOARDING,
        showBack = false,
    )

    hasRoute<Library>() -> TopBarSpec(
        title = RouteTitles.LIBRARY,
        showBack = true,
        actions = listOf(
            TopBarAction(
                icon = Icons.Filled.Settings,
                contentDescription = RouteTitles.SETTINGS,
                onClick = { navController.navigate(Settings) },
            ),
        ),
    )

    hasRoute<Settings>() -> TopBarSpec(
        title = RouteTitles.SETTINGS,
        showBack = true,
    )

    hasRoute<Preview>() -> TopBarSpec(
        title = RouteTitles.PREVIEW,
        showBack = true,
    )

    hasRoute<Progress>() -> TopBarSpec(
        title = RouteTitles.PROGRESS,
        showBack = true,
    )

    hasRoute<Result>() -> TopBarSpec(
        title = RouteTitles.RESULT,
        showBack = true,
    )

    hasRoute<PackDetail>() -> TopBarSpec(
        title = RouteTitles.PACK_DETAIL,
        showBack = true,
    )

    else -> null
}

/** The back arrow, `automirrored` so it flips in RTL layouts. */
internal val BackIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
