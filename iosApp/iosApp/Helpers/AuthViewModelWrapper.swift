//
//  AuthViewModelWrapper.swift
//  iosApp
//
//  iOS wrapper for shared AuthViewModel
//
import Swift
import Shared

@MainActor
class AuthViewModelWrapper: ObservableObject {
    // Auth state
    @Published var authState: AuthStateWrapper = .unauthenticated

    // UI state - form fields
    @Published var emailField: String = ""
    @Published var passwordField: String = ""
    @Published var confirmPasswordField: String = ""

    // UI state - validation errors
    @Published var emailError: String? = nil
    @Published var passwordError: String? = nil
    @Published var confirmPasswordError: String? = nil

    private let helper: AuthViewModelHelper
    /// What `.screenEffects(_:)` listens to: this screen's snackbars.
    var screenEffectSource: ScreenEffectSource { helper }
    private var cancellables: [Shared.Closeable] = []

    init() {
        self.helper = AuthViewModelHelper()
        startObserving()
    }

    private func startObserving() {
        let stateCancellable = helper.observeState { [weak self] state in
            Task { @MainActor [weak self] in
                guard let self else { return }
                self.authState = AuthStateWrapper.from(state.authState)
                self.emailField = state.emailField
                self.passwordField = state.passwordField
                self.confirmPasswordField = state.confirmPasswordField
                self.emailError = state.emailError
                self.passwordError = state.passwordError
                self.confirmPasswordError = state.confirmPasswordError
            }
        }
        cancellables.append(stateCancellable)
    }

    func onEmailChanged(_ value: String) {
        helper.onEmailFieldChanged(value: value)
    }

    func onPasswordChanged(_ value: String) {
        helper.onPasswordFieldChanged(value: value)
    }

    func onConfirmPasswordChanged(_ value: String) {
        helper.onConfirmPasswordFieldChanged(value: value)
    }

    func signIn() {
        helper.validateAndSignIn()
    }

    func signUp() {
        helper.validateAndSignUp()
    }

    func signInWithDiscord() {
        helper.signInWithDiscord()
    }

    func signInWithGoogle() {
        helper.signInWithGoogle()
    }

    func handleOAuthCallback(callbackUrl: String) {
        helper.handleOAuthCallback(callbackUrl: callbackUrl)
    }

    func signInWithApple(idToken: String) {
        helper.signInWithApple(idToken: idToken)
    }

    func dismissEmailConfirmationSheet() {
        helper.dismissEmailConfirmationSheet()
    }

    deinit {
        cancellables.forEach { $0.close() }
        helper.close()
    }
}

// Swift-friendly enum wrapper for AuthState
enum AuthStateWrapper: Equatable {
    case unauthenticated
    case loading
    case authenticated(user: Shared.User)
    case emailConfirmationPending(email: String)

    // Custom Equatable implementation since Kotlin classes may not be Equatable
    static func == (lhs: AuthStateWrapper, rhs: AuthStateWrapper) -> Bool {
        switch (lhs, rhs) {
        case (.unauthenticated, .unauthenticated):
            return true
        case (.loading, .loading):
            return true
        case (.authenticated(let lUser), .authenticated(let rUser)):
            return lUser.id == rUser.id
        case (.emailConfirmationPending(let lEmail), .emailConfirmationPending(let rEmail)):
            return lEmail == rEmail
        default:
            return false
        }
    }

    static func from(_ kotlinState: Shared.AuthState) -> AuthStateWrapper {
        if kotlinState is Shared.AuthState.Unauthenticated {
            return .unauthenticated
        } else if kotlinState is Shared.AuthState.Loading {
            return .loading
        } else if let authenticated = kotlinState as? Shared.AuthState.Authenticated {
            return .authenticated(user: authenticated.user)
        } else if let emailConfirmationPending = kotlinState as? Shared.AuthState.EmailConfirmationPending {
            return .emailConfirmationPending(email: emailConfirmationPending.email)
        }
        return .unauthenticated
    }
}
