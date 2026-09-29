package app.stickerport.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One Telegram sticker set being (or having been) converted — spec §11.
 *
 * `id` is a Room-generated `Long` because it is a local, insert-and-reference row. `PackEntity.id`
 * is the opposite: a stable UUID, because a pack has to keep its identity across the app being
 * reinstalled and its files being re-derived.
 */
@Entity(
    tableName = "imports",
    indices = [Index("createdAt")],
)
data class ImportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * The bare Telegram set name, e.g. `PackByPack_Animals`.
     *
     * Deliberately **not** the URL the user pasted. spec §14 forbids logging or persisting full
     * Telegram links, and the name is all that `getStickerSet` needs.
     */
    val telegramSetName: String,

    /** User-editable display title; defaults to [telegramSetName] (spec §12.2). */
    val title: String,

    val stickerFormat: StickerFormat,

    val totalCount: Int,

    val status: ImportStatus,

    /** Epoch millis. Indexed because Home shows "the last 5 imports" (spec §12.1). */
    val createdAt: Long,
)

/**
 * One sticker within an import — spec §11.
 *
 * @param orderIndex position within the set, 0-based. Preserves Telegram's ordering, which the
 *   user sees in the Preview grid and expects to survive into the converted pack.
 * @param packId assigned after chunking (spec §11). Nullable because a sticker is created long
 *   before a pack exists. See the foreign key for why deleting a pack nulls it rather than
 *   deleting the sticker.
 */
@Entity(
    tableName = "stickers",
    foreignKeys = [
        ForeignKey(
            entity = ImportEntity::class,
            parentColumns = ["id"],
            childColumns = ["importId"],
            // spec §11: "Deleting an Import cascades to Stickers/Packs".
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            // Deleting one pack must not delete the stickers: the import still owns them and the
            // user may re-chunk. Nulling the assignment is the honest representation of "this
            // sticker is not in a pack right now".
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    // Room requires every foreign-key column to be indexed or it emits a warning (and a query
    // planner that table-scans).
    indices = [Index("importId"), Index("packId")],
)
data class StickerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val importId: Long,

    val orderIndex: Int,

    val telegramFileId: String,

    /** Stable across re-uploads; used as the cache key for the thumbnail (spec §12.2). */
    val fileUniqueId: String,

    val emoji: String?,

    val sourceFormat: StickerFormat,

    /** spec §12.2: tap toggles inclusion; dimmed means excluded. */
    val selected: Boolean,

    val convStatus: ConversionStatus,

    /** Why this sticker failed. Null unless [convStatus] is `FAILED` (spec §12.3 partial failure). */
    val failReason: String? = null,

    /** Path under `filesDir/packs/`, e.g. `packs/<packId>/01.webp` (spec §11). */
    val outputFile: String? = null,

    val outputBytes: Long? = null,

    @ColumnInfo(defaultValue = "NULL")
    val packId: String? = null,
)

/**
 * One output pack ready to be handed to the messenger — spec §11.
 *
 * @param id a **stable UUID string**, not a row offset. The pack's identity has to survive the
 *   database being rebuilt, because the files on disk are keyed by it
 *   (`filesDir/packs/<packId>/…`) and the messenger's whitelist is keyed by tray + name.
 * @param imageDataVersion the messenger's tray `imageDataVersion` (spec §11). It has to be written
 *   into the tray PNG so a re-add after a tray change is actually detected.
 */
@Entity(
    tableName = "packs",
    foreignKeys = [
        ForeignKey(
            entity = ImportEntity::class,
            parentColumns = ["id"],
            childColumns = ["importId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("importId"), Index(value = ["name"])],
)
data class PackEntity(
    @PrimaryKey
    val id: String,

    val importId: Long,

    val name: String,

    val publisher: String,

    val animated: Boolean,

    /** `packs/<packId>/tray.png` (spec §11). */
    val trayFile: String,

    val imageDataVersion: Int,

    val status: PackStatus,

    /**
     * The Telegram link this pack came from.
     *
     * spec §14 says full Telegram links must not be logged. This is a *stored user-facing value*
     * (the spec explicitly lists it in the model and in Pack Detail), not a log line, so it is
     * kept — but nothing may ever print it. T14.x re-checks this before release.
     */
    val sourceLink: String,

    val createdAt: Long,
)
