package com.backlogorder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// One-shot migration. Runs on every existing install. Must be correct; test against a v1 database with representative data before shipping.
// Note: RAWG games whose covers had already downloaded before this migration lost their URL as it was not previously stored.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE games ADD COLUMN background_image_url TEXT",
        )
        // Move any URL-shaped values out of background_image into the new column
        db.execSQL(
            """
            UPDATE games
            SET background_image_url = background_image,
                background_image = NULL
            WHERE background_image LIKE 'http://%'
               OR background_image LIKE 'https://%'
            """.trimIndent(),
        )
    }
}

@Database(
    entities = [ListEntity::class, GameEntity::class, ListGameEntity::class, SettingsEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(StringListConverter::class)
abstract class BacklogOrderDatabase : RoomDatabase() {
    abstract fun listDao(): ListDao
    abstract fun gamesDao(): GamesDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        fun create(context: Context): BacklogOrderDatabase = Room.databaseBuilder(
            context.applicationContext,
            BacklogOrderDatabase::class.java,
            "backlogorder.db",
        ).addMigrations(MIGRATION_1_2).build()
    }
}
