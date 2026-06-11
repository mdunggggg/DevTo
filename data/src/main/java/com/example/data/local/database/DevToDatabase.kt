package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ArticleDao
import com.example.data.local.dao.RemoteKeyDao
import com.example.data.local.model.ArticleCacheRef
import com.example.data.local.model.ArticleEntity
import com.example.data.local.model.FlareTagEntity
import com.example.data.local.model.OrganizationEntity
import com.example.data.local.model.RemoteKey
import com.example.data.local.model.UserEntity

@Database(
    entities = [
        ArticleEntity::class,
        FlareTagEntity::class,
        OrganizationEntity::class,
        UserEntity::class,
        RemoteKey::class,
        ArticleCacheRef::class
    ],
    version = 3
)
abstract class DevToDatabase : RoomDatabase() {
    abstract fun articleDao() : ArticleDao
    abstract fun remoteKeyDao() : RemoteKeyDao
    companion object {
        @Volatile
        private var INSTANCE: DevToDatabase? = null

        fun getInstance(context: Context) : DevToDatabase {
            return INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        DevToDatabase::class.java,
                        "dev_to_database"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(false)
                    .build()
                }
                INSTANCE!!
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS remote_keys (
                cache_key TEXT NOT NULL,
                next_page INTEGER,
                PRIMARY KEY(cache_key)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `articles_new` (
                `id` INTEGER NOT NULL,
                `type_of` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `cover_image` TEXT,
                `readable_publish_date` TEXT NOT NULL,
                `social_image` TEXT NOT NULL,
                `tag_list` TEXT NOT NULL,
                `tags` TEXT NOT NULL,
                `slug` TEXT NOT NULL,
                `path` TEXT NOT NULL,
                `url` TEXT NOT NULL,
                `canonical_url` TEXT NOT NULL,
                `comments_count` INTEGER NOT NULL,
                `positive_reactions_count` INTEGER NOT NULL,
                `public_reactions_count` INTEGER NOT NULL,
                `collection_id` INTEGER,
                `created_at` TEXT NOT NULL,
                `edited_at` TEXT,
                `crossposted_at` TEXT,
                `published_at` TEXT NOT NULL,
                `last_comment_at` TEXT NOT NULL,
                `published_timestamp` TEXT NOT NULL,
                `reading_time_minutes` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT OR REPLACE INTO articles_new (
                id, type_of, title, description, cover_image, readable_publish_date,
                social_image, tag_list, tags, slug, path, url, canonical_url,
                comments_count, positive_reactions_count, public_reactions_count,
                collection_id, created_at, edited_at, crossposted_at, published_at,
                last_comment_at, published_timestamp, reading_time_minutes
            )
            SELECT id, type_of, title, description, cover_image, readable_publish_date,
                social_image, tag_list, tags, slug, path, url, canonical_url,
                comments_count, positive_reactions_count, public_reactions_count,
                collection_id, created_at, edited_at, crossposted_at, published_at,
                last_comment_at, published_timestamp, reading_time_minutes
            FROM articles
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `article_cache_refs` (
                `cache_key` TEXT NOT NULL,
                `article_id` INTEGER NOT NULL,
                `position` INTEGER NOT NULL,
                PRIMARY KEY(`cache_key`, `article_id`),
                FOREIGN KEY(`article_id`) REFERENCES `articles_new`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT OR IGNORE INTO article_cache_refs (cache_key, article_id, position)
            SELECT cache_key, id, 0 FROM articles
            """.trimIndent()
        )

        database.execSQL("DROP TABLE articles")
        database.execSQL("ALTER TABLE articles_new RENAME TO articles")

        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_article_cache_refs_article_id` ON `article_cache_refs` (`article_id`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_article_cache_refs_cache_key` ON `article_cache_refs` (`cache_key`)"
        )
    }
}
