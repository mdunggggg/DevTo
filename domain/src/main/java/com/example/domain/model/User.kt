package com.example.domain.model

data class User(
    val name: String,
    val username: String,
    val twitterUsername: String?,
    val githubUsername: String?,
    val userId: Int?,
    val websiteUrl: String?,
    val profileImage: String,
    val profileImage90: String
)
