package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.paging.PagingSource
import com.example.data.local.model.ArticleCacheRef
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
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(organization: OrganizationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlareTag(flareTag: FlareTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCacheRef(ref: ArticleCacheRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCacheRefs(refs: List<ArticleCacheRef>)

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

    @Transaction
    suspend fun insertArticlesForCacheKey(
        articles: List<ArticleWithRelations>,
        cacheKey: String,
        startPosition: Int
    ) {
        insertArticles(articles)
        insertCacheRefs(
            articles.mapIndexed { index, item ->
                ArticleCacheRef(
                    cacheKey = cacheKey,
                    articleId = item.article.id,
                    position = startPosition + index
                )
            }
        )
    }

    @Query("DELETE FROM article_cache_refs WHERE cache_key = :cacheKey")
    suspend fun deleteByCacheKey(cacheKey: String)

    @Transaction
    @Query(
        """
        SELECT a.* FROM articles AS a
        INNER JOIN article_cache_refs AS r ON r.article_id = a.id
        WHERE r.cache_key = :cacheKey
        ORDER BY r.position ASC
        """
    )
    fun getArticlesByCacheKey(cacheKey: String): PagingSource<Int, ArticleWithRelations>

    @Query("DELETE FROM articles WHERE id = :articleId")
    suspend fun deleteArticle(articleId: Int)

    @Query("DELETE FROM articles WHERE id NOT IN (SELECT article_id FROM article_cache_refs)")
    suspend fun deleteArticlesNotInCache()
}
