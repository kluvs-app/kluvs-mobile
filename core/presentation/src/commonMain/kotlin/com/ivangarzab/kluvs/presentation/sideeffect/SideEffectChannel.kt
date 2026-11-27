package com.ivangarzab.kluvs.presentation.sideeffect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Delivers each [SideEffect] exactly once, in the order sent. Effects sent before
 * [handleWith] starts are buffered, not dropped.
 *
 * A ViewModel owns one, sends effects into it, and starts [handleWith] on its own scope.
 */
class SideEffectChannel {

    private val channel = Channel<SideEffect>(Channel.UNLIMITED)

    fun send(effect: SideEffect) {
        channel.trySend(effect)
    }

    /** Hands every effect to [handler] until [scope] is cancelled. Call it once. */
    fun handleWith(handler: SideEffectHandler, scope: CoroutineScope): Job = scope.launch {
        for (effect in channel) handler.handle(effect)
    }
}
