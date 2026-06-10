package com.example.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flare_tags",
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
data class FlareTagEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "article_id")
    val articleId: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "bg_color_hex")
    val bgColorHex: String?,
    @ColumnInfo(name = "text_color_hex")
    val textColorHex: String?
)
