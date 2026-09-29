package app.stickerport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.stickerport.ui.theme.StickerportTheme

/**
 * The app shell (spec §12: Material 3, edge-to-edge).
 *
 * This is a deliberately temporary stand-in for the real thing: T0.3 replaces `content` with a
 * `NavHost` and adds a top bar. What is *not* temporary is the `Scaffold` and the insets handling
 * — every screen will sit inside this, so getting it right once here is the point of T0.2.
 *
 * `Scaffold` defaults to consuming `WindowInsets.systemBars` for its top and bottom bars, and
 * passes the *remaining* insets to `content` as [PaddingValues]. That is what keeps content clear
 * of the status bar and the gesture-navigation area.
 */
@Composable
fun AppShell(
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        content = content,
    )
}

/**
 * Placeholder body for the Home screen. Replaced in T4.1.
 *
 * Kept dumb on purpose: it is a pure function of its inputs, so it is previewable and screenshot-able
 * without a ViewModel, exactly like the real screens will be.
 */
@Composable
fun PlaceholderHome(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Stickerport",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Telegram sticker packs, converted for your messenger.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Light", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun AppShellLightPreview() {
    StickerportTheme(darkTheme = false) {
        AppShell { inner -> PlaceholderHome(innerPadding = inner) }
    }
}

@Preview(name = "Dark", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun AppShellDarkPreview() {
    StickerportTheme(darkTheme = true) {
        AppShell { inner -> PlaceholderHome(innerPadding = inner) }
    }
}

/**
 * Preview of the fallback (non-dynamic) brand palette, which is what Android 11 and below get.
 * Android 12+ previews show the wallpaper-derived scheme instead, so this is the only way to
 * eyeball the fallback without running on an old device.
 */
@Preview(name = "Brand fallback (no dynamic colour)", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun AppShellBrandFallbackPreview() {
    StickerportTheme(darkTheme = false, dynamicColor = false) {
        AppShell { inner -> PlaceholderHome(innerPadding = inner) }
    }
}
