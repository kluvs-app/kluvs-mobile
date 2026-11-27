//
//  LoginView.swift
//  iosApp
//
//  Thin wrapper for login mode
//
import SwiftUI

struct LoginView: View {
    @StateObject private var viewModel = AuthViewModelWrapper()
    let onNavigateToSignup: () -> Void
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
                    mode: .login,
                    viewModel: viewModel,
                    onNavigate: { navigation in
                        switch navigation {
                        case .signUp:
                            onNavigateToSignup()
                        case .forgetPassword:
                            onNavigateToForgotPassword()
                        default:
                            break
                        }
                    }
                )
            }
        }
    }
}

#Preview {
    LoginView(
        onNavigateToSignup: {},
        onNavigateToForgotPassword: {}
    )
}
