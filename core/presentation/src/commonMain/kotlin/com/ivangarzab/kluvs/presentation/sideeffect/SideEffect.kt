package com.ivangarzab.kluvs.presentation.sideeffect

/**
 * A one-shot action a ViewModel wants to happen that isn't screen state: it runs once and
 * leaves nothing on screen to redraw. Each platform runs every case in its [SideEffectHandler],
 * so adding a case won't compile until both platforms handle it.
 *
 * Anything that stays on screen (our own bottom sheets, dialogs) is state, not a side effect:
 * it has to survive rotation and be dismissed.
 */
sealed class SideEffect {
    /** Opens [url] outside the app: the browser, or the app that owns the link. */
    data class OpenUrl(val url: String) : SideEffect()

    /** Opens this app's listing in the platform's store (Google Play or the App Store). */
    data object OpenStoreListing : SideEffect()

    /** Puts [text] on the system clipboard. */
    data class CopyToClipboard(val text: String) : SideEffect()

    /** Opens the system share sheet with [text]. */
    data class ShareText(val text: String) : SideEffect()
}
