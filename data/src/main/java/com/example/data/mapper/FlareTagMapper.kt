package com.example.data.mapper

import com.example.data.remote.model.FlareTagResponse
import com.example.domain.model.FlareTag

fun FlareTagResponse.toDomain(): FlareTag {
    return FlareTag(
        name = name,
        bgColorHex = bgColorHex,
        textColorHex = textColorHex
    )
}
