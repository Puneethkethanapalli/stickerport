package app.stickerport.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.stickerport.data.db.ImportDao
import app.stickerport.data.db.ImportEntity
import app.stickerport.data.db.ImportStatus
import app.stickerport.data.db.PackDao
import app.stickerport.data.db.StickerDao
import app.stickerport.data.db.StickerportDatabase
import app.stickerport.data.db.StickerFormat
import app.stickerport.data.settings.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * T0.4's other half: "app launches with Hilt without crashing".
 *
 * Injection is the one thing in a DI wiring task that **compiles fine and fails only at runtime**,
 * and the usual cause is a missing graph edge or a missing `android:name` in the manifest. A test
 * that injects every binding the app provides is the cheapest possible guard against both.
 *
 * No `@Config` or `@CustomTestApplication` is needed. `@Config` no longer exists in Hilt 2.60, and
 * `@CustomTestApplication(HiltTestApplication::class)` is actively wrong — `HiltTestApplication`
 * is `final`, so the generated subclass fails to compile. The plain `@HiltAndroidTest` default
 * already substitutes the test application.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltInjectionTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: StickerportDatabase

    @Inject
    lateinit var importDao: ImportDao

    @Inject
    lateinit var stickerDao: StickerDao

    @Inject
    lateinit var packDao: PackDao

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Before
    fun inject() = hiltRule.inject()

    @Test
    fun theDatabaseIsASingleton() {
        val again = EntryPointAccessors
            .fromApplication(ApplicationProvider.getApplicationContext<Context>(), DbEntry::class.java)
            .database()
        assertNotNull(again)
        // Same instance, not merely an equal one: a second instance would mean a second SQLite
        // handle and lost writes.
        assertEquals(database, again)
    }

    @Test
    fun everyDaoIsInjectable() {
        assertNotNull(importDao)
        assertNotNull(stickerDao)
        assertNotNull(packDao)
    }

    /** Proves the real (on-disk) database is usable, not just constructible. */
    @Test
    fun injectedDatabaseAcceptsWrites() = runBlocking {
        val id = importDao.insert(
            ImportEntity(
                telegramSetName = "hilt_check",
                title = "hilt check",
                stickerFormat = StickerFormat.TGS,
                totalCount = 3,
                status = ImportStatus.FETCHED,
                createdAt = 1_700_000_000_000L,
            ),
        )
        assertEquals("hilt_check", importDao.findById(id)!!.telegramSetName)
    }

    /**
     * Settings read through the *real* DataStore, so this asserts the documented defaults rather
     * than a value some earlier test may have written. Note that instrumented tests share the app's
     * private storage, so this touches the actual `settings.preferences_pb` file — T0.5 gives the
     * test suite a temp-file DataStore instead.
     */
    @Test
    fun settingsFallBackToDocumentedDefaults() = runBlocking {
        val settings = settingsRepository.settings.first()

        assertEquals(AppSettingsDefaults.PUBLISHER, settings.publisherName)
        assertEquals(AppSettingsDefaults.MESSENGER_TARGET, settings.messengerTarget)
        assertEquals(AppSettingsDefaults.QUALITY_PRESET, settings.qualityPreset)
        assertEquals(AppSettingsDefaults.THEME, settings.theme)
    }

    @Test
    fun aWriteSurvivesTheRoundTrip() = runBlocking {
        val before = settingsRepository.settings.first().publisherName
        settingsRepository.setPublisherName("Round Trip")
        assertEquals("Round Trip", settingsRepository.settings.first().publisherName)
        // Leave the file as we found it, or the next test run asserts a different default.
        settingsRepository.setPublisherName(before)
    }

    /** Reaches the `SingletonComponent` to check the database really is a singleton. */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DbEntry {
        fun database(): StickerportDatabase
    }
}

/** Duplicated here so the test asserts the *documented* values, not whatever the code returns. */
private object AppSettingsDefaults {
    const val PUBLISHER = "Stickerport"
    const val MESSENGER_TARGET = "Consumer"
    const val QUALITY_PRESET = "Balanced"
    const val THEME = "Follow system"
}
