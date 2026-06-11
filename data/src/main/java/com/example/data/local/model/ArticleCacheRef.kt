package com.example.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "article_cache_refs",
    primaryKeys = ["cache_key", "article_id"],
    foreignKeys = [
        ForeignKey(
            entity = ArticleEntity::class,
            parentColumns = ["id"],
            childColumns = ["article_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("article_id"), Index("cache_key")]
)
data class ArticleCacheRef(
    @ColumnInfo(name = "cache_key")
    val cacheKey: String,
    @ColumnInfo(name = "article_id")
    val articleId: Int,
    @ColumnInfo(name = "position")
    val position: Int
)
