package app.stickerport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.stickerport.ui.components.AppShell
import app.stickerport.ui.components.PlaceholderHome
import app.stickerport.ui.theme.StickerportTheme

/**
 * The single activity. T0.2 gives it edge-to-edge and the theme; T0.3 adds the `NavHost`.
 *
 * It is a plain [ComponentActivity], not an AppCompat one: the UI is entirely Compose, and with
 * `enableEdgeToEdge()` there are no platform widgets to theme, so AppCompat would only add
 * dependency and theme-mapping work for no benefit.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate() so the window is laid out behind the system bars
        // from the first frame. On Android 15+ (API 35) this is also *enforced* when targetSdk >= 35,
        // which is why the T0.1 placeholder had its text hidden under the status bar on modern
        // devices: nothing was consuming the insets.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            StickerportTheme {
                AppShell { innerPadding ->
                    PlaceholderHome(innerPadding = innerPadding)
                }
            }
        }
    }
}
