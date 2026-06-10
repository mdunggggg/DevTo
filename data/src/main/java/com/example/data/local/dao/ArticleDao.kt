package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.data.local.model.ArticleWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles")
    fun getAllArticle() : Flow<List<ArticleWithRelations>>
}