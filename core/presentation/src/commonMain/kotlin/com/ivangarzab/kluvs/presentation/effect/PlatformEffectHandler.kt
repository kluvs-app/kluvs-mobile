package com.ivangarzab.kluvs.presentation.effect

/**
 * Runs [PlatformEffect]s on the platform. There is one implementation per platform, registered
 * through `platformEffectHandlerModule`; shared code never knows which one it has.
 *
 * Called on the main thread. A failure is logged and swallowed: an effect that can't run
 * must never crash the app.
 */
interface PlatformEffectHandler {
    fun handle(effect: PlatformEffect)
}
