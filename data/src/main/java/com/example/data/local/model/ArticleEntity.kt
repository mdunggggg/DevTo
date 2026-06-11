package com.example.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.data.local.converter.StringListConverter

@Entity(tableName = "articles")
@TypeConverters(StringListConverter::class)
data class ArticleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "type_of")
    val typeOf: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "cover_image")
    val coverImage: String?,
    @ColumnInfo(name = "readable_publish_date")
    val readablePublishDate: String,
    @ColumnInfo(name = "social_image")
    val socialImage: String,
    @ColumnInfo(name = "tag_list")
    val tagList: List<String>,
    @ColumnInfo(name = "tags")
    val tags: String,
    @ColumnInfo(name = "slug")
    val slug: String,
    @ColumnInfo(name = "path")
    val path: String,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "canonical_url")
    val canonicalUrl: String,
    @ColumnInfo(name = "comments_count")
    val commentsCount: Int,
    @ColumnInfo(name = "positive_reactions_count")
    val positiveReactionsCount: Int,
    @ColumnInfo(name = "public_reactions_count")
    val publicReactionsCount: Int,
    @ColumnInfo(name = "collection_id")
    val collectionId: Int?,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "edited_at")
    val editedAt: String?,
    @ColumnInfo(name = "crossposted_at")
    val crosspostedAt: String?,
    @ColumnInfo(name = "published_at")
    val publishedAt: String,
    @ColumnInfo(name = "last_comment_at")
    val lastCommentAt: String,
    @ColumnInfo(name = "published_timestamp")
    val publishedTimestamp: String,
    @ColumnInfo(name = "reading_time_minutes")
    val readingTimeMinutes: Int
)
