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
    val saveSuccess: Boolean = false,
    // Derived: are there unsaved changes?
    val hasChanges: Boolean = false,
    // Avatar upload state
    val isUploadingAvatar: Boolean = false,
    val avatarError: String? = null,
    // Change password sheet state
    val isChangePasswordSheetOpen: Boolean = false,
    val newPasswordField: String = "",
    val confirmPasswordField: String = "",
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isChangingPassword: Boolean = false,
    val changePasswordGeneralError: AuthError? = null,
    val changePasswordSuccess: Boolean = false,
    // Delete account state
    val isDeleteAccountDialogOpen: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val deleteAccountError: String? = null
)
