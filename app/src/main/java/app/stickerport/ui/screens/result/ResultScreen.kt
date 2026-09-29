package app.stickerport.ui.screens.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.stickerport.ui.components.PlaceholderScreen
import app.stickerport.ui.theme.StickerportTheme

/** Per-pack results — spec §12.4. Route and state only; built in Sprint 4 (T4.2). */
@Immutable
data class ResultUiState(
    val importId: Long = 0L,
    val packCount: Int = 0,
    /** §12.4 partial-failure banner: "3 stickers couldn't be converted". */
    val failedCount: Int = 0,
    val whatsappInstalled: Boolean = true,
)

@Composable
fun ResultScreen(
    state: ResultUiState,
    modifier: Modifier = Modifier,
    onAddToWhatsApp: (String) -> Unit = {},
    onOpenPack: (String) -> Unit = {},
) {
    PlaceholderScreen(
        heading = "Result",
        modifier = modifier,
        arguments = listOf("importId" to state.importId.toString()),
    )
}

@Preview(name = "Result route", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ResultScreenPreview() {
    StickerportTheme(darkTheme = false, dynamicColor = false) {
        ResultScreen(state = ResultUiState(importId = 1L, packCount = 2))
    }
}
