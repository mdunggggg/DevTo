package com.example.data.remote.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ArticleResponse(
    @SerializedName("type_of")
    @Expose
    val typeOf: String,

    @SerializedName("id")
    @Expose
    val id: Int,

    @SerializedName("title")
    @Expose
    val title: String,

    @SerializedName("description")
    @Expose
    val description: String,

    @SerializedName("cover_image")
    @Expose
    val coverImage: String?,

    @SerializedName("readable_publish_date")
    @Expose
    val readablePublishDate: String,

    @SerializedName("social_image")
    @Expose
    val socialImage: String,

    @SerializedName("tag_list")
    @Expose
    val tagList: List<String>,

    @SerializedName("tags")
    @Expose
    val tags: String,

    @SerializedName("slug")
    @Expose
    val slug: String,

    @SerializedName("path")
    @Expose
    val path: String,

    @SerializedName("url")
    @Expose
    val url: String,

    @SerializedName("canonical_url")
    @Expose
    val canonicalUrl: String,

    @SerializedName("comments_count")
    @Expose
    val commentsCount: Int,

    @SerializedName("positive_reactions_count")
    @Expose
    val positiveReactionsCount: Int,

    @SerializedName("public_reactions_count")
    @Expose
    val publicReactionsCount: Int,

    @SerializedName("collection_id")
    @Expose
    val collectionId: Int?,

    @SerializedName("created_at")
    @Expose
    val createdAt: String,

    @SerializedName("edited_at")
    @Expose
    val editedAt: String?,

    @SerializedName("crossposted_at")
    @Expose
    val crosspostedAt: String?,

    @SerializedName("published_at")
    @Expose
    val publishedAt: String,

    @SerializedName("last_comment_at")
    @Expose
    val lastCommentAt: String,

    @SerializedName("published_timestamp")
    @Expose
    val publishedTimestamp: String,

    @SerializedName("reading_time_minutes")
    @Expose
    val readingTimeMinutes: Int,

    @SerializedName("user")
    @Expose
    val user: UserResponse,

    @SerializedName("flare_tag")
    @Expose
    val flareTag: FlareTagResponse?,

    @SerializedName("organization")
    @Expose
    val organization: OrganizationResponse?
)
