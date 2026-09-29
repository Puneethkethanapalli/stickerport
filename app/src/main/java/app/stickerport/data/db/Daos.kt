package app.stickerport.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * The three DAOs of spec §11, kept deliberately thin.
 *
 * T0.4's acceptance criterion is "inserts and reads one row per entity", so these expose exactly
 * what a screen needs and nothing speculative. Pagination, search (spec §12.5) and Flow-based
 * invalidation arrive with the tasks that own them (T1.2, T4.1).
 */
@Dao
interface ImportDao {

    @Insert
    suspend fun insert(import: ImportEntity): Long

    @Update
    suspend fun update(import: ImportEntity)

    @Query("SELECT * FROM imports WHERE id = :id")
    suspend fun findById(id: Long): ImportEntity?

    /** spec §12.1: Home's "Recent" section shows the last 5 imports, newest first. */
    @Query("SELECT * FROM imports ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ImportEntity>>

    @Query("DELETE FROM imports WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface StickerDao {

    @Insert
    suspend fun insertAll(stickers: List<StickerEntity>): List<Long>

    @Insert
    suspend fun insert(sticker: StickerEntity): Long

    @Query("SELECT * FROM stickers WHERE importId = :importId ORDER BY orderIndex")
    fun observeForImport(importId: Long): Flow<List<StickerEntity>>

    @Query("SELECT * FROM stickers WHERE packId = :packId ORDER BY orderIndex")
    fun observeForPack(packId: String): Flow<List<StickerEntity>>

    @Query("SELECT COUNT(*) FROM stickers WHERE importId = :importId")
    suspend fun countForImport(importId: Long): Int
}

@Dao
interface PackDao {

    @Insert
    suspend fun insert(pack: PackEntity)

    @Query("SELECT * FROM packs WHERE id = :id")
    suspend fun findById(id: String): PackEntity?

    @Query("SELECT * FROM packs WHERE importId = :importId ORDER BY createdAt")
    fun observeForImport(importId: Long): Flow<List<PackEntity>>

    @Query("UPDATE stickers SET packId = :packId WHERE id IN (:stickerIds)")
    suspend fun assignToPack(packId: String, stickerIds: List<Long>)

    @Query("DELETE FROM packs WHERE id = :id")
    suspend fun deleteById(id: String)
}
