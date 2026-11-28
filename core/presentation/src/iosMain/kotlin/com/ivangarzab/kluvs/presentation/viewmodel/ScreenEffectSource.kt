package com.ivangarzab.kluvs.presentation.viewmodel

import com.ivangarzab.kluvs.presentation.Closeable
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect

/**
 * What the iOS screen-effect listener (the SwiftUI `.screenEffects(_:)` modifier) subscribes to.
 * Every [BaseViewModelHelper] is one. It isn't generic, so Swift can take any Helper without
 * caring about its state type.
 */
interface ScreenEffectSource {
    /** Calls [callback] for each screen effect until the returned [Closeable] is closed. Not on the main thread. */
    fun observeScreenEffects(callback: (ScreenEffect) -> Unit): Closeable
}
