package app.stickerport.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.stickerport.ui.navigation.BackIcon
import app.stickerport.ui.navigation.StickerportNavHost
import app.stickerport.ui.navigation.topBarSpec

/**
 * The app shell (spec §12: Material 3, edge-to-edge, predictive back).
 *
 * This is the **only** [Scaffold] in the app. That is the whole point of T0.2 + T0.3: window insets
 * and the top bar are handled in exactly one place, so a new screen cannot get them wrong. Screens
 * are pure content and never touch `WindowInsets` themselves.
 *
 * The top bar is derived from the current [NavDestination] (see `topBarSpec`) rather than declared
 * per screen. That keeps screens stateless — they do not know or care what the bar says.
 *
 * Predictive back needs nothing here: `android:enableOnBackInvokedCallback="true"` is in the
 * manifest (T0.2) and `NavHost` animates the pop itself.
 *
 * @param navController injectable so instrumented tests and previews can drive a specific graph or
 *   deep link. Defaults to a fresh controller, which is what production wants.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerportApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val spec = backStackEntry?.destination?.topBarSpec(navController)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (spec != null) {
                TopAppBar(
                    title = { Text(spec.title) },
                    navigationIcon = {
                        if (spec.showBack) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(BackIcon, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        spec.actions.forEach { action ->
                            IconButton(onClick = action.onClick) {
                                Icon(action.icon, action.contentDescription)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            }
        },
    ) { innerPadding ->
        StickerportNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
