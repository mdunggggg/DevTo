package com.example.core.network

import com.google.gson.GsonBuilder
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory

class ConverterBuilder internal constructor(){
    var factory : Converter.Factory? = null
    fun build(): Converter.Factory = factory ?: GsonConverterFactory.create(
        GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    )
}