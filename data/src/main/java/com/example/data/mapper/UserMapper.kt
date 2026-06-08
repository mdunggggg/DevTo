package com.example.data.mapper

import com.example.data.remote.model.UserResponse
import com.example.domain.model.User

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
