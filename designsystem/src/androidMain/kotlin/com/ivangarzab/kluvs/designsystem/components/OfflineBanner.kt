package com.ivangarzab.kluvs.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

private val OfflineBannerHeight = 32.dp
private const val OfflineBannerMotionMs = 250
private val OfflineBannerEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/**
 * Persistent banner telling the user the app is offline and showing saved data. Place it at the
 * top of the main tab shell, above the content, so it pushes the content down rather than
 * covering it. See design-system/docs/states.md § Offline Banner.
 *
 * It has no icon, no action and no dismiss: it mirrors live state, shown while [visible] is true.
 * Entry and exit slide and fade over 250ms on the design system's identity easing (the platform's
 * animation scale still applies, so it is instant when system animations are off).
 *
 * This is the 32dp band only. To make the band and the system status bar read as one, the host
 * paints the status-bar area in [com.ivangarzab.kluvs.designsystem.theme.KluvsColors.offline] too
 * (see MainScreen), and sets light status-bar icons while it shows.
 *
 * Announced to screen readers as a polite live region when it appears.
 *
 * @param visible Whether the network is currently unreachable.
 * @param text The message, e.g. "You're offline. Showing saved data."
 */
@Composable
fun OfflineBanner(
    visible: Boolean,
    text: String,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(OfflineBannerMotionMs, easing = OfflineBannerEasing)) +
                expandVertically(tween(OfflineBannerMotionMs, easing = OfflineBannerEasing)),
        exit = fadeOut(tween(OfflineBannerMotionMs, easing = OfflineBannerEasing)) +
                shrinkVertically(tween(OfflineBannerMotionMs, easing = OfflineBannerEasing)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(OfflineBannerHeight)
                .background(KluvsTheme.colors.offline)
                .padding(horizontal = 16.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = KluvsTheme.colors.onOffline,
                style = KluvsTheme.typography.label,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_OfflineBanner() = KluvsTheme {
    Box(modifier = Modifier.background(KluvsTheme.colors.background)) {
        OfflineBanner(visible = true, text = "You're offline. Showing saved data.")
    }
}
