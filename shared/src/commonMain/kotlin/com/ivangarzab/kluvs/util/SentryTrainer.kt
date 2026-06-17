package com.ivangarzab.kluvs.util

import com.ivangarzab.bark.Level
import com.ivangarzab.bark.Pack
import com.ivangarzab.bark.Trainer
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryLevel
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * A barK Trainer that pipes logs into Sentry.
 *
 * Where:
 * - Milestones (INFO/WARN) become Breadcrumbs (Free).
 * - Issues (ERROR/CRITICAL) become captured Events (Quota-impacting).
 */
class SentryTrainer : Trainer {

    override val minLevel = Level.INFO

    override val pack = Pack.CUSTOM

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun handle(
        level: Level,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        scope.launch {
            // Add as a Breadcrumb for historical context (up to 100 attached on crash report)
            val crumb = Breadcrumb().apply {
                this.category = tag
                this.message = message
                this.level = when (level) {
                    Level.WARNING -> SentryLevel.WARNING
                    Level.ERROR -> SentryLevel.ERROR
                    Level.CRITICAL -> SentryLevel.FATAL
                    else -> SentryLevel.INFO // Covers INFO, and technically DEBUG/VERBOSE if they ever leak
                }
            }
            Sentry.addBreadcrumb(crumb)

            // Report actual exceptions/errors
            // We only "capture" (send to Sentry's servers immediately) ERROR and CRITICAL.
            if (level == Level.ERROR || level == Level.CRITICAL) {
                if (throwable != null) {
                    Sentry.captureException(throwable)
                } else {
                    // If there is no exception object, capture the message as an Error event
                    Sentry.captureMessage("[$tag] $message")
                }
            }
        }
    }
}