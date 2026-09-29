package app.stickerport.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.stickerport.ui.theme.StickerportTheme

/** One row in Home's "Recent" section (§12.1: the last 5 imports with status chips). */
@Immutable
data class RecentImportUi(
    val importId: Long,
    val title: String,
    /** Mirrors `ImportEntity.status`: FETCHED | CONVERTING | DONE | FAILED. */
    val statusLabel: String,
    val stickerCount: Int,
)

/**
 * Home — spec §12.1.
 *
 * Stateless by design: it renders [HomeUiState] and raises events. The `ViewModel` that owns that
 * state arrives with T0.4 (Hilt) and T4.1 (the real screen); until then the `NavHost` holds the
 * state in a `remember`, which is enough to exercise navigation.
 */
@Immutable
data class HomeUiState(
    /**
     * The raw text in the paste field.
     *
     * §12.1 says this validates as the user types using §8.1. T1.1 owns the parser, so until then
     * the field is a plain text holder and [onSubmitLink] receives whatever was typed.
     */
    val linkText: String = "",
    val recents: List<RecentImportUi> = emptyList(),
    /**
     * §12.1: "Until a valid token exists, Home shows a 'Set up your bot token' banner and the
     * paste field is disabled." True in the skeleton because there is no token store yet.
     */
    val hasBotToken: Boolean = false,
    val isSubmitting: Boolean = false,
)

@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onLinkTextChange: (String) -> Unit = {},
    onSubmitLink: (String) -> Unit = {},
    onOpenRecent: (Long) -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    onNavigateToRoute: (SkeletonRoute) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // §12.1: "Until a valid token exists, Home shows a 'Set up your bot token' banner and the
        // paste field is disabled."
        if (!state.hasBotToken) {
            Text(
                text = "Set up your bot token to start importing.",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }

        OutlinedTextField(
            value = state.linkText,
            onValueChange = onLinkTextChange,
            singleLine = true,
            enabled = state.hasBotToken && !state.isSubmitting,
            label = { Text("Paste a Telegram sticker link") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { onSubmitLink(state.linkText) },
            enabled = state.hasBotToken && !state.isSubmitting && state.linkText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Go")
        }

        OutlinedButton(
            onClick = onOpenLibrary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Library")
        }

        // §12.1: "Section: Recent (last 5 imports with status chips) → Library."
        if (state.recents.isEmpty()) {
            Text(
                // §12.7: the empty state is a designed thing, not a blank area. T4.1 replaces this
                // text with the 3-step illustration (Open a pack → Share to Stickerport → Add).
                text = "Open a pack in Telegram, share it to Stickerport, then add the " +
                    "converted pack to your messenger.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        } else {
            Text(
                text = "Recent",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            state.recents.take(MAX_RECENTS).forEach { recent ->
                OutlinedButton(
                    onClick = { onOpenRecent(recent.importId) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "${recent.title} · ${recent.statusLabel} · " +
                            "${recent.stickerCount}",
                    )
                }
            }
        }

        SkeletonRoutes(onNavigate = onNavigateToRoute, onOpenOnboarding = onOpenOnboarding)
    }
}

/** §12.1 caps Recent at the last 5 imports. */
private const val MAX_RECENTS = 5

/**
 * ## TEMPORARY — delete in T0.3's follow-up, or at the latest when T4.1 builds the real Home.
 *
 * Every screen in this graph needs a *specific* set of arguments to be reachable, and on a fresh
 * install there is no Room row and no Telegram link to produce them. Without these chips, four of
 * the eight routes could only be reached from code, and "manual navigation through every route"
 * — this task's own acceptance test — would not actually be possible by hand.
 *
 * They are rendered as an explicit, labelled section rather than hidden behind a debug menu so
 * they cannot be mistaken for part of the product.
 */
@Composable
private fun SkeletonRoutes(
    onNavigate: (SkeletonRoute) -> Unit,
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "SKELETON — temporary route links",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonRoute.entries.forEach { route ->
                AssistChip(
                    onClick = { onNavigate(route) },
                    label = { Text(route.label) },
                )
            }
            AssistChip(
                onClick = onOpenOnboarding,
                label = { Text("Onboarding") },
            )
        }
    }
}

/** The routes that have no natural entry point yet, with the sample arguments to reach them. */
enum class SkeletonRoute(val label: String) {
    PREVIEW("Preview"),
    PROGRESS("Progress"),
    RESULT("Result"),
    PACK_DETAIL("PackDetail"),
}

@Preview(name = "Home — empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeEmptyPreview() {
    StickerportTheme(darkTheme = false, dynamicColor = false) {
        HomeScreen(state = HomeUiState())
    }
}

@Preview(name = "Home — token set, with recents", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeRecentsPreview() {
    StickerportTheme(darkTheme = true, dynamicColor = false) {
        HomeScreen(
            state = HomeUiState(
                linkText = "t.me/addstickers/animals",
                hasBotToken = true,
                recents = listOf(
                    RecentImportUi(1L, "PackByPack_Animals", "DONE", 48),
                    RecentImportUi(2L, "CuteCats", "CONVERTING", 12),
                ),
            ),
        )
    }
}
