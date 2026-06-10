package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ArticleDao
import com.example.data.local.dao.RemoteKeyDao
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
        RemoteKey::class
    ],
    version = 2
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
                    .addMigrations(MIGRATION_1_2)
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