package app.stickerport.ui.screens.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.stickerport.ui.components.PlaceholderScreen
import app.stickerport.ui.theme.StickerportTheme

/**
 * Pack Detail — spec §12.4, "Tap card → Pack Detail".
 *
 * Lives in `screens/result/` rather than getting its own package because §7.2 fixes the directory
 * list and does not include one for it; this screen is part of the Result flow.
 */
@Immutable
data class PackDetailUiState(
    /** `PackEntity.id` — a stable UUID string, not a row offset. */
    val packId: String = "",
    val name: String = "",
    val stickerCount: Int = 0,
    val animated: Boolean = false,
)

@Composable
fun PackDetailScreen(
    state: PackDetailUiState,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        heading = "Pack Detail",
        modifier = modifier,
        arguments = listOf("packId" to state.packId),
    )
}

@Preview(name = "Pack detail route", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PackDetailScreenPreview() {
    StickerportTheme(darkTheme = false, dynamicColor = false) {
        PackDetailScreen(
            state = PackDetailUiState(
                packId = "3f1c9a2e-6b54-4d18-9f77-0c2b5a8e41d3",
                name = "PackByPack_Animals",
                stickerCount = 24,
                animated = true,
            ),
        )
    }
}
