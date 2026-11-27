package com.ivangarzab.kluvs.presentation.effect

/**
 * A one-shot action only the phone can do (open a link, copy, share): it needs no screen on top
 * and leaves nothing on screen to redraw. Each platform runs every case in its
 * [PlatformEffectHandler], so adding a case won't compile until both platforms handle it.
 *
 * Cases are named after what the phone does, never why a screen wants it: `ShareText`, not
 * `ShareClubInvite`. One-shot events the visible screen reacts to (a toast) are screen effects,
 * and anything that stays on screen (sheets, dialogs) is state.
 */
sealed class PlatformEffect {
    /** Opens [url] outside the app: the browser, or the app that owns the link. */
    data class OpenUrl(val url: String) : PlatformEffect()

    /** Opens this app's listing in the platform's store (Google Play or the App Store). */
    data object OpenStoreListing : PlatformEffect()

    /** Puts [text] on the system clipboard. */
    data class CopyToClipboard(val text: String) : PlatformEffect()

    /** Opens the system share sheet with [text]. */
    data class ShareText(val text: String) : PlatformEffect()
}
