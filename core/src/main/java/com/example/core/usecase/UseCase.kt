package com.example.core.usecase

abstract class UseCase<in Input, out Output> {
    abstract suspend operator fun invoke(input: Input): Output
}