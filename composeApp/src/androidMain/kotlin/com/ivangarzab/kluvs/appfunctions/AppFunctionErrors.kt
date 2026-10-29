package com.ivangarzab.kluvs.appfunctions

import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionDeniedException
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionPermissionRequiredException
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.clubs.domain.UnauthorizedException
import com.ivangarzab.kluvs.data.error.toAppError
import com.ivangarzab.kluvs.model.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs an AppFunction body off the main thread and guarantees only [AppFunctionException]s
 * escape — anything else reaches the agent as an opaque unknown error.
 */
internal suspend fun <T> appFunction(name: String, block: suspend () -> T): T =
    withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppFunctionException) {
            Bark.w("AppFunction '$name' rejected: ${e.message}")
            throw e
        } catch (e: Throwable) {
            Bark.e("AppFunction '$name' failed.", e)
            throw e.toAppFunctionException()
        }
    }

internal fun <T> Result<T>.getOrThrowAppFunction(): T = getOrElse { throw it.toAppFunctionException() }

internal fun Throwable.toAppFunctionException(): AppFunctionException = when (this) {
    is AppFunctionException -> this
    is UnauthorizedException -> AppFunctionDeniedException("You don't have permission to do that in this club.")
    else -> when (val error = toAppError()) {
        is AppError.NotFound -> AppFunctionElementNotFoundException(error.detail ?: "That item couldn't be found.")
        is AppError.ValidationError -> AppFunctionInvalidArgumentException(error.detail ?: "The request was invalid.")
        is AppError.Conflict -> AppFunctionInvalidArgumentException(error.detail ?: "That conflicts with existing data.")
        AppError.Unauthorized -> AppFunctionPermissionRequiredException("Sign in to Kluvs again to continue.")
        AppError.Forbidden -> AppFunctionDeniedException("You don't have permission to do that.")
        AppError.NoConnection, AppError.Timeout -> AppFunctionAppUnknownException("Couldn't reach Kluvs. Check your internet connection.")
        AppError.ServerError, AppError.Unknown -> AppFunctionAppUnknownException("Kluvs ran into a problem. Try again later.")
    }
}
