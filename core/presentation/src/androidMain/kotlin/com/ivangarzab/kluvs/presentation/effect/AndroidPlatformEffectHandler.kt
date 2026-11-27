package com.ivangarzab.kluvs.presentation.effect

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ivangarzab.bark.Bark

private const val PACKAGE_NAME = "com.ivangarzab.kluvs"
private const val PLAY_STORE_APP_URL = "market://details?id=$PACKAGE_NAME"
private const val PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id=$PACKAGE_NAME"

/**
 * Runs [PlatformEffect]s with the application [Context]. Activities started from it need
 * [Intent.FLAG_ACTIVITY_NEW_TASK], since there is no Activity to start them from.
 */
internal class AndroidPlatformEffectHandler(private val context: Context) : PlatformEffectHandler {

    override fun handle(effect: PlatformEffect) {
        when (effect) {
            is PlatformEffect.OpenUrl -> {
                Bark.d("Opening external URL")
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)))
            }
            PlatformEffect.OpenStoreListing -> {
                Bark.i("Opening Play Store listing")
                // The Play Store app when it's installed, the web listing otherwise
                if (!startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_APP_URL)))) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_WEB_URL)))
                }
            }
            is PlatformEffect.CopyToClipboard -> {
                Bark.d("Copying text to clipboard")
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                clipboard.setPrimaryClip(ClipData.newPlainText(null, effect.text))
            }
            is PlatformEffect.ShareText -> {
                Bark.d("Opening share sheet")
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.text)
                }
                // Android 10+ ignores chooser titles, so none is passed
                startActivity(Intent.createChooser(send, null))
            }
        }
    }

    /** Returns false when no app on the device can handle [intent]. */
    private fun startActivity(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        Bark.w("No app can handle intent (Action: ${intent.action})", e)
        false
    }
}
