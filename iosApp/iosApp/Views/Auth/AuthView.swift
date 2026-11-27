//
//  AuthView.swift
//  iosApp
//
//  Single view that toggles between login/signup modes (standard iOS pattern)
//
import SwiftUI

struct AuthView: View {
    @StateObject private var viewModel = AuthViewModelWrapper()
    @StateObject private var oauthHandler = OAuthCallbackHandler.shared
    @State private var authMode: AuthMode = .login
    @State private var showForgotSheet = false

    var body: some View {
        Group {
            switch viewModel.authState {
            case .loading:
                LoadingView()
            case .authenticated:
                EmptyView() // Navigation handled by AppCoordinator
            case .unauthenticated, .error, .emailConfirmationPending:
                AuthFormView(
                    mode: authMode,
                    viewModel: viewModel,
                    onNavigate: { navigation in
                        switch navigation {
                        case .signUp:
                            withAnimation {
                                authMode = .signup
                            }
                        case .signIn:
                            withAnimation {
                                authMode = .login
                            }
                        case .forgetPassword:
                            showForgotSheet = true
                        }
                    }
                )
            }
        }
        .onChange(of: oauthHandler.callbackUrl) { _, newUrl in
            if let url = newUrl {
                viewModel.handleOAuthCallback(callbackUrl: url.absoluteString)
                oauthHandler.clearCallback()
            }
        }
        .authForgotSheet(isPresented: $showForgotSheet, viewModel: viewModel)
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
    AuthView()
}
