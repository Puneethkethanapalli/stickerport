package app.stickerport.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.stickerport.ui.theme.StickerportTheme

/**
 * First-run onboarding — spec §12.1. Built for real in Sprint 5 (T5.x) alongside §12.8.
 *
 * The three steps are listed as plain text here; T5.1 replaces them with the designed
 * 3-step illustration and a real stepper.
 */
@Immutable
data class OnboardingUiState(
    val whatsappInstalled: Boolean? = null,
    val isCheckingInstallation: Boolean = false,
)

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
    onCheckInstallation: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Stickerport turns Telegram sticker packs into packs you can add to your " +
                "messenger. It needs the messenger app to be installed, and it needs your own " +
                "Telegram bot token so it can download sticker files.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = when {
                state.isCheckingInstallation -> "Checking…"
                state.whatsappInstalled == null -> "Messenger not checked yet."
                state.whatsappInstalled -> "Messenger found — good to go."
                else -> "Messenger not found. Install it, then check again."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Get started")
        }
    }
}

@Preview(name = "Onboarding", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun OnboardingScreenPreview() {
    StickerportTheme(darkTheme = false, dynamicColor = false) {
        OnboardingScreen(state = OnboardingUiState(whatsappInstalled = true))
    }
}

@Preview(name = "Onboarding — messenger missing", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun OnboardingScreenMissingPreview() {
    StickerportTheme(darkTheme = true, dynamicColor = false) {
        OnboardingScreen(state = OnboardingUiState(whatsappInstalled = false))
    }
}
