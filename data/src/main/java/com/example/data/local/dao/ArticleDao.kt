package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.paging.PagingSource
import com.example.data.local.model.ArticleEntity
import com.example.data.local.model.ArticleWithRelations
import com.example.data.local.model.FlareTagEntity
import com.example.data.local.model.OrganizationEntity
import com.example.data.local.model.UserEntity

@Dao
interface ArticleDao {
    @Transaction
    @Query("SELECT * FROM articles ORDER BY published_timestamp DESC, id DESC")
    fun getArticles(): PagingSource<Int, ArticleWithRelations>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

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

    @Transaction
    suspend fun insertArticles(articles: List<ArticleWithRelations>) {
        articles.forEach { insertArticle(it) }
    }

    @Query("DELETE FROM articles WHERE cache_key = :cacheKey")
    suspend fun deleteByCacheKey(cacheKey: String)

    @Transaction
    @Query("SELECT * FROM articles WHERE cache_key = :cacheKey")
    fun getArticlesByCacheKey(cacheKey: String): PagingSource<Int, ArticleWithRelations>

    @Query("DELETE FROM articles WHERE id = :articleId")
    suspend fun deleteArticle(articleId: Int)
}
