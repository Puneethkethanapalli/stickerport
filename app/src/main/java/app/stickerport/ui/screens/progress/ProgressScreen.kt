package app.stickerport.ui.screens.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.stickerport.ui.components.PlaceholderScreen
import app.stickerport.ui.theme.StickerportTheme

/** Conversion progress — spec §12.3. Route and state only; built in Sprint 6 (T6.x). */
@Immutable
data class ProgressUiState(
    val importId: Long = 0L,
    val done: Int = 0,
    val total: Int = 0,
    /** Downloading → Converting → Packaging. T6.1 makes this real. */
    val stage: String = "",
)

@Composable
fun ProgressScreen(
    state: ProgressUiState,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
) {
    PlaceholderScreen(
        heading = "Progress",
        modifier = modifier,
        arguments = listOf("importId" to state.importId.toString()),
    )
}

@Preview(name = "Progress route", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ProgressScreenPreview() {
    StickerportTheme(darkTheme = false, dynamicColor = false) {
        ProgressScreen(state = ProgressUiState(importId = 1L, done = 12, total = 48))
    }
}
