package com.example.data.remote.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class OrganizationResponse(
    @SerializedName("name")
    @Expose
    val name: String,

    @SerializedName("username")
    @Expose
    val username: String,

    @SerializedName("slug")
    @Expose
    val slug: String,

    @SerializedName("profile_image")
    @Expose
    val profileImage: String,

    @SerializedName("profile_image_90")
    @Expose
    val profileImage90: String
)