package com.example.domain.model.params

data class ArticleParam(
    val page: Int = 1,
    val perPage: Int = 30,
    val tag: String? = null,
    val tags: String? = null,
    val tagsExclude: String? = null,
    val state: ArticleParamState? = null,
    val top: Int? = null,
    val collectionId: Int? = null
) {
    fun toQueryMap(): Map<String, String> {
        return buildMap {
            put("page", page.toString())
            put("per_page", perPage.toString())

            tag?.takeIf { it.isNotBlank() }?.let {
                put("tag", it)
            }

            tags?.takeIf { it.isNotBlank() }?.let {
                put("tags", it)
            }

            tagsExclude?.takeIf { it.isNotBlank() }?.let {
                put("tags_exclude", it)
            }

            state?.let {
                put("state", it.name.lowercase())
            }

            top?.let {
                put("top", it.toString())
            }

            collectionId?.let {
                put("collection_id", it.toString())
            }
        }
    }
}

enum class ArticleParamState {
    FRESH, RISING, ALL
}