import SwiftUI
import Shared

/// Web app domain used to build shareable invite links — mobile does not yet handle this URL via deep link.
private let webAppDomain = "https://kluvs.com"

/// Bottom sheet for managing a club's invite link (mirrors web's `ShareClubModal` and
/// Android's `ShareClubBottomSheet`).
///
/// Toggling the join policy and rotating the invite token are owner-only (`canManage`);
/// an admin sees the current link read-only with just the copy/share actions.
struct ShareClubSheet: View {
    let joinPolicy: Shared.JoinPolicy?
    let inviteToken: String?
    let canManage: Bool
    let isOperationInProgress: Bool
    let onTogglePolicy: (Shared.JoinPolicy) -> Void
    let onRotate: () -> Void
    let onDismiss: () -> Void

    @State private var showShareSheet = false

    private var isInviteActive: Bool {
        joinPolicy == Shared.JoinPolicy.inviteLink && inviteToken != nil
    }

    private var inviteUrl: String? {
        inviteToken.map { "\(webAppDomain)/join/\($0)" }
    }

    var body: some View {
        NavigationView {
            Form {
                Section {
                    Toggle(
                        "Allow join via link",
                        isOn: Binding(
                            get: { joinPolicy == Shared.JoinPolicy.inviteLink },
                            set: { checked in
                                onTogglePolicy(checked ? Shared.JoinPolicy.inviteLink : Shared.JoinPolicy.private_)
                            }
                        )
                    )
                    .disabled(!canManage || isOperationInProgress)
                }

                if isOperationInProgress {
                    Section {
                        ProgressView()
                    }
                }

                if isInviteActive, let inviteUrl {
                    Section {
                        Text(inviteUrl)
                            .foregroundColor(.secondary)

                        HStack {
                            Button("Copy") {
                                UIPasteboard.general.string = inviteUrl
                            }
                            Spacer()
                            Button("Share") {
                                showShareSheet = true
                            }
                        }

                        if canManage {
                            Button("Rotate link", action: onRotate)
                                .disabled(isOperationInProgress)
                        }
                    }
                } else if !canManage {
                    Section {
                        Text("Invite link sharing is currently off for this club.")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .navigationTitle("Share Club")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Done", action: onDismiss)
                }
            }
            .sheet(isPresented: $showShareSheet) {
                if let inviteUrl {
                    ActivityView(activityItems: [inviteUrl])
                }
            }
        }
        .presentationDetents([.medium])
    }
}

/// `UIActivityViewController` wrapper — SwiftUI has no built-in native share sheet
/// trigger prior to `ShareLink` (iOS 16+); this works across the app's deployment target.
private struct ActivityView: UIViewControllerRepresentable {
    let activityItems: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

#Preview {
    ShareClubSheet(
        joinPolicy: Shared.JoinPolicy.inviteLink,
        inviteToken: "abc123",
        canManage: true,
        isOperationInProgress: false,
        onTogglePolicy: { _ in },
        onRotate: {},
        onDismiss: {}
    )
}
