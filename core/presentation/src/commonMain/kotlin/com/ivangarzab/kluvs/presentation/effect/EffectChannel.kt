package com.ivangarzab.kluvs.presentation.effect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Delivers each effect of type [E] exactly once, in the order sent. Effects sent before
 * [handleWith] starts are buffered, not dropped.
 *
 * `BaseViewModel` owns one per effect type. Each channel has exactly one consumer: either
 * [handleWith] (platform effects, run by the platform handler) or [asFlow] (screen effects,
 * collected by the screen's listener), never both.
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

    /** The effects as a flow for a single collector. Each effect goes to one collector only. */
    fun asFlow(): Flow<E> = channel.receiveAsFlow()
}
