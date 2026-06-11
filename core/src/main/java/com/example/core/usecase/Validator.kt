package com.example.core.usecase

fun interface Validator<in Input> {
    fun validate(input: Input)
}