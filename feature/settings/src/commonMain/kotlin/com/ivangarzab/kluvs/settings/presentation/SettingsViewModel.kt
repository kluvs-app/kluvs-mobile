package com.ivangarzab.kluvs.settings.presentation

import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.auth.domain.SignOutUseCase
import com.ivangarzab.kluvs.presentation.effect.NoticeKey
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import com.ivangarzab.kluvs.presentation.effect.SnackbarMessage
import com.ivangarzab.kluvs.presentation.error.toDisplayMessage
import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModel
import com.ivangarzab.kluvs.settings.domain.ChangePasswordUseCase
import com.ivangarzab.kluvs.settings.domain.DeleteAccountUseCase
import com.ivangarzab.kluvs.settings.domain.GetEditableProfileUseCase
import com.ivangarzab.kluvs.settings.domain.UpdateAvatarUseCase
import com.ivangarzab.kluvs.settings.domain.UpdateUserProfileUseCase
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Settings screen.
 *
 * Loads the current user's profile for editing and handles save operations.
 */
class SettingsViewModel(
    private val getEditableProfile: GetEditableProfileUseCase,
    private val updateUserProfile: UpdateUserProfileUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val signOutUseCase: SignOutUseCase
) : BaseViewModel<SettingsState>(SettingsState()) {

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            getEditableProfile(userId)
                .onSuccess { profile ->
                    Bark.i("Settings profile loaded (Member ID: ${profile.memberId})")
                    updateState {
                        it.copy(
                            isLoading = false,
                            profile = profile,
                            editedName = profile.name,
                            editedHandle = profile.handle,
                            hasChanges = false
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to load profile for Settings screen.", error)
                    updateState { it.copy(isLoading = false, error = error.toDisplayMessage()) }
                }
        }
    }

    fun onNameChanged(name: String) {
        updateState { state ->
            state.copy(
                editedName = name,
                hasChanges = computeHasChanges(state.copy(editedName = name))
            )
        }
    }

    /**
     * Case is a soft transform — uppercase input is silently lowercased as the user types, since
     * the backend's handle format is lowercase-only. Any other out-of-charset character (e.g.
     * underscore, accented letters) is kept as typed and surfaced as [SettingsState.handleError]
     * instead, so the user sees exactly what they typed and why it's rejected. Full structural
     * validation (segment/hyphen rules, length) happens in [UpdateUserProfileUseCase] on save —
     * only the server can authoritatively confirm the handle is free.
     */
    fun onHandleChanged(handle: String) {
        val lowered = handle.lowercase()
        val handleError = if (HANDLE_CHARSET_REGEX.matches(lowered)) {
            null
        } else {
            "Handles can only contain lowercase letters, numbers, and hyphens"
        }
        updateState { state ->
            val updated = state.copy(editedHandle = lowered, handleError = handleError)
            updated.copy(hasChanges = computeHasChanges(updated))
        }
    }

    fun onSaveProfile() {
        val state = currentState
        if (state.isSaving || state.handleError != null) return
        val profile = state.profile ?: return

        viewModelScope.launch {
            updateState { it.copy(isSaving = true, saveError = null) }

            updateUserProfile(profile.memberId, state.editedName, state.editedHandle)
                .onSuccess {
                    Bark.i("Profile saved successfully (Member ID: ${profile.memberId})")
                    updateState {
                        it.copy(
                            isSaving = false,
                            profile = it.profile?.copy(name = it.editedName, handle = it.editedHandle),
                            hasChanges = false
                        )
                    }
                    send(ScreenEffect.ShowSnackbar(SnackbarMessage.Notice(NoticeKey.SAVE_SUCCESS)))
                }
                .onFailure { error ->
                    // Covers the 409 "handle already taken" case — AppError.Conflict carries the
                    // backend's own clean, human-readable detail via toDisplayMessage().
                    Bark.e("Failed to save profile (Member ID: ${profile.memberId}).", error)
                    updateState {
                        it.copy(isSaving = false, saveError = error.toDisplayMessage())
                    }
                }
        }
    }

    fun uploadAvatar(imageData: ByteArray) {
        viewModelScope.launch {
            updateState { it.copy(isUploadingAvatar = true, avatarError = null) }

            val memberId = currentState.profile?.memberId
            if (memberId == null) {
                Bark.e("No member ID available to update avatar. Please retry.", null)
                updateState {
                    it.copy(
                        isUploadingAvatar = false,
                        avatarError = "No member ID available"
                    )
                }
                return@launch
            }

            updateAvatarUseCase(memberId, imageData)
                .onSuccess { newAvatarUrl ->
                    Bark.i("Avatar uploaded successfully (ID: $memberId)")
                    updateState {
                        it.copy(profile = it.profile?.copy(avatarUrl = newAvatarUrl))
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to upload avatar (ID: $memberId). Please retry.", error)
                    updateState { it.copy(avatarError = error.toDisplayMessage()) }
                }

            updateState { it.copy(isUploadingAvatar = false) }
        }
    }

    /** Surfaces a failure that happened while reading/compressing a picked image, before upload was even attempted. */
    fun onAvatarPickFailed(reason: String?) {
        Bark.e("Failed to read picked avatar image. ${reason.orEmpty()}", null)
        updateState { it.copy(avatarError = "Failed to read selected image") }
    }

    fun clearAvatarError() {
        updateState { it.copy(avatarError = null) }
    }

    /** Opens [sheet], replacing any sheet already open. */
    fun onOpenSheet(sheet: SettingsSheet) {
        updateState { it.copy(activeSheet = sheet) }
    }

    /** Closes the open sheet: Cancel, the close button, swipe down, back, or a tap outside. */
    fun onDismissSheet() {
        currentState.activeSheet?.let { closeSheet(ifShowing = it) }
    }

    /** Opens [dialog] on top of the screen. */
    fun onOpenDialog(dialog: SettingsDialog) {
        updateState { it.copy(activeDialog = dialog) }
    }

    /** Closes the open dialog. Confirming a dialog closes it too. */
    fun onDismissDialog() {
        updateState { it.copy(activeDialog = null) }
    }

    fun onNewPasswordFieldChanged(value: String) {
        updateState { it.copy(newPasswordField = value, newPasswordError = null) }
    }

    fun onConfirmPasswordFieldChanged(value: String) {
        updateState { it.copy(confirmPasswordField = value, confirmPasswordError = null) }
    }

    fun onSubmitChangePassword() {
        val state = currentState
        if (state.isChangingPassword) return

        val newPasswordError = when {
            state.newPasswordField.isBlank() -> "Password is required"
            state.newPasswordField.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        val confirmPasswordError = when {
            state.newPasswordField != state.confirmPasswordField -> "Passwords must match"
            else -> null
        }

        if (newPasswordError != null || confirmPasswordError != null) {
            updateState {
                it.copy(newPasswordError = newPasswordError, confirmPasswordError = confirmPasswordError)
            }
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isChangingPassword = true, changePasswordGeneralError = null) }

            changePasswordUseCase(state.newPasswordField)
                .onSuccess {
                    Bark.i("Password changed successfully")
                    updateState {
                        it.copy(
                            isChangingPassword = false,
                            newPasswordField = "",
                            confirmPasswordField = ""
                        )
                    }
                    closeSheet(ifShowing = SettingsSheet.ChangePassword)
                    send(ScreenEffect.ShowSnackbar(SnackbarMessage.Notice(NoticeKey.PASSWORD_UPDATED)))
                }
                .onFailure { error ->
                    Bark.e("Failed to change password.", error)
                    updateState {
                        it.copy(
                            isChangingPassword = false,
                            changePasswordGeneralError = (error as? AuthError) ?: AuthError.UnexpectedError
                        )
                    }
                }
        }
    }

    /**
     * Deletes the signed-in member's account, then signs out and clears local data on success.
     * AppCoordinator observes auth state and navigates to the auth flow automatically — the same
     * path taken after a normal sign-out (see MeViewModel.onSignOutDialogConfirmed).
     *
     * Runs [NonCancellable] so that if this ViewModel is cleared mid-request, the deletion and
     * the sign-out that follows still finish together. Otherwise the server could delete the
     * account while the client never signs out.
     */
    fun onDeleteAccountConfirmed() {
        val state = currentState
        if (state.isDeletingAccount) return
        val memberId = state.profile?.memberId ?: return

        viewModelScope.launch {
            // Close the dialog up front (same as MeViewModel's sign-out) — on iOS the dialog's
            // binding hides itself locally on confirm, so leaving this true would re-present it.
            updateState {
                it.copy(activeDialog = null, isDeletingAccount = true, deleteAccountError = null)
            }

            withContext(NonCancellable) { deleteAccountUseCase(memberId) }
                .onSuccess {
                    Bark.i("Account deleted successfully (Member ID: $memberId)")
                    // SignOutUseCase clears the local session and cache even if the server call
                    // fails, so the user always lands on the auth flow from here.
                    withContext(NonCancellable) { signOutUseCase() }.onFailure { error ->
                        Bark.w("Server sign-out after account deletion failed; local session cleared anyway.", error)
                    }
                    updateState { it.copy(isDeletingAccount = false) }
                }
                .onFailure { error ->
                    updateState {
                        it.copy(
                            isDeletingAccount = false,
                            deleteAccountError = error.toDisplayMessage()
                        )
                    }
                }
        }
    }

    fun clearDeleteAccountError() {
        updateState { it.copy(deleteAccountError = null) }
    }

    private fun computeHasChanges(state: SettingsState): Boolean {
        val profile = state.profile ?: return false
        return state.editedName != profile.name || state.editedHandle != profile.handle
    }

    companion object {
        // Charset-only check for live typing feedback — lowercase letters, digits, and hyphens.
        // Full structural validation (segment/hyphen placement, length) is enforced at save time
        // by UpdateUserProfileUseCase's HANDLE_REGEX, which matches the backend exactly.
        private val HANDLE_CHARSET_REGEX = Regex("^[a-z0-9-]*$")
    }

    /**
     * Closes the open sheet if it's still [ifShowing]: after a successful save the user may have
     * dismissed it mid-save and opened another one, which must stay open. Closing Change Password
     * clears its fields so it opens empty next time.
     */
    private fun closeSheet(ifShowing: SettingsSheet) {
        updateState {
            when {
                it.activeSheet != ifShowing -> it
                ifShowing == SettingsSheet.ChangePassword -> it.copy(
                    activeSheet = null,
                    newPasswordField = "",
                    confirmPasswordField = "",
                    newPasswordError = null,
                    confirmPasswordError = null,
                    changePasswordGeneralError = null
                )
                else -> it.copy(activeSheet = null)
            }
        }
    }
}
