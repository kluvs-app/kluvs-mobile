import AuthenticationServices
import SwiftUI
import Shared
//
//  AuthFormView.swift
//  iosApp
//
//  Main auth form UI (equivalent to Android's AuthFormContent)
//
import DesignSystem

struct AuthFormView: View {
    let mode: AuthMode
    @ObservedObject var viewModel: AuthViewModelWrapper
    let onNavigate: (LoginNavigation) -> Void

    @FocusState private var focusedField: Field?
    @State private var snackbarData: SnackbarData?

    enum Field {
        case email, password, confirmPassword
    }

    /// Friendly copy for an Apple Sign-In failure, keyed off Apple's error code rather than its
    /// system-generated description. `.unknown` is what iOS reports when the device has no
    /// iCloud account signed in, so it gets an actionable hint. The raw error goes to Bark.
    private func appleSignInMessage(for error: Error) -> String {
        guard let authError = error as? ASAuthorizationError else {
            return String(localized: "error_apple_sign_in_failed")
        }
        switch authError.code {
        case .unknown:
            return String(localized: "error_apple_sign_in_icloud")
        default:
            return String(localized: "error_apple_sign_in_failed")
        }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Spacer()
                    .frame(height: 24)

                AuthMasthead(
                    voicePhrase: mode == .login
                        ? String(localized: "headline_welcome_back")
                        : String(localized: "headline_welcome_in"),
                    subhead: mode == .login
                        ? String(localized: "hint_sign_in_subhead")
                        : String(localized: "hint_sign_up_subhead")
                )

                Spacer().frame(height: 8)

                SocialButton(
                    text: String(localized: "button_continue_apple"),
                    icon: IconType.apple,
                    backgroundColor: .black,
                    textColor: .white,
                    action: {
                        AppleSignInHandler.shared.signIn { result in
                            switch result {
                            case .success(let idToken):
                                viewModel.signInWithApple(idToken: idToken)
                            case .failure(let error):
                                // Don't show error for user cancellation
                                if let authError = error as? ASAuthorizationError,
                                   authError.code == .canceled {
                                    return
                                }
                                Bark.e("Apple Sign-In failed", throwable: error)
                                snackbarData = SnackbarData(message: appleSignInMessage(for: error), variant: .danger)
                            }
                        }
                    }
                )

                SocialButton(
                    text: String(localized: "button_continue_discord"),
                    icon: IconType.discord,
                    backgroundColor: .discordBlue,
                    textColor: .white,
                    action: {
                        viewModel.signInWithDiscord()
                    }
                )

                SocialButton(
                    text: String(localized: "button_continue_google"),
                    icon: IconType.google,
                    backgroundColor: .googleGray,
                    textColor: .googleTextGray,
                    action: {
                        viewModel.signInWithGoogle()
                    },
                    iconSize: 24
                )

                TextDividerView(text: String(localized: "hint_or_continue_email"))

                // Email field
                InputField(
                    label: String(localized: "label_email"),
                    value: Binding(
                        get: { viewModel.emailField },
                        set: { viewModel.onEmailChanged($0) }
                    ),
                    error: viewModel.emailError,
                    helperText: viewModel.emailError == nil ? String(localized: "hint_email") : nil,
                    keyboardType: .emailAddress,
                    submitLabel: .next,
                    onSubmit: { focusedField = .password }
                )
                .focused($focusedField, equals: .email)

                // Password field
                PasswordField(
                    label: String(localized: "label_password"),
                    value: Binding(
                        get: { viewModel.passwordField },
                        set: { viewModel.onPasswordChanged($0) }
                    ),
                    error: viewModel.passwordError,
                    helperText: viewModel.passwordError == nil ? (mode == .login ? String(localized: "hint_password_login") : String(localized: "hint_password_signup")) : nil,
                    submitLabel: mode == .login ? .go : .next,
                    onSubmit: {
                        if mode == .login {
                            viewModel.signIn()
                        } else {
                            focusedField = .confirmPassword
                        }
                    }
                )
                .focused($focusedField, equals: .password)

                // Confirm password (signup only)
                if mode == .signup {
                    PasswordField(
                        label: String(localized: "label_confirm_password"),
                        value: Binding(
                            get: { viewModel.confirmPasswordField },
                            set: { viewModel.onConfirmPasswordChanged($0) }
                        ),
                        error: viewModel.confirmPasswordError,
                        helperText: viewModel.confirmPasswordError == nil ? String(localized: "hint_confirm_password") : nil,
                        submitLabel: .go,
                        onSubmit: { viewModel.signUp() }
                    )
                    .focused($focusedField, equals: .confirmPassword)
                }

                // Forgot password (login only)
                if mode == .login {
                    HStack {
                        Spacer()
                        TextButton(text: "Forgot password?", action: { onNavigate(.forgetPassword) }, emphasized: true)
                    }
                    .frame(height: 20) // Fixed height to match signup spacing
                }

                // Submit button
                PrimaryButton(text: mode == .login ? "Sign In" : "Sign Up", action: {
                    if mode == .login {
                        viewModel.signIn()
                    } else {
                        viewModel.signUp()
                    }
                })
                .frame(maxWidth: .infinity)

                // Navigation link
                HStack(spacing: 4) {
                    Text(mode == .login ? "Don't have an account?" : "Already have an account?")
                        .kluvsStyle(KluvsTheme.typography.body.medium)
                        .foregroundColor(KluvsTheme.colors.contentMuted)

                    Button(mode == .login ? "Sign Up" : "Sign In") {
                        onNavigate(mode == .login ? .signUp : .signIn)
                    }
                    .kluvsStyle(KluvsTheme.typography.label)
                    .foregroundColor(KluvsTheme.colors.accent)
                }

                Spacer().frame(height: 12)

                AuthFooter()

                Spacer()
            }
            .padding(16)
        }
        // Without this, `.snackbar`'s bottom-anchored overlay positions itself against the
        // ScrollView's content height (landing next to AuthFooter) instead of the actual screen
        // bottom — a ScrollView only claims as much height as its content needs unless told
        // otherwise.
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .snackbar($snackbarData)
        // `.task(id:)`, not `.onChange` — this view is recreated fresh each time the parent
        // switches away from `.loading` back to `.error` (a different switch case), so by the
        // time it exists the state has already "changed" from `.onChange`'s perspective and it
        // never fires. `.task(id:)` runs immediately on appearance for the current id too,
        // matching Android's `LaunchedEffect(key)` semantics.
        .task(id: viewModel.authState) {
            if case .error(let error) = viewModel.authState {
                snackbarData = SnackbarData(message: error.toLocalizedMessage(), variant: .danger)
            }
        }
    }
}

enum AuthMode {
    case login
    case signup
}

enum LoginNavigation {
    case signIn
    case signUp
    case forgetPassword
}

#Preview {
    AuthFormView(
        mode: .login,
        viewModel: AuthViewModelWrapper(),
        onNavigate: { _ in }
    )
}
