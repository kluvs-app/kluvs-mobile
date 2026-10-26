import SwiftUI
import DesignSystem

/// "Change Password" as a bottom sheet extending the shared `SettingsViewModelWrapper` rather
/// than a dedicated sheet view model — same stateless-sheet pattern used for auth's forgot-
/// password sheet. Mirrors Android's `ChangePasswordSheet`.
struct ChangePasswordSheetModifier: ViewModifier {
    @Binding var isPresented: Bool
    @ObservedObject var viewModel: SettingsViewModelWrapper

    func body(content: Content) -> some View {
        content
            .kluvsBottomSheet(
                isPresented: $isPresented,
                header: String(localized: "change_password"),
                onDismiss: { viewModel.onChangePasswordSheetDismissed() },
                content: {
                    ChangePasswordSheetForm(viewModel: viewModel)
                },
                footer: {
                    BottomSheetFooter(
                        actionLabel: String(localized: "update_password"),
                        onAction: { viewModel.onSubmitChangePassword() },
                        onCancel: { isPresented = false },
                        actionEnabled: !viewModel.isChangingPassword
                    )
                }
            )
    }
}

extension View {
    func changePasswordSheet(isPresented: Binding<Bool>, viewModel: SettingsViewModelWrapper) -> some View {
        modifier(ChangePasswordSheetModifier(isPresented: isPresented, viewModel: viewModel))
    }
}

private struct ChangePasswordSheetForm: View {
    @ObservedObject var viewModel: SettingsViewModelWrapper

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            PasswordField(
                label: String(localized: "new_password"),
                value: Binding(
                    get: { viewModel.newPasswordField },
                    set: { viewModel.onNewPasswordFieldChanged($0) }
                ),
                error: viewModel.newPasswordError,
                helperText: viewModel.newPasswordError == nil ? String(localized: "min_eight_characters") : nil,
                submitLabel: .next
            )

            PasswordField(
                label: String(localized: "confirm_new_password"),
                value: Binding(
                    get: { viewModel.confirmPasswordField },
                    set: { viewModel.onConfirmPasswordFieldChanged($0) }
                ),
                error: viewModel.confirmPasswordError,
                submitLabel: .go,
                onSubmit: { viewModel.onSubmitChangePassword() }
            )

            if let errorMessage = viewModel.changePasswordGeneralError?.toLocalizedMessage() {
                ErrorBanner(message: errorMessage)
            }
        }
    }
}

#Preview {
    Color.warmDarkBase
        .ignoresSafeArea()
        .changePasswordSheet(isPresented: .constant(true), viewModel: SettingsViewModelWrapper())
}
