//
//  SettingsViewModelWrapper.swift
//  iosApp
//
import Swift
import Shared


@MainActor
class SettingsViewModelWrapper: ObservableObject {
    @Published var isLoading: Bool = true
    @Published var error: String? = nil
    @Published var profile: Shared.EditableProfile? = nil
    @Published var editedName: String = ""
    @Published var editedHandle: String = ""
    @Published var isSaving: Bool = false
    @Published var saveError: String? = nil
    @Published var handleError: String? = nil
    @Published var saveSuccess: Bool = false
    @Published var hasChanges: Bool = false
    @Published var isUploadingAvatar: Bool = false
    @Published var avatarError: String? = nil
    @Published var newPasswordField: String = ""
    @Published var confirmPasswordField: String = ""
    @Published var newPasswordError: String? = nil
    @Published var confirmPasswordError: String? = nil
    @Published var isChangingPassword: Bool = false
    @Published var changePasswordGeneralError: Shared.AuthError? = nil
    @Published var changePasswordSuccess: Bool = false
    // Sheets and dialogs — owned by the shared ViewModel, see SettingsState.activeSheet
    @Published var activeSheet: Shared.SettingsSheet? = nil
    @Published var activeDialog: Shared.SettingsDialog? = nil
    @Published var isDeletingAccount: Bool = false
    @Published var deleteAccountError: String? = nil

    private let helper: SettingsViewModelHelper
    private var cancellables: [Shared.Closeable] = []

    init() {
        self.helper = SettingsViewModelHelper()
        startObserving()
    }

    private func startObserving() {
        let stateCancellable = helper.observeState { [weak self] state in
            Task { @MainActor [weak self] in
                guard let self else { return }
                self.isLoading = state.isLoading
                self.error = state.error
                self.profile = state.profile
                self.editedName = state.editedName
                self.editedHandle = state.editedHandle
                self.isSaving = state.isSaving
                self.saveError = state.saveError
                self.handleError = state.handleError
                self.saveSuccess = state.saveSuccess
                self.hasChanges = state.hasChanges
                self.isUploadingAvatar = state.isUploadingAvatar
                self.avatarError = state.avatarError
                self.newPasswordField = state.newPasswordField
                self.confirmPasswordField = state.confirmPasswordField
                self.newPasswordError = state.newPasswordError
                self.confirmPasswordError = state.confirmPasswordError
                self.isChangingPassword = state.isChangingPassword
                self.changePasswordGeneralError = state.changePasswordGeneralError
                self.changePasswordSuccess = state.changePasswordSuccess
                self.activeSheet = state.activeSheet
                self.activeDialog = state.activeDialog
                self.isDeletingAccount = state.isDeletingAccount
                self.deleteAccountError = state.deleteAccountError
            }
        }
        cancellables.append(stateCancellable)
    }

    func loadProfile(userId: String) {
        helper.loadProfile(userId: userId)
    }

    func onNameChanged(_ name: String) {
        helper.onNameChanged(name: name)
    }

    func onHandleChanged(_ handle: String) {
        helper.onHandleChanged(handle: handle)
    }

    func onSaveProfile() {
        helper.onSaveProfile()
    }

    func onDismissSaveSuccess() {
        helper.onDismissSaveSuccess()
    }

    func uploadAvatar(imageData: Data) {
        let byteArray = KotlinByteArray(size: Int32(imageData.count))
        imageData.withUnsafeBytes { (bytes: UnsafeRawBufferPointer) in
            if let baseAddress = bytes.baseAddress {
                for i in 0..<imageData.count {
                    byteArray.set(index: Int32(i), value: Int8(bitPattern: baseAddress.load(fromByteOffset: i, as: UInt8.self)))
                }
            }
        }
        helper.uploadAvatar(imageData: byteArray)
    }

    func onAvatarPickFailed(reason: String?) {
        helper.onAvatarPickFailed(reason: reason)
    }

    func clearAvatarError() {
        helper.clearAvatarError()
    }

    // Sheets and dialogs
    func onOpenSheet(_ sheet: Shared.SettingsSheet) { helper.onOpenSheet(sheet: sheet) }
    func onDismissSheet() { helper.onDismissSheet() }
    func onOpenDialog(_ dialog: Shared.SettingsDialog) { helper.onOpenDialog(dialog: dialog) }
    func onDismissDialog() { helper.onDismissDialog() }

    func onNewPasswordFieldChanged(_ value: String) {
        helper.onNewPasswordFieldChanged(value: value)
    }

    func onConfirmPasswordFieldChanged(_ value: String) {
        helper.onConfirmPasswordFieldChanged(value: value)
    }

    func onSubmitChangePassword() {
        helper.onSubmitChangePassword()
    }

    func onDismissChangePasswordSuccess() {
        helper.onDismissChangePasswordSuccess()
    }

    func onDeleteAccountConfirmed() {
        helper.onDeleteAccountConfirmed()
    }

    func clearDeleteAccountError() {
        helper.clearDeleteAccountError()
    }

    deinit {
        cancellables.forEach { $0.close() }
    }
}
