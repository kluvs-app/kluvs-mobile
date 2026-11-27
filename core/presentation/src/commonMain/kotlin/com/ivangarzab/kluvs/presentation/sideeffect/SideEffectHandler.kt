package com.ivangarzab.kluvs.presentation.sideeffect

/**
 * Runs [SideEffect]s on the platform. There is one implementation per platform, registered
 * through `sideEffectHandlerModule`; shared code never knows which one it has.
 *
 * Called on the main thread. A failure is logged and swallowed: a side effect that can't run
 * must never crash the app.
 */
interface SideEffectHandler {
    fun handle(effect: SideEffect)
}
