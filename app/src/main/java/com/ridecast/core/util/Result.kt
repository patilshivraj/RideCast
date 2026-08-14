package com.ridecast.core.util

/**
 * A discriminated union representing the outcome of an operation at any point in its lifecycle.
 *
 * Used across all layers (domain → data → presentation) so that UI can react to loading,
 * success, and error states without coupling to Android or any specific framework.
 *
 * @param T The type of data carried in the [Success] state.
 */
sealed class Result<out T> {

    /** The operation completed successfully. [data] holds the result. */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * The operation failed.
     *
     * @param exception The underlying throwable.
     * @param message   Human-readable error message; defaults to the exception's localised message.
     */
    data class Error(
        val exception: Throwable? = null,
        val message: String? = exception?.localizedMessage,
    ) : Result<Nothing>()

    /** The operation is in progress. No data is available yet. */
    data object Loading : Result<Nothing>()
}

/** Returns `true` if this result is [Result.Loading]. */
val Result<*>.isLoading: Boolean get() = this is Result.Loading

/** Returns `true` if this result is [Result.Success]. */
val Result<*>.isSuccess: Boolean get() = this is Result.Success

/** Returns `true` if this result is [Result.Error]. */
val Result<*>.isError: Boolean get() = this is Result.Error

/** Returns the data from [Result.Success], or `null` for all other states. */
fun <T> Result<T>.getOrNull(): T? = (this as? Result.Success)?.data

/** Returns the data from [Result.Success], or [default] for all other states. */
fun <T> Result<T>.getOrDefault(default: T): T = (this as? Result.Success)?.data ?: default

/** Transforms the data inside [Result.Success], leaving other states unchanged. */
fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> this
}
