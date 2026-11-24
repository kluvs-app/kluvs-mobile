package com.ivangarzab.kluvs.presentation.error

import com.ivangarzab.kluvs.data.error.toAppError
import com.ivangarzab.kluvs.model.UserFacingException

/**
 * The single place a failure becomes text for the user. [UserFacingException] messages are shown
 * as written; anything else is classified via [toAppError] and shown as friendly copy, so raw
 * exception text (HTTP bodies, class names) never reaches the UI. Log the raw throwable with Bark
 * separately.
 */
fun Throwable.toDisplayMessage(): String =
    if (this is UserFacingException) message ?: toAppError().toUserMessage()
    else toAppError().toUserMessage()
