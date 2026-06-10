package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ArticleDao
import com.example.data.local.model.ArticleEntity
import com.example.data.local.model.FlareTagEntity
import com.example.data.local.model.OrganizationEntity
import com.example.data.local.model.UserEntity

@Database(
    entities = [
        ArticleEntity::class,
        FlareTagEntity::class,
        OrganizationEntity::class,
        UserEntity::class,
    ],
    version = 1
)
abstract class DevToDatabase : RoomDatabase() {
    abstract fun articleDao() : ArticleDao
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
                    .fallbackToDestructiveMigration(false)
                    .build()
                }
                INSTANCE!!
            }
        }
    }
}