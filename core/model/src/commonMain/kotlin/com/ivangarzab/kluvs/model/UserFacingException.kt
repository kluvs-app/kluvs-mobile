package com.ivangarzab.kluvs.model

/**
 * An exception whose [message] was written for end users (e.g. "Cannot remove yourself from the
 * club") and is safe to show as-is. Domain code throws this for deliberate rule violations; every
 * other exception reaching the UI is mapped to a friendly, generic message instead (see
 * `Throwable.toDisplayMessage()` in `core:presentation`).
 */
open class UserFacingException(message: String, cause: Throwable? = null) : Exception(message, cause)
