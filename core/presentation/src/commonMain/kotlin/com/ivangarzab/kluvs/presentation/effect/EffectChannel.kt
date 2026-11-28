package com.ivangarzab.kluvs.presentation.effect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Delivers each effect of type [E] exactly once, in the order sent. Effects sent before
 * [handleWith] starts are buffered, not dropped.
 *
 * `BaseViewModel` uses one for [PlatformEffect]s, wired to the platform handler. Platform effects
 * need no screen, so buffering them until the handler runs is always right. Screen effects don't
 * use this: they're dropped when no screen is listening (see `BaseViewModel.screenEffects`).
 */
class EffectChannel<E : Any> {

    private val channel = Channel<E>(Channel.UNLIMITED)

    fun send(effect: E) {
        channel.trySend(effect)
    }

    /** Hands every effect to [handle] until [scope] is cancelled. Call it once. */
    fun handleWith(scope: CoroutineScope, handle: (E) -> Unit): Job = scope.launch {
        for (effect in channel) handle(effect)
    }
}
