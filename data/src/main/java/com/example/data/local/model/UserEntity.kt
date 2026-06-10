package com.example.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = ArticleEntity::class,
            parentColumns = ["id"],
            childColumns = ["article_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["article_id"], unique = true)]
)
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "article_id")
    val articleId: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "twitter_username")
    val twitterUsername: String?,
    @ColumnInfo(name = "github_username")
    val githubUsername: String?,
    @ColumnInfo(name = "remote_user_id")
    val remoteUserId: Int?,
    @ColumnInfo(name = "website_url")
    val websiteUrl: String?,
    @ColumnInfo(name = "profile_image")
    val profileImage: String,
    @ColumnInfo(name = "profile_image_90")
    val profileImage90: String
)
