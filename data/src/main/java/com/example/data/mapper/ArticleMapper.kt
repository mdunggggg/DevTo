package com.example.data.mapper

import com.example.data.local.model.ArticleEntity
import com.example.data.local.model.ArticleWithRelations
import com.example.data.remote.model.ArticleResponse
import com.example.domain.model.Article

fun ArticleResponse.toDomain(): Article {
    return Article(
        typeOf = typeOf,
        id = id,
        title = title,
        description = description,
        readablePublishDate = readablePublishDate,
        slug = slug,
        path = path,
        url = url,
        commentsCount = commentsCount,
        publicReactionsCount = publicReactionsCount,
        collectionId = collectionId,
        publishedTimestamp = publishedTimestamp,
        positiveReactionsCount = positiveReactionsCount,
        coverImage = coverImage,
        socialImage = socialImage,
        canonicalUrl = canonicalUrl,
        createdAt = createdAt,
        editedAt = editedAt,
        crosspostedAt = crosspostedAt,
        publishedAt = publishedAt,
        lastCommentAt = lastCommentAt,
        readingTimeMinutes = readingTimeMinutes,
        tagList = tagList,
        tags = tags,
        user = user.toDomain(),
        organization = organization?.toDomain(),
        flareTag = flareTag?.toDomain()
    )
}

fun List<ArticleResponse>.toDomain(): List<Article> {
    return map { it.toDomain() }
}

fun ArticleResponse.toEntity(): ArticleEntity {
    return ArticleEntity(
        id = id,
        typeOf = typeOf,
        title = title,
        description = description,
        coverImage = coverImage,
        readablePublishDate = readablePublishDate,
        socialImage = socialImage,
        tagList = tagList,
        tags = tags,
        slug = slug,
        path = path,
        url = url,
        canonicalUrl = canonicalUrl,
        commentsCount = commentsCount,
        positiveReactionsCount = positiveReactionsCount,
        publicReactionsCount = publicReactionsCount,
        collectionId = collectionId,
        createdAt = createdAt,
        editedAt = editedAt,
        crosspostedAt = crosspostedAt,
        publishedAt = publishedAt,
        lastCommentAt = lastCommentAt,
        publishedTimestamp = publishedTimestamp,
        readingTimeMinutes = readingTimeMinutes
    )
}

fun ArticleResponse.toEntityWithRelation(): ArticleWithRelations {
    return ArticleWithRelations(
        article = toEntity(),
        user = user.toEntity(id),
        organization = organization?.toEntity(id),
        flareTag = flareTag?.toEntity(id)
    )
}

fun ArticleWithRelations.toDomain(): Article {
    return Article(
        id = article.id,
        typeOf = article.typeOf,
        title = article.title,
        description = article.description,
        coverImage = article.coverImage,
        readablePublishDate = article.readablePublishDate,
        socialImage = article.socialImage,
        tagList = article.tagList,
        tags = article.tags,
        slug = article.slug,
        path = article.path,
        url = article.url,
        canonicalUrl = article.canonicalUrl,
        commentsCount = article.commentsCount,
        positiveReactionsCount = article.positiveReactionsCount,
        publicReactionsCount = article.publicReactionsCount,
        collectionId = article.collectionId,
        createdAt = article.createdAt,
        editedAt = article.editedAt,
        crosspostedAt = article.crosspostedAt,
        publishedAt = article.publishedAt,
        lastCommentAt = article.lastCommentAt,
        publishedTimestamp = article.publishedTimestamp,
        readingTimeMinutes = article.readingTimeMinutes,
        organization = organization?.toDomain(),
        flareTag = flareTag?.toDomain(),
        user = user.toDomain()
    )
}