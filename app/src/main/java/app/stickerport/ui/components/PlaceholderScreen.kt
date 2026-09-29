package app.stickerport.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.stickerport.ui.theme.StickerportTheme

/**
 * The body used by every screen that is a declared route but not yet built.
 *
 * It deliberately renders its own **route arguments**, so a screenshot or a UI test proves that
 * arguments actually survived the trip through the back stack. A placeholder that only said
 * "TODO" would not have caught a `toRoute<T>()` mistake — which is the main thing T0.3 needs to
 * verify.
 *
 * ## TEMPORARY
 * Delete this and its call sites as each screen is built. If it is still here at the Sprint 0
 * gate, a route was never implemented.
 */
@Composable
fun PlaceholderScreen(
    heading: String,
    modifier: Modifier = Modifier,
    arguments: List<Pair<String, String>> = emptyList(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        if (arguments.isEmpty()) {
            Text(
                text = "No route arguments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            arguments.forEach { (name, value) ->
                Text(
                    text = "$name = $value",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(name = "Placeholder", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PlaceholderScreenPreview() {
    StickerportTheme(darkTheme = false, dynamicColor = false) {
        PlaceholderScreen(
            heading = "Coming in a later sprint",
            arguments = listOf("setName" to "PackByPack_Animals"),
        )
    }
}
