package app.stickerport.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Home's ViewModel (spec §7: "UI observes `StateFlow<UiState>`; user actions are events to the
 * ViewModel").
 *
 * ## Why this has no dependencies yet
 * The state Home can honestly derive today is "the text the user has typed". Everything else is
 * missing infrastructure, not missing code:
 *
 * - **Recent imports** need Room rows (T1.2 writes them).
 * - **Has a bot token** needs the encrypted `TokenStore` (T1.3). `SettingsRepository` deliberately
 *   does *not* hold the token (spec §6 requires Keystore-encrypted storage, which DataStore
 *   Preferences cannot provide), so there is no signal to read yet.
 *
 * Pretending otherwise — deriving `hasBotToken` from an unrelated preference, or faking a recents
 * list — would make the skeleton lie about the app's state, and every later task would then have to
 * unlearn the assumption. `@HiltViewModel` with no constructor arguments still exercises the whole
 * Hilt + Navigation + ViewModel path, which is what T0.4 is actually verifying.
 *
 * The parameters T1.2 and T1.3 add are noted here so the next task knows exactly where they land.
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val linkText = MutableStateFlow("")

    /**
     * spec §12.1: "Until a valid token exists, Home shows a 'Set up your bot token' banner and the
     * paste field is disabled." Always true at present, so the banner always shows.
     *
     * T1.3 replaces this with a read of the encrypted token's presence.
     */
    private val hasBotToken = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        linkText,
        hasBotToken,
    ) { text, tokenPresent ->
        HomeUiState(
            linkText = text,
            // T1.2 replaces this with `importDao.observeRecent(5)`.
            recents = emptyList(),
            hasBotToken = tokenPresent,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = HomeUiState(),
    )

    /** UI event, spec §7. T1.1 adds the §8.1 validation. */
    fun onLinkTextChange(value: String) {
        linkText.value = value
    }

    private companion object {
        /**
         * 5 s. Long enough that a configuration change does not tear down and recreate the
         * ViewModel mid-flow; short enough that a backgrounded app releases its upstream flows.
         */
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
