package com.example.data.mapper

import com.example.data.local.model.FlareTagEntity
import com.example.data.remote.model.FlareTagResponse
import com.example.domain.model.FlareTag
import java.util.UUID

fun FlareTagResponse.toDomain(): FlareTag {
    return FlareTag(
        name = name,
        bgColorHex = bgColorHex,
        textColorHex = textColorHex
    )
}

fun FlareTagResponse.toEntity(articleId: Int): FlareTagEntity {
    return FlareTagEntity(
        id = UUID.randomUUID().toString(),
        articleId = articleId,
        name = name,
        bgColorHex = bgColorHex,
        textColorHex = textColorHex
    )
}

fun FlareTag.toEntity(articleId: Int): FlareTagEntity {
    return FlareTagEntity(
        id = UUID.randomUUID().toString(),
        articleId = articleId,
        name = name,
        bgColorHex = bgColorHex,
        textColorHex = textColorHex
    )
}

fun FlareTagEntity.toDomain(): FlareTag {
    return FlareTag(
        name = name,
        bgColorHex = bgColorHex,
        textColorHex = textColorHex
    )
}
