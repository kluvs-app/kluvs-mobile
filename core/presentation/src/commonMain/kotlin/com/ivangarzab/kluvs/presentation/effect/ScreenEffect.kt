package com.ivangarzab.kluvs.presentation.effect

/**
 * A one-shot event the visible screen reacts to (show a snackbar): it needs this screen and
 * leaves nothing behind once it's shown. One shared listener per platform shows every case, so
 * screens never handle them one by one.
 *
 * Cases are named after what the screen does, never why: `ShowSnackbar`, not `ShowSaveSuccess`.
 */
sealed class ScreenEffect {
    /**
     * Shows a snackbar. Duration and dismissal follow design-system/docs/states.md § Snackbar;
     * [tone] is the only style choice.
     */
    data class ShowSnackbar(
        val message: SnackbarMessage,
        val tone: SnackbarTone = SnackbarTone.SUCCESS
    ) : ScreenEffect() {
        companion object {
            /** A success snackbar with text built in Kotlin. */
            fun success(text: String) = ShowSnackbar(SnackbarMessage.Text(text), SnackbarTone.SUCCESS)

            /** A danger snackbar with text built in Kotlin, usually from `toDisplayMessage()`. */
            fun error(text: String) = ShowSnackbar(SnackbarMessage.Text(text), SnackbarTone.DANGER)
        }
    }
}

/** What a snackbar says. */
sealed class SnackbarMessage {
    /** Text already built in Kotlin, such as an error from `toDisplayMessage()`. */
    data class Text(val text: String) : SnackbarMessage()

    /** A notice each platform's listener turns into its own string resource. */
    data class Notice(val key: NoticeKey) : SnackbarMessage()
}

/** Snackbar notices whose text lives in platform string resources. */
enum class NoticeKey {
    SAVE_SUCCESS,
    PASSWORD_UPDATED,

    // Auth failures, one per user-facing `AuthError` (mapped in feature/auth)
    AUTH_INVALID_CREDENTIALS,
    AUTH_EMAIL_NOT_CONFIRMED,
    AUTH_NO_CONNECTION,
    AUTH_RATE_LIMIT_EXCEEDED,
    AUTH_USER_NOT_FOUND,
    AUTH_WEAK_PASSWORD,
    AUTH_USER_ALREADY_EXISTS,
    AUTH_AUTHENTICATION_FAILED,
    AUTH_UNEXPECTED_ERROR
}

/** The design system's two snackbar tones. */
enum class SnackbarTone {
    SUCCESS,
    DANGER
}
