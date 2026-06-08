package com.example.domain.model.params

data class ArticleParam(
    val page : Int = 1,
    val perPage: Int = 30,
    val tag: String? = null,
    val tags : String? = null,
    val tagsExclude : String? = null,
    val state: ArticleParamState? = null,
    val top: Int? = null,
    val collectionId : Int? = null
)

enum class ArticleParamState {
    FRESH, RISING, ALL
}