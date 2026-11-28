//
//  ForgotPasswordViewModelWrapper.swift
//  iosApp
//
//  iOS wrapper for shared ForgotPasswordViewModel
//
import Swift
import Shared

@MainActor
class ForgotPasswordViewModelWrapper: ObservableObject {
    @Published var emailField: String = ""
    @Published var emailError: String? = nil
    @Published var isLoading: Bool = false
    @Published var isEmailSent: Bool = false

    private let helper: ForgotPasswordViewModelHelper
    /// What `.screenEffects(_:)` listens to: this screen's snackbars.
    var screenEffectSource: ScreenEffectSource { helper }
    private var cancellables: [Shared.Closeable] = []

    init() {
        self.helper = ForgotPasswordViewModelHelper()
        let stateCancellable = helper.observeState { [weak self] state in
            Task { @MainActor [weak self] in
                guard let self else { return }
                self.emailField = state.emailField
                self.emailError = state.emailError
                self.isLoading = state.isLoading
                self.isEmailSent = state.isEmailSent
            }
        }
        cancellables.append(stateCancellable)
    }

    func onEmailFieldChanged(_ value: String) {
        helper.onEmailFieldChanged(value: value)
    }

    func onSendResetLink() {
        helper.onSendResetLink()
    }

    deinit {
        cancellables.forEach { $0.close() }
        helper.close()
    }
}
