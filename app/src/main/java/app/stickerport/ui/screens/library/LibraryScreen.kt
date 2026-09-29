package app.stickerport.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.stickerport.ui.theme.StickerportTheme

/** One row in the Library list (§12.5). */
@Immutable
data class LibraryItemUi(
    val importId: Long,
    val title: String,
    /** Mirrors `ImportEntity.status`, for the chip. */
    val statusLabel: String,
    val stickerCount: Int,
    val packCount: Int,
)

/** Library — spec §12.5. Search and list chrome only in T0.3; data arrives with T1.2. */
@Immutable
data class LibraryUiState(
    val query: String = "",
    val items: List<LibraryItemUi> = emptyList(),
    val isLoading: Boolean = false,
)

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit = {},
    onOpen: (Long) -> Unit = {},
    onDelete: (Long) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // §12.5 search by name. The real filtering is T1.2, once there is something to filter.
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            singleLine = true,
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth(),
        )

        when {
            state.isLoading -> Text(
                text = "Loading…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            state.items.isEmpty() -> Text(
                // §12.7: an empty state is a designed thing, not a blank screen or a toast.
                text = "Nothing here yet. Import a Telegram sticker pack from Home and it will " +
                    "show up in this list.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            else -> state.items.forEach { item ->
                Text(
                    text = "${item.title} — ${item.stickerCount} stickers, ${item.packCount} packs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Preview(name = "Library — empty", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun LibraryEmptyPreview() {
    StickerportTheme(darkTheme = false, dynamicColor = false) {
        LibraryScreen(state = LibraryUiState())
    }
}

@Preview(name = "Library — with items", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun LibraryItemsPreview() {
    StickerportTheme(darkTheme = true, dynamicColor = false) {
        LibraryScreen(
            state = LibraryUiState(
                items = listOf(
                    LibraryItemUi(1L, "PackByPack_Animals", "DONE", 48, 2),
                    LibraryItemUi(2L, "CuteCats", "CONVERTING", 12, 0),
                ),
            ),
        )
    }
}
