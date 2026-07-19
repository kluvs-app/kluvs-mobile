//
//  ForgotPasswordView.swift
//  iosApp
//
//  Forgot password screen, backed by the shared AuthViewModel
//
import SwiftUI

struct ForgotPasswordView: View {
    @StateObject private var viewModel = AuthViewModelWrapper()
    @Environment(\.dismiss) private var dismiss

    @FocusState private var emailFocused: Bool
    @State private var showErrorAlert = false

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Spacer()
                    .frame(height: 24)

                Text("Forgot it?")
                    .font(.title2)
                    .fontWeight(.bold)

                if !viewModel.isForgotPasswordEmailSent {
                    Text("Drop your email — we'll send a link to set a new password.")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)

                    InputFieldView(
                        label: String(localized: "label_email"),
                        text: Binding(
                            get: { viewModel.forgotPasswordEmailField },
                            set: { viewModel.onForgotPasswordEmailChanged($0) }
                        ),
                        icon: .email,
                        supportingText: viewModel.forgotPasswordEmailError ?? String(localized: "hint_email"),
                        supportingTextColor: viewModel.forgotPasswordEmailError != nil ? .red : .gray,
                        keyboardType: .emailAddress,
                        submitLabel: .go,
                        onSubmit: { viewModel.sendPasswordResetEmail() }
                    )
                    .focused($emailFocused)

                    Button(action: {
                        viewModel.sendPasswordResetEmail()
                    }) {
                        Text(viewModel.isForgotPasswordLoading ? "Sending…" : "Send reset link")
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundColor(Color(uiColor: .systemBackground))
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .background(Color.brandOrange)
                            .cornerRadius(8)
                    }
                    .disabled(viewModel.isForgotPasswordLoading)
                } else {
                    VStack(spacing: 12) {
                        Text("Reset link sent to")
                            .font(.caption)
                            .foregroundColor(.secondary)

                        Text(viewModel.forgotPasswordEmailField)
                            .font(.body)
                            .fontWeight(.medium)

                        Text("Open the link from your email to choose a new password.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(20)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(uiColor: .secondarySystemBackground))
                    )

                    Button("Back to sign in") {
                        viewModel.resetForgotPasswordState()
                        dismiss()
                    }
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundColor(.brandOrange)
                }

                Spacer()
            }
            .padding(16)
        }
        .onDisappear {
            viewModel.resetForgotPasswordState()
        }
        .onChange(of: viewModel.forgotPasswordErrorMessage) { _, newMessage in
            if newMessage != nil {
                showErrorAlert = true
            }
        }
        .alert("Authentication Error", isPresented: $showErrorAlert) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(viewModel.forgotPasswordErrorMessage ?? "An unexpected error occurred")
        }
    }
}

#Preview {
    ForgotPasswordView()
}
