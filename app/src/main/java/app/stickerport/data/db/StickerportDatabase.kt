package app.stickerport.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The app's Room database — spec §11.
 *
 * ## Migration strategy (T0.4's "first migration strategy noted")
 *
 * **This is schema version 1 and there are no migrations yet.** The policy that starts now:
 *
 * 1. **Schemas are exported and committed** under `app/schemas/` by the Room Gradle plugin. That
 *    JSON is the only reliable record of what a shipped version's schema actually looked like, and
 *    it is what `MigrationTestHelper` migrates between later.
 * 2. **Never bump the version without a `Migration` and a migration test.** A version bump with no
 *    migration means existing users' data is destroyed or the app crashes at launch.
 * 3. **Never ship `fallbackToDestructiveMigration()`.** A silent wipe of a user's converted packs
 *    and their import history is far worse than a failed launch we can diagnose. If a release ever
 *    needs it, it is debug-only and the release build must be able to open the previous schema.
 * 4. **Add columns with a default, never by rewriting the table.** spec §11's
 *    `imageDataVersion` shows the pattern: nullable or defaulted columns keep migrations to
 *    `ALTER TABLE ADD COLUMN`.
 *
 * The first real migration is expected in Sprint 1 or 2, when the Telegram response DTOs settle.
 * Until then `exportSchema = true` is the whole of the export story.
 */
@Database(
    entities = [ImportEntity::class, StickerEntity::class, PackEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(StickerportTypeConverters::class)
abstract class StickerportDatabase : RoomDatabase() {

    abstract fun importDao(): ImportDao
    abstract fun stickerDao(): StickerDao
    abstract fun packDao(): PackDao

    companion object {
        const val NAME = "stickerport.db"
    }
}
