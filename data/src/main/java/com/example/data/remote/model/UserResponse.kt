package com.example.data.remote.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("name")
    @Expose
    val name: String,

    @SerializedName("username")
    @Expose
    val username: String,

    @SerializedName("twitter_username")
    @Expose
    val twitterUsername: String?,

    @SerializedName("github_username")
    @Expose
    val githubUsername: String?,

    @SerializedName("user_id")
    @Expose
    val userId: Int?,

    @SerializedName("website_url")
    @Expose
    val websiteUrl: String?,

    @SerializedName("profile_image")
    @Expose
    val profileImage: String,

    @SerializedName("profile_image_90")
    @Expose
    val profileImage90: String
)
