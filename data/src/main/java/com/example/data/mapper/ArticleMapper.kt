package com.example.data.mapper

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
