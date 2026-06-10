package com.example.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.ext.isNotEmpty
import com.example.domain.model.params.ArticleParam

@Entity(tableName = "remote_keys")
data class RemoteKey(
    @PrimaryKey
    @ColumnInfo(name = "cache_key")
    val cacheKey: String,

    @ColumnInfo(name = "next_page")
    val nextPage: Int?,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        fun buildKey(articleParam: ArticleParam): String {
            return buildString {
                if (articleParam.tag.isNotEmpty())
                    append("tag=${articleParam.tag}")
                if (articleParam.tags.isNotEmpty())
                    append("&tags=${articleParam.tags}")
                if (articleParam.tagsExclude.isNotEmpty())
                    append("&tagsExclude=${articleParam.tagsExclude}")
                if (articleParam.state != null)
                    append("&state=${articleParam.state}")
                if (articleParam.top != null)
                    append("&top=${articleParam.top}")
                if (articleParam.collectionId != null)
                    append("&collectionId=${articleParam.collectionId}")
                if (isEmpty()) append("default")
            }
        }
    }
}