package com.example.data.mapper

import com.example.data.local.model.UserEntity
import com.example.data.remote.model.UserResponse
import com.example.domain.model.User
import java.util.UUID

fun UserResponse.toDomain(): User {
    return User(
        name = name,
        username = username,
        twitterUsername = twitterUsername,
        githubUsername = githubUsername,
        userId = userId,
        websiteUrl = websiteUrl,
        profileImage = profileImage,
        profileImage90 = profileImage90
    )
}

fun UserResponse.toEntity(articleId: Int): UserEntity {
    return UserEntity(
        id = UUID.randomUUID().toString(),
        articleId = articleId,
        name = name,
        username = username,
        twitterUsername = twitterUsername,
        githubUsername = githubUsername,
        remoteUserId = userId,
        websiteUrl = websiteUrl,
        profileImage = profileImage,
        profileImage90 = profileImage90
    )
}

fun User.toEntity(articleId: Int): UserEntity {
    return UserEntity(
        id = UUID.randomUUID().toString(),
        articleId = articleId,
        name = name,
        username = username,
        twitterUsername = twitterUsername,
        githubUsername = githubUsername,
        remoteUserId = userId,
        websiteUrl = websiteUrl,
        profileImage = profileImage,
        profileImage90 = profileImage90
    )
}

fun UserEntity.toDomain(): User {
    return User(
        name = name,
        username = username,
        twitterUsername = twitterUsername,
        githubUsername = githubUsername,
        userId = remoteUserId,
        websiteUrl = websiteUrl,
        profileImage = profileImage,
        profileImage90 = profileImage90
    )
}
