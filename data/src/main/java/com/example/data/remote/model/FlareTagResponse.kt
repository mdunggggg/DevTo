package com.example.data.remote.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FlareTagResponse(
    @SerializedName("name")
    @Expose
    val name: String,

    @SerializedName("bg_color_hex")
    @Expose
    val bgColorHex: String?,

    @SerializedName("text_color_hex")
    @Expose
    val textColorHex: String?
)
