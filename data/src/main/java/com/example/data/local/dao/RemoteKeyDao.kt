package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.model.RemoteKey
import retrofit2.http.GET

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remoteKey: RemoteKey)

    @Query("SELECT * FROM remote_keys WHERE cache_key = :cacheKey")
    suspend fun getRemoteKey(cacheKey: String): RemoteKey?

    @Query("DELETE FROM remote_keys WHERE cache_key = :cacheKey")
    suspend fun deleteByKey(cacheKey: String)
}