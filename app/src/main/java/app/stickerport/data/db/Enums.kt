package app.stickerport.data.db

import androidx.room.TypeConverter

/**
 * How the source stickers in a set are encoded (spec §11 `stickerFormat`).
 *
 * Stored as the enum **name** string rather than an ordinal. An ordinal silently changes meaning
 * the moment anyone reorders the enum, and every existing row would be reinterpreted with no error
 * and no migration — the worst possible failure mode for a column that decides which encoder runs.
 */
enum class StickerFormat {
    /** Plain PNG/WebP single frames. The only format M1 supports. */
    STATIC,

    /** Telegram's Lottie-based `.tgs`. Sprint 9. */
    TGS,

    /** Telegram's `.webm` video stickers. Sprint 13; alpha is the open risk. */
    WEBM,
}

/** Lifecycle of one import (spec §11 `status`). */
enum class ImportStatus {
    /** The set was fetched and stickers are listed; nothing has been downloaded. */
    FETCHED,
    CONVERTING,
    DONE,
    FAILED,
}

/** Per-sticker conversion state (spec §11 `convStatus`). */
enum class ConversionStatus {
    PENDING,
    DOWNLOADED,
    CONVERTED,
    FAILED,
}

/** Pack lifecycle (spec §11 `PackEntity.status`). */
enum class PackStatus {
    /** Being written; not yet servable. */
    DRAFT,
    /** Complete and servable. */
    READY,
    /**
     * Confirmed present in the messenger's whitelist.
     *
     * This is a **cache of the whitelist check, not a source of truth** (spec §11) — the messenger
     * can be uninstalled or its data cleared at any time, so this is re-verified on app resume.
     * Never gate UI on it.
     */
    ADDED,
}

/**
 * Room converters for the enums above.
 *
 * Explicit rather than relying on Room's built-in enum handling, so the on-disk representation is
 * visible in one place and a future change (for example adding a `WEBM` value) has an obvious home.
 */
class StickerportTypeConverters {

    @TypeConverter
    fun stickerFormatToString(value: StickerFormat): String = value.name

    @TypeConverter
    fun stringToStickerFormat(value: String): StickerFormat =
        StickerFormat.entries.first { it.name == value }

    @TypeConverter
    fun importStatusToString(value: ImportStatus): String = value.name

    @TypeConverter
    fun stringToImportStatus(value: String): ImportStatus =
        ImportStatus.entries.first { it.name == value }

    @TypeConverter
    fun conversionStatusToString(value: ConversionStatus): String = value.name

    @TypeConverter
    fun stringToConversionStatus(value: String): ConversionStatus =
        ConversionStatus.entries.first { it.name == value }

    @TypeConverter
    fun packStatusToString(value: PackStatus): String = value.name

    @TypeConverter
    fun stringToPackStatus(value: String): PackStatus =
        PackStatus.entries.first { it.name == value }
}
