package com.ivangarzab.kluvs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.kluvs.presentation.effect.EffectChannel
import com.ivangarzab.kluvs.presentation.effect.PlatformEffect
import com.ivangarzab.kluvs.presentation.effect.PlatformEffectHandler
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Base for every screen ViewModel: owns the screen's single [state] and its side effects.
 *
 * Platform effects go to the platform's [PlatformEffectHandler], which comes from Koin the first
 * time one is sent, so a ViewModel that never sends one never needs it (nor do its tests).
 * Screen effects go to [screenEffects], collected by the platform's shared listener, and only
 * while it's listening.
 *
 * [S] is bound to [Any] so Kotlin/Native exports it to Swift as a typed generic.
 */
abstract class BaseViewModel<S : Any>(initialState: S) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val platformEffectHandler: PlatformEffectHandler by inject()
    private val platformEffects by lazy {
        EffectChannel<PlatformEffect>().apply {
            handleWith(viewModelScope, platformEffectHandler::handle)
        }
    }

    // No replay: an effect sent while no screen listens is dropped, so a ViewModel that outlives
    // its screen (a tab switched away from mid-request) can't show it later, out of context.
    // The unlimited extra buffer only applies while someone listens, so tryEmit never fails.
    private val _screenEffects = MutableSharedFlow<ScreenEffect>(extraBufferCapacity = Channel.UNLIMITED)

    /** One-shot screen events, collected by the screen's shared listener. */
    val screenEffects: SharedFlow<ScreenEffect> = _screenEffects.asSharedFlow()

    protected val currentState: S get() = _state.value

    protected fun updateState(transform: (S) -> S) {
        _state.update(transform)
    }

    protected fun send(effect: PlatformEffect) {
        platformEffects.send(effect)
    }

    protected fun send(effect: ScreenEffect) {
        _screenEffects.tryEmit(effect)
    }
}
