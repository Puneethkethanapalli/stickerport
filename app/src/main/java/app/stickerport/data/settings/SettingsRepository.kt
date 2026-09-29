package app.stickerport.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** The non-secret settings of spec §12.6, with their defaults. */
data class AppSettings(
    val publisherName: String = DEFAULT_PUBLISHER,
    val messengerTarget: String = DEFAULT_MESSENGER_TARGET,
    val qualityPreset: String = DEFAULT_QUALITY_PRESET,
    val theme: String = DEFAULT_THEME,
) {
    companion object {
        const val DEFAULT_PUBLISHER = "Stickerport"
        const val DEFAULT_MESSENGER_TARGET = "Consumer"
        const val DEFAULT_QUALITY_PRESET = "Balanced"
        const val DEFAULT_THEME = "Follow system"
    }
}

/**
 * Settings over DataStore (spec §11 / §12.6). A **stub** in T0.4: real values are written by T4.x
 * (publisher, messenger target), T2.x (quality preset) and T11.x (theme).
 *
 * ## Where the bot token is *not*
 * The Telegram bot token is deliberately absent from here. spec §6 requires it to be encrypted with
 * an Android Keystore AES-GCM key before it is stored, which DataStore Preferences cannot do, so
 * it lives in a separate `TokenStore` owned by T1.3. Putting it here and encrypting later would
 * mean plaintext tokens on disk in the meantime, and a half-migrated token file on every upgrade.
 */
@Singleton
class SettingsRepository @Inject constructor(
    // The store arrives as a plain `DataStore<Preferences>`; the `flow { }` below is what defers
    // the first *read* until collection, which is when DataStore actually opens and locks the file.
    // Wrapping it in a `Provider` or `dagger.Lazy` would be the more obvious way to be lazy, and
    // both are traps here:
    //
    //  - `dagger.Lazy` has no built-in Hilt binding, and a hand-written `@Provides` for it is
    //    rejected outright: "@Provides methods must not return framework types".
    //  - `Provider<T>` gets past that check but then trips Dagger's own "multiple bindings" or
    //    leaves the file acquired at injection time anyway.
    //
    // Deferring inside the flow is the version that actually holds the lock for the shortest time
    // and needs no extra binding.
    private val dataStore: DataStore<Preferences>,
) {

    /**
     * The current settings, as a stream.
     *
     * ## Why the body is wrapped in `flow { }`
     * The obvious `dataStore.data.map { … }` looks equivalent and is **not**. Property
     * initialisers run at construction, so `.data` would be touched the moment the repository is
     * created: the settings file would be opened on first injection rather than on first read, and
     * an `IOException` from a corrupt file would escape from the *constructor*, where nothing can
     * catch it and no `Flow` collector exists yet.
     *
     * Deferring both into the flow makes the `catch` able to see a failure that happens while
     * acquiring the store, which is exactly the case it exists for.
     *
     * The [catch] is not defensive noise: a DataStore read throws `IOException` if the underlying
     * file is unreadable or corrupt, and an uncaught exception in a `Flow` read kills the collector
     * for good. Falling back to defaults keeps the app usable and the user can re-enter settings.
     */
    val settings: Flow<AppSettings> = flow {
        dataStore.data.collect { prefs -> emit(prefs.toAppSettings()) }
    }.catch { cause ->
        if (cause is IOException) {
            emit(emptyPreferences().toAppSettings())
        } else {
            throw cause
        }
    }

    suspend fun setPublisherName(value: String) = put(KEY_PUBLISHER, value)

    suspend fun setMessengerTarget(value: String) = put(KEY_MESSENGER_TARGET, value)

    suspend fun setQualityPreset(value: String) = put(KEY_QUALITY_PRESET, value)

    suspend fun setTheme(value: String) = put(KEY_THEME, value)

    private suspend fun put(key: Preferences.Key<String>, value: String) {
        dataStore.edit { it[key] = value }
    }

    private fun Preferences.toAppSettings() = AppSettings(
        publisherName = this[KEY_PUBLISHER] ?: AppSettings.DEFAULT_PUBLISHER,
        messengerTarget = this[KEY_MESSENGER_TARGET] ?: AppSettings.DEFAULT_MESSENGER_TARGET,
        qualityPreset = this[KEY_QUALITY_PRESET] ?: AppSettings.DEFAULT_QUALITY_PRESET,
        theme = this[KEY_THEME] ?: AppSettings.DEFAULT_THEME,
    )

    companion object {
        /** Referenced by `DataStoreModule`'s delegate, which is top-level and private. */
        const val STORE_NAME = "settings"

        val KEY_PUBLISHER = stringPreferencesKey("publisher_name")
        val KEY_MESSENGER_TARGET = stringPreferencesKey("messenger_target")
        val KEY_QUALITY_PRESET = stringPreferencesKey("quality_preset")
        val KEY_THEME = stringPreferencesKey("theme")
    }
}
