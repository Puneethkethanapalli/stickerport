package app.stickerport.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.stickerport.ui.theme.StickerportTheme

/**
 * Settings — spec §12.6.
 *
 * T0.3 renders the full *list* of rows so the section cannot silently lose one, but the rows are
 * inert. Every interactive row is owned by a later task:
 *
 * | Row | Owner |
 * |---|---|
 * | Publisher name | T4.x (with `PackEntity.publisher`) |
 * | Messenger target | T4.x (with the §12.4 segmented control) |
 * | Quality preset | T2.x (with `BudgetFitter`) |
 * | Bot token | T5.1 (§12.8) |
 * | Theme | T11.x (accessibility pass) |
 * | Licenses / privacy | T14.x (Play readiness) |
 * | Version | read from `BuildConfig` once a build-config plugin lands |
 */
@Immutable
data class SettingsUiState(
    val publisherName: String = "Stickerport",
    val messengerTarget: String = "Consumer",
    val qualityPreset: String = "Balanced",
    /** Only ever the masked form; the plaintext token never enters UI state (§12.8). */
    val maskedToken: String? = null,
    val theme: String = "Follow system",
    val appVersion: String = "0.1.0",
)

/** The rows, in spec order, so the section cannot drift from §12.6 by accident. */
private val SettingRows = listOf(
    "Publisher name" to "Used as the pack author in the messenger",
    "Messenger target" to "Consumer or Business",
    "Quality preset" to "Size budget for each converted sticker",
    "Bot token" to "Required to download stickers from Telegram",
    "Theme" to "Follow the system setting",
    "Open-source licences" to "Third-party notices",
    "Privacy policy" to "Opens in your browser",
    "Version" to "0.1.0",
)

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    modifier: Modifier = Modifier,
    onRowClick: (String) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        SettingRows.forEachIndexed { index, (label, summary) ->
            if (index > 0) {
                HorizontalDivider()
            }
            SettingRow(
                label = label,
                summary = if (label == "Version") state.appVersion else summary,
                onClick = { onRowClick(label) },
            )
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    summary: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // T0.3: a real click would be `Modifier.clickable(onClick = onClick)`. Left as a
            // plain Row so the skeleton cannot look interactive when it is not.
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Settings", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SettingsScreenPreview() {
    StickerportTheme(darkTheme = false, dynamicColor = false) {
        SettingsScreen(state = SettingsUiState())
    }
}

@Preview(name = "Settings — token set", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SettingsScreenTokenPreview() {
    StickerportTheme(darkTheme = true, dynamicColor = false) {
        SettingsScreen(state = SettingsUiState(maskedToken = "123456789:ABC•••"))
    }
}
