package app.stickerport.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.IOException

/**
 * The **MockK** exercise in the catalog: it proves a `DataStore` can be substituted for a repository
 * test, which is what every future unit test of a ViewModel or use case will rely on.
 *
 * `SettingsRepository` takes `Lazy<DataStore<Preferences>>` rather than the store itself precisely
 * so this substitution is easy: MockK can stub `data` and `updateData` on the interface, and the
 * `Lazy` keeps construction cheap.
 *
 * This documents the *technique*. The real behaviour is covered by `SettingsRepositoryTest`, which
 * runs against an actual DataStore file and is the test to trust.
 */
class SettingsRepositoryMockTest {

    private fun mockedDataStore(
        initial: Preferences = emptyPreferences(),
    ): Pair<DataStore<Preferences>, MutableStateFlow<Preferences>> {
        val state = MutableStateFlow(initial)
        val store = mockk<DataStore<Preferences>>()
        coEvery { store.data } returns state
        coEvery { store.updateData(any()) } coAnswers {
            val transform: suspend (Preferences) -> Preferences = firstArg()
            state.value = transform(state.value)
            state.value
        }
        return store to state
    }

    @Test
    fun `the repository reads through an injected DataStore`() = runTest {
        val (store, _) = mockedDataStore()

        SettingsRepository(store).settings.first() shouldBe AppSettings()
    }

    @Test
    fun `writing goes through the injected DataStore`() = runTest {
        val (store, _) = mockedDataStore()
        val repository = SettingsRepository(store)

        repository.setTheme("Dark")

        // Verifies the interaction, not just the result: a test that only checked the resulting
        // value would still pass if the repository wrote to a different store instance.
        // `updateData` takes a transform function, not a `Preferences`, so the captured value is
        // applied to the mock's own state to recover what would have been written.
        val transform = slot<suspend (Preferences) -> Preferences>()
        coVerify(exactly = 1) { store.updateData(capture(transform)) }
        transform.captured(emptyPreferences())[SettingsRepository.KEY_THEME] shouldBe "Dark"
    }

    /**
     * The store must not be *read* at construction.
     *
     * DataStore acquires an exclusive lock on its file when first read. Because Hilt injects
     * `SettingsRepository` as a singleton, reading eagerly would hold the settings file for the
     * whole process lifetime and make a second instance over the same file throw. Deferring the
     * read into the flow is what avoids that — see `SettingsRepository`'s constructor KDoc.
     */
    @Test
    fun `the store is not read until the settings flow is collected`() = runTest {
        val store = mockk<DataStore<Preferences>>()
        var readCount = 0
        val state = MutableStateFlow(emptyPreferences())
        coEvery { store.data } answers { readCount++; state }
        coEvery { store.updateData(any()) } coAnswers {
            val transform: suspend (Preferences) -> Preferences = firstArg()
            state.value = transform(state.value)
            state.value
        }

        val repository = SettingsRepository(store)
        readCount shouldBe 0

        repository.settings.test {
            awaitItem()
            readCount shouldBe 1
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * `SettingsRepository` catches `IOException` from the upstream and falls back to defaults;
     * anything else must propagate. This is the branch that stops a disk error from silently
     * becoming a blank screen — and, just as importantly, the branch that stops it from silently
     * *hiding* a bug.
     */
    @Test
    fun `an IOException from the store falls back to defaults`() = runTest {
        val store = mockk<DataStore<Preferences>>()
        coEvery { store.data } throws IOException("disk gone")

        SettingsRepository(store).settings.first() shouldBe AppSettings()
    }
}
