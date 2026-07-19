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

    // Forgot password UI state
    @Published var forgotPasswordEmailField: String = ""
    @Published var forgotPasswordEmailError: String? = nil
    @Published var isForgotPasswordLoading: Bool = false
    @Published var isForgotPasswordEmailSent: Bool = false
    @Published var forgotPasswordErrorMessage: String? = nil

    private let helper: AuthViewModelHelper
    private var cancellables: [Shared.Closeable] = []

    init() {
        self.helper = AuthViewModelHelper()
        startObserving()
    }

    private func startObserving() {
        // Observe auth state
        let stateCancellable = helper.observeState { [weak self] state in
            DispatchQueue.main.async {
                self?.authState = AuthStateWrapper.from(state)
            }
        }
        cancellables.append(stateCancellable)

        // Observe UI state
        let uiStateCancellable = helper.observeUiState { [weak self] uiState in
            DispatchQueue.main.async {
                self?.emailField = uiState.emailField
                self?.passwordField = uiState.passwordField
                self?.confirmPasswordField = uiState.confirmPasswordField
                self?.emailError = uiState.emailError
                self?.passwordError = uiState.passwordError
                self?.confirmPasswordError = uiState.confirmPasswordError
            }
        }
        cancellables.append(uiStateCancellable)

        // Observe forgot password state
        let forgotPasswordCancellable = helper.observeForgotPasswordState { [weak self] state in
            DispatchQueue.main.async {
                self?.forgotPasswordEmailField = state.emailField
                self?.forgotPasswordEmailError = state.emailError
                self?.isForgotPasswordLoading = state.isLoading
                self?.isForgotPasswordEmailSent = state.isEmailSent
                self?.forgotPasswordErrorMessage = state.generalError?.toLocalizedMessage()
            }
        }
        cancellables.append(forgotPasswordCancellable)
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

    func signOut() {
        helper.signOut()
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

    func onOAuthUrlLaunched() {
        helper.onOAuthUrlLaunched()
    }

    func signInWithApple(idToken: String) {
        helper.signInWithApple(idToken: idToken)
    }

    func onForgotPasswordEmailChanged(_ value: String) {
        helper.onForgotPasswordEmailChanged(value: value)
    }

    func sendPasswordResetEmail() {
        helper.sendPasswordResetEmail()
    }

    func resetForgotPasswordState() {
        helper.resetForgotPasswordState()
    }

    deinit {
        cancellables.forEach { $0.close() }
    }
}

// Swift-friendly enum wrapper for AuthState
enum AuthStateWrapper: Equatable {
    case unauthenticated
    case loading
    case authenticated(user: Shared.User)
    case error(error: Shared.AuthError)
    case oauthPending(url: String) // Why not just loading?

    // Custom Equatable implementation since Kotlin classes may not be Equatable
    static func == (lhs: AuthStateWrapper, rhs: AuthStateWrapper) -> Bool {
        switch (lhs, rhs) {
        case (.unauthenticated, .unauthenticated):
            return true
        case (.loading, .loading):
            return true
        case (.authenticated(let lUser), .authenticated(let rUser)):
            return lUser.id == rUser.id
        case (.error, .error):
            return true // We just care that it's an error state
        case (.oauthPending(let lUrl), .oauthPending(let rUrl)):
            return lUrl == rUrl
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
        } else if let error = kotlinState as? Shared.AuthState.Error {
            return .error(error: error.error)
        } else if let oauthPending = kotlinState as? Shared.AuthState.OAuthPending {
            return .oauthPending(url: oauthPending.url)
        }
        return .unauthenticated
    }
}
