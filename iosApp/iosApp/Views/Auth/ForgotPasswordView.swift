//
//  ForgotPasswordView.swift
//  iosApp
//
//  Sends a password reset link. Mirrors Android's `ForgotPasswordScreen`: a quick detour off
//  Sign in, so it reuses the auth form's masthead and field but skips the footer.
//
import SwiftUI
import Shared
import DesignSystem

struct ForgotPasswordView: View {
    @StateObject private var viewModel = ForgotPasswordViewModelWrapper()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 0) {
            // Back only: the masthead below carries the screen's headline
            TopAppBar(header: "", onNavigateBack: { dismiss() })

            ScrollView {
                VStack(spacing: 16) {
                    AuthMasthead(
                        voicePhrase: viewModel.isEmailSent
                            ? String(localized: "headline_check_your_inbox")
                            : String(localized: "headline_forgot_it"),
                        subhead: viewModel.isEmailSent
                            ? nil
                            : String(localized: "hint_reset_password_subhead")
                    )

                    Spacer().frame(height: 12)

                    if viewModel.isEmailSent {
                        ResetLinkSentCard(email: viewModel.emailField)

                        TextButton(
                            text: String(localized: "button_back_to_sign_in"),
                            action: { dismiss() },
                            emphasized: true
                        )
                    } else {
                        InputField(
                            label: String(localized: "label_email"),
                            value: Binding(
                                get: { viewModel.emailField },
                                set: { viewModel.onEmailFieldChanged($0) }
                            ),
                            error: viewModel.emailError,
                            helperText: viewModel.emailError == nil ? String(localized: "hint_email") : nil,
                            keyboardType: .emailAddress,
                            submitLabel: .go,
                            onSubmit: { viewModel.onSendResetLink() }
                        )

                        PrimaryButton(
                            text: viewModel.isLoading
                                ? String(localized: "button_sending")
                                : String(localized: "button_send_reset_link"),
                            action: { viewModel.onSendResetLink() },
                            enabled: !viewModel.isLoading,
                            fillWidth: true
                        )
                    }
                }
                .padding(16)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(KluvsTheme.colors.background)
        .toolbar(.hidden, for: .navigationBar)
        .screenEffects(viewModel.screenEffectSource)
    }
}

private struct ResetLinkSentCard: View {
    let email: String

    var body: some View {
        VStack(alignment: .center, spacing: 8) {
            Text(String(localized: "label_reset_link_sent_to"))
                .kluvsStyle(KluvsTheme.typography.label)
                .foregroundColor(KluvsTheme.colors.contentMuted)

            Text(email)
                .kluvsStyle(KluvsTheme.typography.body.large)
                .fontWeight(.medium)
                .foregroundColor(KluvsTheme.colors.content)

            Text(String(localized: "text_reset_link_sent_body"))
                .kluvsStyle(KluvsTheme.typography.body.medium)
                .foregroundColor(KluvsTheme.colors.contentMuted)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(20)
        .background(KluvsTheme.colors.cardAlt)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

#Preview {
    NavigationStack {
        ForgotPasswordView()
    }
}
