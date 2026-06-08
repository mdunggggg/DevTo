package com.example.core.network

import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class ClientBuilder internal constructor(){
    var interceptors: List<Interceptor> = listOf()

    var readTimeoutInMillis: Long = 10_000

    var writeTimeoutInMillis: Long = 10_000

    var authenticator: Authenticator? = null

    private val loggingBuilder = LoggingBuilder()

    fun logging(builder : LoggingBuilder.() -> Unit) {
        loggingBuilder.apply(builder)
    }

    fun addInterceptor(interceptor: Interceptor) {
        interceptors = interceptors + interceptor
    }

    fun build() : OkHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(loggingBuilder.build())
        .readTimeout(readTimeoutInMillis, TimeUnit.MILLISECONDS)
        .writeTimeout(writeTimeoutInMillis, TimeUnit.MILLISECONDS)
        .apply { interceptors.forEach { addInterceptor(it) } }
        .apply { authenticator?.let { authenticator(it) } }
        .build()
}

class LoggingBuilder internal constructor() {
    var level : HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY

    fun build() : Interceptor = HttpLoggingInterceptor().apply { setLevel(level) }
}