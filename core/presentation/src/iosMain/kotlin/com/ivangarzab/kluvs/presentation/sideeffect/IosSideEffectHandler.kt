package com.ivangarzab.kluvs.presentation.sideeffect

import com.ivangarzab.bark.Bark
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

private const val APP_STORE_URL = "https://apps.apple.com/app/id6757938950"

/** Runs [SideEffect]s with UIKit. Must be called on the main thread, as UIKit requires. */
internal class IosSideEffectHandler : SideEffectHandler {

    override fun handle(effect: SideEffect) {
        when (effect) {
            is SideEffect.OpenUrl -> {
                Bark.d("Opening external URL")
                openUrl(effect.url)
            }
            SideEffect.OpenStoreListing -> {
                Bark.i("Opening App Store listing")
                openUrl(APP_STORE_URL)
            }
            is SideEffect.CopyToClipboard -> {
                Bark.d("Copying text to clipboard")
                UIPasteboard.generalPasteboard.string = effect.text
            }
            is SideEffect.ShareText -> {
                Bark.d("Opening share sheet")
                shareText(effect.text)
            }
        }
    }

    private fun openUrl(urlString: String) {
        val url = NSURL.URLWithString(urlString)
        if (url == null) {
            Bark.w("Can't open URL: not a valid URL")
            return
        }
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>()) { opened ->
            if (!opened) Bark.w("No app could open URL")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun shareText(text: String) {
        val presenter = topViewController()
        if (presenter == null) {
            Bark.w("Can't open share sheet: no view controller to present from")
            return
        }
        val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        // iPad shows the share sheet as a popover, which needs an anchor; center it on screen
        controller.popoverPresentationController?.let { popover ->
            popover.sourceView = presenter.view
            presenter.view.bounds.useContents {
                popover.sourceRect = CGRectMake(size.width / 2, size.height / 2, 0.0, 0.0)
            }
        }
        presenter.presentViewController(controller, animated = true, completion = null)
    }

    /** The view controller currently on top, including any sheet presented over the root. */
    private fun topViewController(): UIViewController? {
        val window = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstNotNullOfOrNull { it.keyWindow }
        var top = window?.rootViewController
        while (top?.presentedViewController != null) {
            top = top.presentedViewController
        }
        return top
    }
}
