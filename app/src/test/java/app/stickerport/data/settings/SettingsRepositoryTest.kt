package app.stickerport.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * A JVM-side test of [SettingsRepository], with a **real** DataStore backed by a temp file.
 *
 * The instrumented version in `HiltInjectionTest` uses the app's actual `settings.preferences_pb`,
 * so it cannot assert defaults reliably (a previous run may have written a value) and it is slow.
 * This one is hermetic: `PreferenceDataStoreFactory` takes a `produceFile` lambda, so no
 * instrumentation, no shared state, and each test gets a clean directory via `@TempDir`.
 *
 * Libraries exercised here: **JUnit 5** (`@Test`, `runTest`), **Kotest** (`shouldBe`),
 * **Turbine** (collecting the Flow and asserting emissions in order). MockK is exercised in
 * `SettingsRepositoryMockTest`.
 */
class SettingsRepositoryTest {

    @TempDir
    lateinit var tempDir: File

    private fun repositoryOver(
        fileName: String,
        corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    ): SettingsRepository = SettingsRepository(
        PreferenceDataStoreFactory.create(
            corruptionHandler = corruptionHandler,
            produceFile = { File(tempDir, fileName) },
        ),
    )

    private val defaults = AppSettings(
        publisherName = AppSettings.DEFAULT_PUBLISHER,
        messengerTarget = AppSettings.DEFAULT_MESSENGER_TARGET,
        qualityPreset = AppSettings.DEFAULT_QUALITY_PRESET,
        theme = AppSettings.DEFAULT_THEME,
    )

    @Test
    fun `an unwritten store yields the documented defaults`() = runTest {
        repositoryOver("a.preferences_pb").settings.test {
            awaitItem() shouldBe defaults
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a written value is observed`() = runTest {
        val repository = repositoryOver("b.preferences_pb")

        repository.settings.test {
            awaitItem() shouldBe defaults

            repository.setPublisherName("Ada")

            awaitItem().publisherName shouldBe "Ada"
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `every setting round-trips independently`() = runTest {
        val repository = repositoryOver("c.preferences_pb")

        repository.setPublisherName("Grace")
        repository.setMessengerTarget("Business")
        repository.setQualityPreset("Smallest")
        repository.setTheme("Dark")

        repository.settings.test {
            awaitItem() shouldBe AppSettings(
                publisherName = "Grace",
                messengerTarget = "Business",
                qualityPreset = "Smallest",
                theme = "Dark",
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * A single repository sees its own write — enough to prove the value went through DataStore
     * rather than being held in a local field.
     *
     * It deliberately does **not** open a second store over the same file. DataStore holds an
     * exclusive per-file lock for the life of the instance and throws
     * `IllegalStateException: There are multiple DataStores active for the same file` on the
     * second open, in-process or not. Proving survival across a *process restart* needs either a
     * real device or `close()` plus a fresh instance, which is why this coverage lives in the
     * instrumented `HiltInjectionTest` rather than here.
     */
    @Test
    fun `a write is visible to the next read of the same store`() = runTest {
        val repository = repositoryOver("d.preferences_pb")

        repository.setTheme("Light")
        repository.settings.test {
            awaitItem().theme shouldBe "Light"
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a corrupt file falls back to defaults instead of throwing`() = runTest {
        val repository = repositoryOver(
            fileName = "e.preferences_pb",
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        )
        // Pre-seed the file with bytes that are not a valid Preferences protobuf. DataStore throws
        // IOException while decoding these, and SettingsRepository's `catch` is what turns that
        // into "use the defaults" rather than a crash.
        File(tempDir, "e.preferences_pb").writeText("not a preferences protobuf")

        repository.settings.test {
            awaitItem() shouldBe defaults
            cancelAndIgnoreRemainingEvents()
        }
    }
}
