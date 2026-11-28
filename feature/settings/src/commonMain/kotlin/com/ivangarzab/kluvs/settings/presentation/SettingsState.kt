package com.ivangarzab.kluvs.settings.presentation

import com.ivangarzab.kluvs.auth.domain.AuthError

data class SettingsState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val profile: EditableProfile? = null,
    // Editable field values (user's in-progress edits)
    val editedName: String = "",
    val editedHandle: String = "",
    // Inline format validation for editedHandle (e.g. disallowed characters); null when valid
    val handleError: String? = null,
    // Save operation state
    val isSaving: Boolean = false,
    val saveError: String? = null,
    // Derived: are there unsaved changes?
    val hasChanges: Boolean = false,
    // Avatar upload state
    val isUploadingAvatar: Boolean = false,
    // Change password sheet state
    val newPasswordField: String = "",
    val confirmPasswordField: String = "",
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isChangingPassword: Boolean = false,
    val changePasswordGeneralError: AuthError? = null,
    // Delete account state
    val isDeletingAccount: Boolean = false,
    /** The bottom sheet on screen, if any. Opened with `onOpenSheet`, closed by `onDismissSheet` or a successful save. */
    val activeSheet: SettingsSheet? = null,
    /** The confirmation dialog on screen, if any. */
    val activeDialog: SettingsDialog? = null
)

/** Bottom sheets on the Settings screen. At most one is open at a time. */
sealed class SettingsSheet {
    data object ChangePassword : SettingsSheet()
    data object Credits : SettingsSheet()
}

/** Confirmation dialogs on the Settings screen. */
sealed class SettingsDialog {
    data object DeleteAccount : SettingsDialog()
}
