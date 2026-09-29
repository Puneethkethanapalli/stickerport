package app.stickerport.ui.screens.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.stickerport.ui.components.PlaceholderScreen
import app.stickerport.ui.theme.StickerportTheme

/**
 * Import Preview — spec §12.2.
 *
 * Not built in T0.3; the route exists so the back stack, arguments, and predictive back are real.
 * T1.x fills in `state`; T4.x renders it.
 */
@Immutable
data class PreviewUiState(
    /** The Telegram set name being previewed. This is a name, never a full `t.me` URL. */
    val setName: String = "",
    val title: String = "",
    val totalCount: Int = 0,
    val selectedCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@Composable
fun PreviewScreen(
    state: PreviewUiState,
    modifier: Modifier = Modifier,
    onToggleSticker: (String) -> Unit = {},
    onConvert: () -> Unit = {},
) {
    PlaceholderScreen(
        heading = "Import Preview",
        modifier = modifier,
        arguments = listOf("setName" to state.setName),
    )
}

@Preview(name = "Preview route", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PreviewScreenPreview() {
    StickerportTheme(darkTheme = false, dynamicColor = false) {
        PreviewScreen(state = PreviewUiState(setName = "PackByPack_Animals"))
    }
}
