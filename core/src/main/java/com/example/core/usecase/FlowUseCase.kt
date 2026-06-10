package com.example.core.usecase

import kotlinx.coroutines.flow.Flow

abstract class FlowUseCase <in Input, out Output> {
    abstract suspend operator fun invoke(input: Input): Flow<Output>
}