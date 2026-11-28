package com.ivangarzab.kluvs.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.components.KluvsSnackbarVisuals
import com.ivangarzab.kluvs.designsystem.components.SnackbarVariant
import com.ivangarzab.kluvs.presentation.effect.NoticeKey
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import com.ivangarzab.kluvs.presentation.effect.SnackbarMessage
import com.ivangarzab.kluvs.presentation.effect.SnackbarTone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * The one Android listener for [ScreenEffect]s. A screen attaches it once, next to its
 * `SnackbarHost`, and never handles screen effects itself:
 * `ScreenEffectHost(viewModel.screenEffects, snackbarHostState)`.
 *
 * A new snackbar replaces the one showing instead of queuing behind it
 * (design-system/docs/states.md § Snackbar): `collectLatest` cancels the previous
 * `showSnackbar`, and cancelling it takes that snackbar off screen.
 */
@Composable
fun ScreenEffectHost(
    effects: Flow<ScreenEffect>,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    LaunchedEffect(effects, snackbarHostState) {
        effects.collectLatest { effect ->
            when (effect) {
                is ScreenEffect.ShowSnackbar -> {
                    val message = when (val m = effect.message) {
                        is SnackbarMessage.Text -> m.text
                        is SnackbarMessage.Notice -> context.getString(m.key.stringRes)
                    }
                    snackbarHostState.showSnackbar(
                        KluvsSnackbarVisuals(message = message, variant = effect.tone.variant)
                    )
                }
            }
        }
    }
}

private val NoticeKey.stringRes: Int
    get() = when (this) {
        NoticeKey.SAVE_SUCCESS -> R.string.save_success
        NoticeKey.PASSWORD_UPDATED -> R.string.password_updated
        NoticeKey.AUTH_INVALID_CREDENTIALS -> R.string.error_invalid_credentials
        NoticeKey.AUTH_EMAIL_NOT_CONFIRMED -> R.string.error_email_not_confirmed
        NoticeKey.AUTH_NO_CONNECTION -> R.string.error_no_connection
        NoticeKey.AUTH_RATE_LIMIT_EXCEEDED -> R.string.error_rate_limit_exceeded
        NoticeKey.AUTH_USER_NOT_FOUND -> R.string.error_user_not_found
        NoticeKey.AUTH_WEAK_PASSWORD -> R.string.error_weak_password
        NoticeKey.AUTH_USER_ALREADY_EXISTS -> R.string.error_user_already_exists
        NoticeKey.AUTH_AUTHENTICATION_FAILED -> R.string.error_authentication_failed
        NoticeKey.AUTH_UNEXPECTED_ERROR -> R.string.error_unexpected
    }

private val SnackbarTone.variant: SnackbarVariant
    get() = when (this) {
        SnackbarTone.SUCCESS -> SnackbarVariant.SUCCESS
        SnackbarTone.DANGER -> SnackbarVariant.DANGER
    }
