package com.example.core.result

sealed class Result<out V: Any, out T: Throwable> {
    class Failure<out E : Throwable>(val throwable : E) : Result<Nothing, E>() {
        override fun toString(): String {
            return "[Failure: $throwable]"
        }
    }

    class Success<out V : Any>(val value : V) : Result<V, Nothing>() {
        override fun toString(): String {
            return "[Success: $value]"
        }
    }

    fun isSuccess() = this !is Failure
    fun isFailure() = this is Failure

    fun getOrNull(): V? = when {
        isSuccess() -> (this as Success).value
        else -> null
    }

    fun get(): V {
        check(isSuccess()) { "Could not get value of Failure Result" }
        return (this as Success).value
    }

    fun exceptionOrNull(): T? = when {
        isFailure() -> (this as Failure).throwable
        else -> null
    }

    fun exception(): T {
        check(isFailure()) { "Could not get exception of Success Result" }
        return (this as Failure).throwable
    }

    companion object {
        fun <V : Any> success(value: V): Result<V, Nothing> = Success(value)
        fun <E : Throwable> failure(throwable: E): Result<Nothing, E> = Failure(throwable)
    }
}

suspend fun<V: Any> getResult(
    block: suspend () -> V
): Result<V, Throwable> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e)
    }
}