package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.model.ArticleEntity
import com.example.data.local.model.ArticleWithRelations
import com.example.data.local.model.FlareTagEntity
import com.example.data.local.model.OrganizationEntity
import com.example.data.local.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Transaction
    @Query("SELECT * FROM articles")
    fun getAllArticle(): Flow<List<ArticleWithRelations>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(organization: OrganizationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlareTag(flareTag: FlareTagEntity)

    @Transaction
    suspend fun insertArticle(article: ArticleWithRelations) {
        insertArticle(article.article)
        insertUser(article.user)
        article.organization?.let { insertOrganization(it) }
        article.flareTag?.let { insertFlareTag(it) }
    }

    @Query("DELETE FROM articles WHERE id = :articleId")
    suspend fun deleteArticle(articleId: Int)
}
