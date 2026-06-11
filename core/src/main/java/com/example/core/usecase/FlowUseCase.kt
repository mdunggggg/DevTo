package com.example.core.usecase

import kotlinx.coroutines.flow.Flow

abstract class FlowUseCase <in Input, out Output> : Validator<Input> {
    suspend operator fun invoke(input: Input): Flow<Output> {
        validate(input)
        return execute(input)
    }

    protected abstract suspend fun execute(input: Input): Flow<Output>

}