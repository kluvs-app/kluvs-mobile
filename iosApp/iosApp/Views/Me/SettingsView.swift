import SwiftUI
import SafariServices
import PhotosUI
import DesignSystem

struct SettingsView: View {
    let userId: String
    @StateObject private var viewModel = SettingsViewModelWrapper()
    @State private var showSaveSuccess = false
    @State private var showChangePasswordSuccess = false
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 0) {
            // DS `TopAppBar`, matching Android's `SettingsScreen` — not `.navigationTitle`/
            // `.toolbar`, which don't share Kluvs's branded chrome (eyebrow style, back icon).
            TopAppBar(header: String(localized: "settings_title"), onNavigateBack: { dismiss() })

            ScrollView {
                VStack(spacing: 0) {
                    EditProfileSection(
                        avatarUrl: viewModel.profile?.avatarUrl,
                        isUploadingAvatar: viewModel.isUploadingAvatar,
                        onAvatarPicked: { imageData in
                            viewModel.uploadAvatar(imageData: imageData)
                        },
                        onAvatarPickFailed: { reason in
                            viewModel.onAvatarPickFailed(reason: reason)
                        },
                        editedName: Binding(
                            get: { viewModel.editedName },
                            set: { viewModel.onNameChanged($0) }
                        ),
                        editedHandle: Binding(
                            get: { viewModel.editedHandle },
                            set: { viewModel.onHandleChanged($0) }
                        ),
                        hasChanges: viewModel.hasChanges,
                        isSaving: viewModel.isSaving,
                        saveError: viewModel.saveError,
                        handleError: viewModel.handleError,
                        onSaveProfile: { viewModel.onSaveProfile() }
                    )

                    AccountSection(
                        onChangePasswordClick: { viewModel.onChangePasswordSheetOpened() },
                        onDeleteAccountClick: { viewModel.onDeleteAccountClicked() }
                    )

                    LegalSection()

                    AboutSection()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }
        }
        .background(KluvsTheme.colors.background)
        .toolbar(.hidden, for: .navigationBar)
        .onAppear {
            viewModel.loadProfile(userId: userId)
        }
        .onChange(of: viewModel.saveSuccess) { success in
            if success {
                showSaveSuccess = true
                viewModel.onDismissSaveSuccess()
            }
        }
        .onChange(of: viewModel.changePasswordSuccess) { success in
            if success {
                // Close the sheet before the toast appears, matching Android's ordering —
                // otherwise the sheet would stay open while the toast is visible.
                viewModel.onChangePasswordSheetDismissed()
                showChangePasswordSuccess = true
                viewModel.onDismissChangePasswordSuccess()
            }
        }
        .overlay(alignment: .bottom) {
            if showSaveSuccess {
                SaveSuccessToast {
                    showSaveSuccess = false
                }
                .padding()
                .transition(.move(edge: .bottom).combined(with: .opacity))
            } else if showChangePasswordSuccess {
                SaveSuccessToast(message: String(localized: "password_updated")) {
                    showChangePasswordSuccess = false
                }
                .padding()
                .transition(.move(edge: .bottom).combined(with: .opacity))
            } else if let avatarError = viewModel.avatarError {
                SnackbarView(message: avatarError) {
                    viewModel.clearAvatarError()
                }
                .padding()
                .transition(.move(edge: .bottom).combined(with: .opacity))
            } else if let deleteAccountError = viewModel.deleteAccountError {
                SnackbarView(message: deleteAccountError) {
                    viewModel.clearDeleteAccountError()
                }
                .padding()
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showSaveSuccess)
        .animation(.easeInOut(duration: 0.3), value: showChangePasswordSuccess)
        .animation(.easeInOut(duration: 0.3), value: viewModel.avatarError)
        .animation(.easeInOut(duration: 0.3), value: viewModel.deleteAccountError)
        .kluvsConfirmationDialog(
            isPresented: $viewModel.isDeleteAccountDialogOpen,
            title: String(localized: "delete_account"),
            message: String(localized: "delete_account_confirmation_message"),
            confirmLabel: String(localized: "delete_account"),
            isDestructive: true,
            onDismiss: { viewModel.onDeleteAccountDialogDismissed() },
            onConfirm: { viewModel.onDeleteAccountConfirmed() }
        )
        .changePasswordSheet(
            isPresented: Binding(
                get: { viewModel.isChangePasswordSheetOpen },
                set: { if !$0 { viewModel.onChangePasswordSheetDismissed() } }
            ),
            viewModel: viewModel
        )
    }
}

// MARK: - Edit Profile Section

struct EditProfileSection: View {
    let avatarUrl: String?
    var isUploadingAvatar: Bool = false
    var onAvatarPicked: ((Data) -> Void)? = nil
    var onAvatarPickFailed: ((String?) -> Void)? = nil
    @Binding var editedName: String
    @Binding var editedHandle: String
    let hasChanges: Bool
    let isSaving: Bool
    let saveError: String?
    let handleError: String?
    let onSaveProfile: () -> Void

    @State private var selectedItem: PhotosPickerItem? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(String(localized: "edit_profile").uppercased())
                .kluvsStyle(KluvsTheme.typography.eyebrow)
                .foregroundColor(KluvsTheme.colors.contentMuted)

            Spacer()
                .frame(height: 4)

