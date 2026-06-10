package com.example.data.remote.api

import com.example.core.network.buildServiceApi
import com.example.data.remote.model.ArticleResponse
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ArticleApi {
    @GET("articles")
    suspend fun getArticles(
        @QueryMap params: Map<String, String>
    ) : List<ArticleResponse>

    companion object {
        fun build(host: String) : ArticleApi {
            return buildServiceApi(
                serviceBaseUrl = host,
                serviceApiClass = ArticleApi::class.java,
                clientBuilder = {
                    logging {
                        levelLog = HttpLoggingInterceptor.Level.BODY
                    }
                }
            )
        }
    }
}