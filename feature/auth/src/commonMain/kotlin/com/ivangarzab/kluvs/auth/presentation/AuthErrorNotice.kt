package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.presentation.effect.NoticeKey
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import com.ivangarzab.kluvs.presentation.effect.SnackbarMessage
import com.ivangarzab.kluvs.presentation.effect.SnackbarTone

/**
 * The danger snackbar for an auth failure. Its text stays in platform string resources: each
 * screen-effect listener maps the [NoticeKey] to its `error_*` string.
 */
internal fun Throwable.toAuthErrorSnackbar(): ScreenEffect.ShowSnackbar =
    ScreenEffect.ShowSnackbar(SnackbarMessage.Notice(toAuthNoticeKey()), SnackbarTone.DANGER)

private fun Throwable.toAuthNoticeKey(): NoticeKey = when (this as? AuthError ?: AuthError.UnexpectedError) {
    AuthError.InvalidCredentials -> NoticeKey.AUTH_INVALID_CREDENTIALS
    AuthError.EmailNotConfirmed -> NoticeKey.AUTH_EMAIL_NOT_CONFIRMED
    AuthError.NoConnection -> NoticeKey.AUTH_NO_CONNECTION
    AuthError.RateLimitExceeded -> NoticeKey.AUTH_RATE_LIMIT_EXCEEDED
    AuthError.UserNotFound -> NoticeKey.AUTH_USER_NOT_FOUND
    AuthError.WeakPassword -> NoticeKey.AUTH_WEAK_PASSWORD
    AuthError.UserAlreadyExists -> NoticeKey.AUTH_USER_ALREADY_EXISTS
    AuthError.AuthenticationFailed -> NoticeKey.AUTH_AUTHENTICATION_FAILED
    // Sign up handles EmailConfirmationRequired as a success, so it never reaches a snackbar
    AuthError.EmailConfirmationRequired,
    AuthError.UnexpectedError -> NoticeKey.AUTH_UNEXPECTED_ERROR
}
