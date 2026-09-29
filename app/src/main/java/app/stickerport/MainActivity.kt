package app.stickerport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import app.stickerport.ui.components.StickerportApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * The single activity. T0.2 gave it edge-to-edge and the theme; T0.3 gave it the navigation graph;
 * T0.4 made it a Hilt entry point.
 *
 * It is a plain [ComponentActivity], not an AppCompat one: the UI is entirely Compose, and with
 * `enableEdgeToEdge()` there are no platform widgets to theme, so AppCompat would only add
 * dependency and theme-mapping work for no benefit.
 *
 * `@AndroidEntryPoint` is what lets `hiltViewModel()` find the Hilt component. Without it the app
 * compiles and launches, then throws at the first injection — which is exactly the failure mode
 * T0.4's "app launches with Hilt" test exists to catch.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * The app's single `NavHostController`, exposed so `StickerportNavHostTest` can drive
     * navigation. `internal`, so it is invisible to the rest of the app but visible to the
     * `androidTest` source set (Kotlin treats it as a friend module).
     *
     * The test needs it because `createAndroidComposeRule<MainActivity>()` launches the **real**
     * Activity, which has already called `setContent`, so the test cannot install its own content
     * and hand in its own controller.
     *
     * It is assigned from inside `setContent` via `rememberNavController()` — **not** constructed
     * here with `NavHostController(this)`. A hand-built controller has no navigator registered,
     * and the graph then fails with "Could not find Navigator with name \"composable\"". Setup that
     * `rememberNavController()` does for us is exactly the kind that must not be re-implemented.
     */
    internal var navController: NavHostController? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate() so the window is laid out behind the system bars
        // from the first frame. On Android 15+ (API 35) this is also *enforced* when targetSdk >= 35,
        // which is why the T0.1 placeholder had its text hidden under the status bar on modern
        // devices: nothing was consuming the insets.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val controller = rememberNavController().also { navController = it }
            StickerportApp(navController = controller)
        }
    }
}
