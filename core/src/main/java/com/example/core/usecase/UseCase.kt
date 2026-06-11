package com.example.core.usecase

abstract class UseCase<in Input, out Output> : Validator<Input>{
    suspend operator fun invoke(input: Input): Output {
        validate(input)
        return execute(input)
    }

    protected abstract suspend fun execute(input: Input): Output

}