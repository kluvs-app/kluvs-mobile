//
//  SignupView.swift
//  iosApp
//
//  Thin wrapper for signup mode
//
import SwiftUI

struct SignupView: View {
    @StateObject private var viewModel = AuthViewModelWrapper()
    let onNavigateToLogin: () -> Void
    let onNavigateToForgotPassword: () -> Void

    var body: some View {
        Group {
            switch viewModel.authState {
            case .loading:
                LoadingView()
            case .authenticated:
                EmptyView()
            case .unauthenticated, .error, .emailConfirmationPending:
                AuthFormView(
                    mode: .signup,
                    viewModel: viewModel,
                    onNavigate: { navigation in
                        switch navigation {
                        case .signIn:
                            onNavigateToLogin()
                        case .forgetPassword:
                            onNavigateToForgotPassword()
                        default:
                            break
                        }
                    }
                )
            }
        }
        .authSignupSentSheet(
            isPresented: isEmailConfirmationPendingBinding,
            email: emailConfirmationPendingEmail,
            onDismiss: { viewModel.dismissEmailConfirmationSheet() }
        )
    }

    private var emailConfirmationPendingEmail: String {
        if case .emailConfirmationPending(let email) = viewModel.authState {
            return email
        }
        return ""
    }

    private var isEmailConfirmationPendingBinding: Binding<Bool> {
        Binding(
            get: {
                if case .emailConfirmationPending = viewModel.authState {
                    return true
                }
                return false
            },
            set: { newValue in
                if !newValue {
                    viewModel.dismissEmailConfirmationSheet()
                }
            }
        )
    }
}

#Preview {
    SignupView(
        onNavigateToLogin: {},
        onNavigateToForgotPassword: {}
    )
}
