package com.backlogorder.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.backlogorder.data.local.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {

    @Test
    fun migrate1To2_movesUrlsAndPreservesLocalPaths() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration-test.db"
        context.deleteDatabase(dbName)

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS games (
                                id TEXT NOT NULL PRIMARY KEY,
                                rawg_id INTEGER,
                                name TEXT NOT NULL,
                                background_image TEXT,
                                metacritic INTEGER,
                                rating REAL,
                                playtime_hours REAL,
                                genres TEXT NOT NULL DEFAULT '[]',
                                platforms TEXT NOT NULL DEFAULT '[]',
                                tags TEXT NOT NULL DEFAULT '[]',
                                is_manual INTEGER NOT NULL DEFAULT 0,
                                franchise_name TEXT,
                                franchise_order INTEGER,
                                created_at INTEGER NOT NULL,
                                updated_at INTEGER NOT NULL
                            )
                        """.trimIndent())
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build(),
        )

        val db1 = helper.writableDatabase
        db1.execSQL("""
            INSERT INTO games (id, rawg_id, name, background_image, metacritic, rating, playtime_hours, genres, platforms, tags, is_manual, franchise_name, franchise_order, created_at, updated_at)
            VALUES ('game-http', 1, 'HTTP Game', 'http://example.com/cover.jpg', 80, 4.0, 10, '[]', '[]', '[]', 0, NULL, NULL, 1000, 1000)
        """.trimIndent())
        db1.execSQL("""
            INSERT INTO games (id, rawg_id, name, background_image, metacritic, rating, playtime_hours, genres, platforms, tags, is_manual, franchise_name, franchise_order, created_at, updated_at)
            VALUES ('game-https', 2, 'HTTPS Game', 'https://example.com/cover2.jpg', 90, 4.5, 20, '[]', '[]', '[]', 0, NULL, NULL, 1000, 1000)
        """.trimIndent())
        db1.execSQL("""
            INSERT INTO games (id, rawg_id, name, background_image, metacritic, rating, playtime_hours, genres, platforms, tags, is_manual, franchise_name, franchise_order, created_at, updated_at)
            VALUES ('game-local', NULL, 'Local Game', '/data/user/0/com.backlogorder/files/cover.jpg', NULL, NULL, NULL, '[]', '[]', '[]', 1, NULL, NULL, 1000, 1000)
        """.trimIndent())
        db1.execSQL("""
            INSERT INTO games (id, rawg_id, name, background_image, metacritic, rating, playtime_hours, genres, platforms, tags, is_manual, franchise_name, franchise_order, created_at, updated_at)
            VALUES ('game-null', NULL, 'Null Game', NULL, NULL, NULL, NULL, '[]', '[]', '[]', 1, NULL, NULL, 1000, 1000)
        """.trimIndent())

        MIGRATION_1_2.migrate(db1)

        val cursor = db1.query("SELECT id, background_image, background_image_url FROM games")
        val results = mutableMapOf<String, Pair<String?, String?>>()
        while (cursor.moveToNext()) {
            val id = cursor.getString(0)
            val bgImage = cursor.getString(1)
            val bgUrl = cursor.getString(2)
            results[id] = Pair(bgImage, bgUrl)
        }
        cursor.close()
        helper.close()

        assertEquals(Pair(null, "http://example.com/cover.jpg"), results["game-http"])
        assertEquals(Pair(null, "https://example.com/cover2.jpg"), results["game-https"])
        assertEquals(Pair("/data/user/0/com.backlogorder/files/cover.jpg", null), results["game-local"])
        assertEquals(Pair(null, null), results["game-null"])
    }
}
