package com.ridecast.core.extension

import com.ridecast.core.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Wraps a [Flow] of [T] into a [Flow] of [Result]<[T]>, automatically emitting
 * [Result.Loading] on start, [Result.Success] for each item, and [Result.Error]
 * if an exception is thrown downstream.
 *
 * Typical usage in a ViewModel:
 * ```
 * someFlow.asResult().collect { result ->
 *     when (result) {
 *         is Result.Loading  -> showSpinner()
 *         is Result.Success  -> render(result.data)
 *         is Result.Error    -> showError(result.message)
 *     }
 * }
 * ```
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it)) }
}
