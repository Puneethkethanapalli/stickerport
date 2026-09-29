package app.stickerport.data.db

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the Room database and its DAOs.
 *
 * Lives next to the database rather than in a separate `di/` package because spec §7.2 fixes the
 * package layout and has no `di` entry. Each module sits with the thing it wires, which keeps the
 * layout requirement and the discoverability of the module in tension only in a harmless way.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): StickerportDatabase = androidx.room.Room
        .databaseBuilder(context, StickerportDatabase::class.java, StickerportDatabase.NAME)
        // No fallbackToDestructiveMigration — see StickerportDatabase's KDoc. A version bump with
        // no migration must fail loudly in development, not wipe a user's packs in production.
        .build()

    @Provides
    fun provideImportDao(db: StickerportDatabase): ImportDao = db.importDao()

    @Provides
    fun provideStickerDao(db: StickerportDatabase): StickerDao = db.stickerDao()

    @Provides
    fun providePackDao(db: StickerportDatabase): PackDao = db.packDao()
}
