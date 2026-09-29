package app.stickerport.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T0.4's acceptance test: "Room in-memory instrumented test inserts and reads one row per entity".
 *
 * Beyond the round trip, this pins the two decisions that are invisible until they are wrong:
 * enums round-trip **by name** (not ordinal), and the foreign keys do what spec §11 says on delete.
 * Both are cheap to assert now and expensive to discover after users have data.
 */
@RunWith(AndroidJUnit4::class)
class StickerportDatabaseTest {

    private lateinit var db: StickerportDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StickerportDatabase::class.java,
        ).build()

        // Foreign keys are OFF by default in SQLite and Room turns them on for us. Assert that,
        // because the cascade tests below would otherwise pass for the wrong reason: with FKs off,
        // SQLite silently ignores the ON DELETE clauses and nothing cascades.
        db.openHelper.writableDatabase
            .query("PRAGMA foreign_keys")
            .use { cursor ->
                assertTrue("cursor should have one row", cursor.moveToFirst())
                assertEquals("foreign key enforcement must be on", 1, cursor.getInt(0))
            }
    }

    @After
    fun closeDb() = db.close()

    private fun sampleImport(name: String = "PackByPack_Animals") = ImportEntity(
        telegramSetName = name,
        title = name,
        stickerFormat = StickerFormat.STATIC,
        totalCount = 48,
        status = ImportStatus.FETCHED,
        createdAt = 1_700_000_000_000L,
    )

    private fun samplePack(importId: Long, id: String = PACK_ID) = PackEntity(
        id = id,
        importId = importId,
        name = "PackByPack_Animals",
        publisher = "Stickerport",
        animated = false,
        trayFile = "packs/$id/tray.png",
        imageDataVersion = 1,
        status = PackStatus.READY,
        sourceLink = "https://t.me/addstickers/animals",
        createdAt = 1_700_000_000_000L,
    )

    private fun sampleSticker(
        importId: Long,
        order: Int = 0,
        convStatus: ConversionStatus = ConversionStatus.PENDING,
    ) = StickerEntity(
        importId = importId,
        orderIndex = order,
        telegramFileId = "AgACfile$order",
        fileUniqueId = "AgACunique$order",
        emoji = "🐶",
        sourceFormat = StickerFormat.STATIC,
        selected = true,
        convStatus = convStatus,
    )

    // ---- one row per entity, in and out ----

    @Test
    fun importRoundTrips() = runBlocking {
        val id = db.importDao().insert(sampleImport())
        assertTrue("autoGenerate should assign a positive id", id > 0)

        val read = db.importDao().findById(id)
        assertNotNull(read)
        assertEquals("PackByPack_Animals", read!!.telegramSetName)
        assertEquals(StickerFormat.STATIC, read.stickerFormat)
        assertEquals(ImportStatus.FETCHED, read.status)
        assertEquals(48, read.totalCount)
    }

    @Test
    fun stickerRoundTrips() = runBlocking {
        val importId = db.importDao().insert(sampleImport())
        val stickerId = db.stickerDao().insert(sampleSticker(importId))

        val read = db.stickerDao().observeForImport(importId).first().single()
        assertEquals(stickerId, read.id)
        assertEquals("🐶", read.emoji)
        assertEquals(ConversionStatus.PENDING, read.convStatus)
        assertNull(read.failReason)
        assertNull(read.packId)
    }

    @Test
    fun packRoundTrips() = runBlocking {
        val importId = db.importDao().insert(sampleImport())
        db.packDao().insert(samplePack(importId))

        val read = db.packDao().findById(PACK_ID)
        assertNotNull(read)
        assertEquals(PACK_ID, read!!.id)
        assertEquals(PackStatus.READY, read.status)
        assertEquals(1, read.imageDataVersion)
        assertTrue(!read.animated)
    }

    /**
     * The reason the enums are stored as names. If someone "optimises" the converters to
     * `value.ordinal`, this test fails instead of silently reinterpreting every existing row.
     */
    @Test
    fun everyEnumValueSurvivesARoundTrip() = runBlocking {
        StickerFormat.entries.forEach { format ->
            val id = db.importDao().insert(sampleImport(name = "set_${format.name}").copy(stickerFormat = format))
            assertEquals(format, db.importDao().findById(id)!!.stickerFormat)
        }
        ImportStatus.entries.forEach { status ->
            val id = db.importDao().insert(sampleImport(name = "set_${status.name}").copy(status = status))
            assertEquals(status, db.importDao().findById(id)!!.status)
        }
        val importId = db.importDao().insert(sampleImport())
        ConversionStatus.entries.forEach { conv ->
            val stickerId = db.stickerDao().insert(
                sampleSticker(importId, order = conv.ordinal, convStatus = conv),
            )
            val read = db.stickerDao().observeForImport(importId).first()
                .first { it.id == stickerId }
            assertEquals(conv, read.convStatus)
        }
        PackStatus.entries.forEach { status ->
            val id = "$PACK_ID$status"
            db.packDao().insert(samplePack(importId, id).copy(status = status))
            assertEquals(status, db.packDao().findById(id)!!.status)
        }
    }

    // ---- the two referential rules spec §11 states ----

    /** "Deleting an Import cascades to Stickers/Packs." */
    @Test
    fun deletingAnImportCascadesToStickersAndPacks() = runBlocking {
        val importId = db.importDao().insert(sampleImport())
        db.packDao().insert(samplePack(importId))
        db.stickerDao().insert(sampleSticker(importId))
        assertEquals(1, db.stickerDao().countForImport(importId))

        db.importDao().deleteById(importId)

        assertEquals(0, db.stickerDao().countForImport(importId))
        assertNull("pack should have cascaded away", db.packDao().findById(PACK_ID))
    }

    /**
     * Deleting a pack must **not** delete its stickers — the import still owns them and the user
     * may re-chunk. The pack reference is nulled instead.
     */
    @Test
    fun deletingAPackNullsStickerPackIdInsteadOfDeletingThem() = runBlocking {
        val importId = db.importDao().insert(sampleImport())
        db.packDao().insert(samplePack(importId))
        val stickerId = db.stickerDao().insert(sampleSticker(importId))
        db.packDao().assignToPack(PACK_ID, listOf(stickerId))
        assertEquals(
            PACK_ID,
            db.stickerDao().observeForImport(importId).first().single().packId,
        )

        db.packDao().deleteById(PACK_ID)

        val sticker = db.stickerDao().observeForImport(importId).first().single()
        assertEquals("sticker must survive its pack", stickerId, sticker.id)
        assertNull("pack reference must be cleared", sticker.packId)
    }

    /** spec §12.1: Home shows the last 5 imports, newest first. */
    @Test
    fun recentImportsAreNewestFirstAndCapped() = runBlocking {
        repeat(7) { i ->
            db.importDao().insert(sampleImport(name = "set_$i").copy(createdAt = 1_000L + i))
        }

        val recent = db.importDao().observeRecent(limit = 5).first()

        assertEquals(5, recent.size)
        assertEquals(listOf("set_6", "set_5", "set_4", "set_3", "set_2"), recent.map { it.telegramSetName })
    }

    private companion object {
        const val PACK_ID = "3f1c9a2e-6b54-4d18-9f77-0c2b5a8e41d3"
    }
}
