import SwiftUI
import DesignSystem

/// "Forgot it?" as a bottom sheet rather than a full screen — reset is a quick, single-field
/// detour off Sign in / Sign up, not a destination worth its own masthead/footer chrome. Reuses
/// the shared `AuthViewModelWrapper` rather than a dedicated sheet view model, matching the
/// stateless-sheet pattern used elsewhere. Mirrors Android's `AuthForgotSheet`.
struct AuthForgotSheetModifier: ViewModifier {
    @Binding var isPresented: Bool
    @ObservedObject var viewModel: AuthViewModelWrapper

    func body(content: Content) -> some View {
        content
            .kluvsBottomSheet(
                isPresented: $isPresented,
                header: viewModel.isForgotPasswordEmailSent
                    ? String(localized: "headline_check_your_inbox")
                    : String(localized: "headline_forgot_it"),
                content: {
                    if !viewModel.isForgotPasswordEmailSent {
                        AuthForgotSheetForm(viewModel: viewModel)
                    } else {
                        AuthForgotSheetSent(email: viewModel.forgotPasswordEmailField)
                    }
                },
                footer: {
                    if !viewModel.isForgotPasswordEmailSent {
                        BottomSheetFooter(
                            actionLabel: viewModel.isForgotPasswordLoading
                                ? String(localized: "button_sending")
                                : String(localized: "button_send_reset_link"),
                            onAction: { viewModel.sendPasswordResetEmail() },
                            onCancel: { isPresented = false },
                            actionEnabled: !viewModel.isForgotPasswordLoading
                        )
                    }
                }
            )
    }
}

extension View {
    func authForgotSheet(isPresented: Binding<Bool>, viewModel: AuthViewModelWrapper) -> some View {
        modifier(AuthForgotSheetModifier(isPresented: isPresented, viewModel: viewModel))
    }
}

private struct AuthForgotSheetForm: View {
    @ObservedObject var viewModel: AuthViewModelWrapper

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(String(localized: "hint_reset_password_subhead"))
                .kluvsStyle(KluvsTheme.typography.body.medium)
                .foregroundColor(KluvsTheme.colors.contentMuted)

            InputField(
                label: String(localized: "label_email"),
                value: Binding(
                    get: { viewModel.forgotPasswordEmailField },
                    set: { viewModel.onForgotPasswordEmailChanged($0) }
                ),
                error: viewModel.forgotPasswordEmailError,
                helperText: viewModel.forgotPasswordEmailError == nil ? String(localized: "hint_email") : nil,
                keyboardType: .emailAddress,
                submitLabel: .go,
                onSubmit: { viewModel.sendPasswordResetEmail() }
            )

            if let errorMessage = viewModel.forgotPasswordErrorMessage {
                ErrorBanner(message: errorMessage)
            }
        }
    }
}

private struct AuthForgotSheetSent: View {
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
        .padding(.vertical, 8)
    }
}

#Preview {
    Color.warmDarkBase
        .ignoresSafeArea()
        .authForgotSheet(isPresented: .constant(true), viewModel: AuthViewModelWrapper())
}
