package com.example.data.mapper

import com.example.data.remote.model.OrganizationResponse
import com.example.domain.model.Organization

fun OrganizationResponse.toDomain(): Organization {
    return Organization(
        name = name,
        username = username,
        slug = slug,
        profileImage = profileImage,
        profileImage90 = profileImage90
    )
}
