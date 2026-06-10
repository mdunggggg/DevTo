package com.example.data.mapper

import com.example.data.local.model.OrganizationEntity
import com.example.data.remote.model.OrganizationResponse
import com.example.domain.model.Organization
import java.util.UUID

fun OrganizationResponse.toDomain(): Organization {
    return Organization(
        name = name,
        username = username,
        slug = slug,
        profileImage = profileImage,
        profileImage90 = profileImage90
    )
}

fun OrganizationResponse.toEntity(articleId: Int): OrganizationEntity {
    return OrganizationEntity(
        id = UUID.randomUUID().toString(),
        articleId = articleId,
        name = name,
        username = username,
        slug = slug,
        profileImage = profileImage,
        profileImage90 = profileImage90
    )
}

fun Organization.toEntity(articleId: Int): OrganizationEntity {
    return OrganizationEntity(
        id = UUID.randomUUID().toString(),
        articleId = articleId,
        name = name,
        username = username,
        slug = slug,
        profileImage = profileImage,
        profileImage90 = profileImage90
    )
}

fun OrganizationEntity.toDomain(): Organization {
    return Organization(
        name = name,
        username = username,
        slug = slug,
        profileImage = profileImage,
        profileImage90 = profileImage90
    )
}
