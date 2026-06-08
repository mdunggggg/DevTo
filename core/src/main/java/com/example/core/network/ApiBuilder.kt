package com.example.core.network

import retrofit2.Retrofit

class ApiBuilder<T> internal constructor(private val api : Class<T>) {
    private val converterBuilder = ConverterBuilder()
    private val clientBuilder = ClientBuilder()

    var baseUrl: String = ""


    fun converter(block: ConverterBuilder.() -> Unit) {
        converterBuilder.apply(block)
    }
    fun client(block: ClientBuilder.() -> Unit) {
        clientBuilder.apply(block)
    }

    fun build() : T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(converterBuilder.build())
            .build()
            .create(api)
    }
}

internal fun <T> apiBuilder(
    api : Class<T>,
    builder : ApiBuilder<T>.() -> Unit,
) : T {
    return ApiBuilder(api).apply(builder).build()
}

fun <API> buildServiceApi(
    serviceBaseUrl: String,
    serviceApiClass: Class<API>,
    clientBuilder: (ClientBuilder.() -> Unit)? = null,
    converterBuilder: (ConverterBuilder.() -> Unit)? = null
): API {
    return apiBuilder(serviceApiClass) {
        baseUrl = serviceBaseUrl
        client(clientBuilder ?: {})
        converter(converterBuilder ?: {})
    }
}