            ZStack(alignment: .bottomTrailing) {
                MemberAvatar(
                    avatarUrl: avatarUrl,
                    size: 64,
                    name: editedName,
                    isLoading: isUploadingAvatar,
                    onClick: nil
                )

                PhotosPicker(selection: $selectedItem, matching: .images) {
                    ZStack {
                        Circle()
                            .fill(Color.brandOrange.opacity(0.9))
                            .frame(width: 24, height: 24)

                        IconType.edit.image
                            .resizable()
                            .scaledToFit()
                            .frame(width: 12, height: 12)
                            .foregroundColor(KluvsTheme.colors.onAccent)
                    }
                }
                .onChange(of: selectedItem) { newItem in
                    guard let newItem else { return }
                    Task {
                        do {
                            guard let data = try await newItem.loadTransferable(type: Data.self) else {
                                onAvatarPickFailed?("loadTransferable returned nil data")
                                return
                            }
                            let compressedData = compressImage(data)
                            onAvatarPicked?(compressedData)
                        } catch {
                            onAvatarPickFailed?(error.localizedDescription)
                        }
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .center)
            .padding(.bottom, 4)

            InputField(label: String(localized: "label_name"), value: $editedName)

            InputField(label: String(localized: "label_handle"), value: $editedHandle, prefix: "@", error: handleError)

            if let saveError = saveError {
                Text(saveError)
                    .kluvsStyle(KluvsTheme.typography.body.medium)
                    .foregroundColor(KluvsTheme.colors.danger)
                    .padding(.top, 4)
            }

            PrimaryButton(
                text: isSaving ? String(localized: "button_save") + "…" : String(localized: "button_save"),
                action: onSaveProfile,
                enabled: hasChanges && !isSaving && handleError == nil
            )
            .frame(maxWidth: .infinity)
            .padding(.top, 4)
        }
        .padding(.vertical, 12)
    }
}

// MARK: - Account Section

struct AccountSection: View {
    let onChangePasswordClick: () -> Void
    var onDeleteAccountClick: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(String(localized: "account_title").uppercased())
                .kluvsStyle(KluvsTheme.typography.eyebrow)
                .foregroundColor(KluvsTheme.colors.contentMuted)
                .padding(.bottom, 8)

            AccountRow(label: String(localized: "change_password"), action: onChangePasswordClick)
            Divider().overlay(KluvsTheme.colors.divider)

            AccountRow(
                label: String(localized: "delete_account"),
                action: onDeleteAccountClick,
                textColor: KluvsTheme.colors.danger
            )
            Divider().overlay(KluvsTheme.colors.divider)
        }
        .padding(.vertical, 12)
    }
}

private struct AccountRow: View {
    let label: String
    let action: () -> Void
    var textColor: Color = KluvsTheme.colors.accent

    var body: some View {
        Button(action: action) {
            HStack {
                Text(label)
                    .kluvsStyle(KluvsTheme.typography.body.large)
                    .foregroundColor(textColor)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(KluvsTheme.colors.contentMuted)
            }
            .padding(.vertical, 12)
        }
    }
}

// MARK: - Legal Section

struct LegalSection: View {
    @State private var safariUrl: URL? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(String(localized: "legal_title").uppercased())
                .kluvsStyle(KluvsTheme.typography.eyebrow)
                .foregroundColor(KluvsTheme.colors.contentMuted)
                .padding(.bottom, 8)

            LegalRow(label: String(localized: "privacy_policy")) {
                safariUrl = URL(string: "https://kluvs.com/privacy")
            }
            Divider().overlay(KluvsTheme.colors.divider)

            LegalRow(label: String(localized: "terms_of_use")) {
                safariUrl = URL(string: "https://kluvs.com/terms")
            }
            Divider().overlay(KluvsTheme.colors.divider)

            LegalRow(label: String(localized: "data_deletion")) {
                safariUrl = URL(string: "https://kluvs.com/delete-account")
            }
            Divider().overlay(KluvsTheme.colors.divider)
        }
        .padding(.vertical, 12)
        .sheet(item: $safariUrl) { url in
            SafariView(url: url)
                .ignoresSafeArea()
        }
    }
}

private struct LegalRow: View {
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                Text(label)
                    .kluvsStyle(KluvsTheme.typography.body.large)
                    .foregroundColor(KluvsTheme.colors.accent)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(KluvsTheme.colors.contentMuted)
            }
            .padding(.vertical, 12)
        }
    }
}

// MARK: - Safari View

private struct SafariView: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        let config = SFSafariViewController.Configuration()
        config.entersReaderIfAvailable = false
        let vc = SFSafariViewController(url: url, configuration: config)
        vc.preferredControlTintColor = UIColor(named: "AccentColor")
        return vc
    }

    func updateUIViewController(_ uiViewController: SFSafariViewController, context: Context) {}
}

extension URL: @retroactive Identifiable {
    public var id: String { absoluteString }
}

// MARK: - About Section

struct AboutSection: View {
    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? ""
    }

    var body: some View {
        HStack {
            Spacer()
            Text(String(format: NSLocalizedString("app_version", comment: ""), appVersion))
                .kluvsStyle(KluvsTheme.typography.finePrint)
                .italic()
                .foregroundColor(KluvsTheme.colors.contentMuted)
        }
        .padding(.vertical, 12)
    }
}

// MARK: - Save Success Toast

private struct SaveSuccessToast: View {
    var message: String = String(localized: "save_success")
    let onDismiss: () -> Void

    var body: some View {
        HStack {
            Text(message)
                .font(.body)
                .foregroundColor(.white)
                .lineLimit(2)

            Spacer()

            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .foregroundColor(.white)
            }
        }
        .padding()
        .background(KluvsTheme.colors.success.opacity(0.9))
        .cornerRadius(8)
        .shadow(radius: 4)
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                onDismiss()
            }
        }
    }
}

#Preview {
    NavigationStack {
        SettingsView(userId: "1")
    }
}
