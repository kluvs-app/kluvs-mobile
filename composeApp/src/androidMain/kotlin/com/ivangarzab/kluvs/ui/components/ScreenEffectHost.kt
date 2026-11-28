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
    }

private val SnackbarTone.variant: SnackbarVariant
    get() = when (this) {
        SnackbarTone.SUCCESS -> SnackbarVariant.SUCCESS
        SnackbarTone.DANGER -> SnackbarVariant.DANGER
    }
