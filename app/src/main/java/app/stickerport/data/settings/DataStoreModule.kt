package app.stickerport.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The single [DataStore] for app settings (spec §6: "DataStore (settings)").
 *
 * The `preferencesDataStore` delegate is top-level and `private`, which is deliberate: the
 * DataStore docs are explicit that creating two instances for the same file throws
 * `IllegalStateException` at runtime, and the only reliable way to prevent that is to make
 * construction impossible from anywhere else. Everything goes through [provideDataStore].
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SettingsRepository.STORE_NAME,
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.settingsDataStore
}
