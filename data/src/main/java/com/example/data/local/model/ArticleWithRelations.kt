package com.example.data.local.model

import androidx.room.Embedded
import androidx.room.Relation

data class ArticleWithRelations(
    @Embedded
    val article: ArticleEntity,
    @Relation(parentColumn = "id", entityColumn = "article_id")
    val user: UserEntity,
    @Relation(parentColumn = "id", entityColumn = "article_id")
    val organization: OrganizationEntity?,
    @Relation(parentColumn = "id", entityColumn = "article_id")
    val flareTag: FlareTagEntity?
)
