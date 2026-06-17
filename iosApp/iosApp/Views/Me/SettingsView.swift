import SwiftUI
import SafariServices

struct SettingsView: View {
    let userId: String
    @StateObject private var viewModel = SettingsViewModelWrapper()
    @State private var showSaveSuccess = false
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                EditProfileSection(
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
                    onSaveProfile: { viewModel.onSaveProfile() }
                )

                Divider()
                    .padding(.top, 12)

                LegalSection()

                AboutSection()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        }
        .navigationTitle(String(localized: "settings_title"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { dismiss() }) {
                    Image.custom(.back)
                }
            }
        }
        .onAppear {
            viewModel.loadProfile(userId: userId)
        }
        .onChange(of: viewModel.saveSuccess) { success in
            if success {
                showSaveSuccess = true
                viewModel.onDismissSaveSuccess()
            }
        }
        .overlay(alignment: .bottom) {
            if showSaveSuccess {
                SaveSuccessToast {
                    showSaveSuccess = false
                }
                .padding()
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showSaveSuccess)
    }
}

// MARK: - Edit Profile Section

struct EditProfileSection: View {
    @Binding var editedName: String
    @Binding var editedHandle: String
    let hasChanges: Bool
    let isSaving: Bool
    let saveError: String?
    let onSaveProfile: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(String(localized: "edit_profile"))
                .font(.headline)

            Spacer()
                .frame(height: 4)

            VStack(alignment: .leading, spacing: 4) {
                Text(String(localized: "label_name"))
                    .font(.caption)
                    .foregroundColor(.secondary)

                TextField(String(localized: "label_name"), text: $editedName)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color(UIColor.systemGray4), lineWidth: 1)
                    )
            }

            HandleInputField(handle: $editedHandle)

            if let saveError = saveError {
                Text(saveError)
                    .font(.caption)
                    .foregroundColor(.red)
                    .padding(.top, 4)
            }

            Button(action: onSaveProfile) {
                Group {
                    if isSaving {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text(String(localized: "button_save"))
                            .font(.body)
                            .fontWeight(.medium)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 44)
            }
            .buttonStyle(.borderedProminent)
            .disabled(!hasChanges || isSaving)
            .padding(.top, 4)
        }
        .padding(.vertical, 12)
    }
}

// MARK: - Handle Input Field

private struct HandleInputField: View {
    @Binding var handle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(String(localized: "label_handle"))
                .font(.caption)
                .foregroundColor(.secondary)

            HStack(spacing: 4) {
                Text("@")
                    .foregroundColor(.secondary)
                    .padding(.leading, 12)

                TextField(String(localized: "label_handle"), text: $handle)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                    .padding(.trailing, 12)
                    .padding(.vertical, 12)
            }
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color(UIColor.systemGray4), lineWidth: 1)
            )
        }
    }
}

// MARK: - Legal Section

struct LegalSection: View {
    @State private var safariUrl: URL? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(String(localized: "legal_title"))
                .font(.headline)
                .padding(.bottom, 8)

            LegalRow(label: String(localized: "privacy_policy")) {
                safariUrl = URL(string: "https://kluvs.com/privacy")
            }
            Divider()

            LegalRow(label: String(localized: "terms_of_use")) {
                safariUrl = URL(string: "https://kluvs.com/terms")
            }
            Divider()
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
                    .font(.body)
                    .foregroundColor(.accentColor)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
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
                .font(.caption)
                .italic()
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 12)
    }
}

// MARK: - Save Success Toast

private struct SaveSuccessToast: View {
    let onDismiss: () -> Void

    var body: some View {
        HStack {
            Text(String(localized: "save_success"))
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
        .background(Color.green.opacity(0.9))
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
