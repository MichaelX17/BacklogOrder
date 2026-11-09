package com.backlogorder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ListEntity::class, GameEntity::class, ListGameEntity::class, SettingsEntity::class],
    version = 1,
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
        ).fallbackToDestructiveMigration().build()
    }
}
