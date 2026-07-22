import SwiftUI
import Shared

/// Join-by-invite-token screen. Reachable today only via manual token entry — tapping a raw
/// invite URL does not yet open this screen (iOS Universal Links deep linking is a follow-up).
///
/// The preview shows only the club name — `Shared.ClubPreview` has no avatar/member-count yet
/// (also a follow-up, needs a backend spec change).
struct JoinView: View {
    let onNavigateToClub: (String) -> Void
    let onNeedsSignIn: (String) -> Void

    @StateObject private var viewModel = JoinViewModelWrapper()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Join a club")
                .font(.kluvsPageHeading)
                .foregroundColor(.primary)

            TextField("Invite code", text: Binding(
                get: { viewModel.tokenInput },
                set: { viewModel.onTokenChanged($0) }
            ))
            .textFieldStyle(.roundedBorder)
            .autocapitalization(.none)
            .disableAutocorrection(true)

            Button("Preview") {
                viewModel.previewInvite()
            }
            .disabled(viewModel.tokenInput.isEmpty || viewModel.isLoadingPreview)

            if viewModel.isLoadingPreview {
                ProgressView()
            }

            if let previewError = viewModel.previewError {
                Text(previewError)
                    .foregroundColor(.red)
            }

            if let preview = viewModel.preview {
                Text(preview.name)
                    .font(.kluvsSectionHeading)
                    .foregroundColor(.primary)

                if let joinError = viewModel.joinError {
                    Text(joinError)
                        .foregroundColor(.red)
                }

                Button(viewModel.isJoining ? "Joining..." : "Join") {
                    viewModel.onJoinClicked()
                }
                .disabled(viewModel.isJoining)
            }

            Spacer()
        }
        .padding(16)
        .navigationBarTitleDisplayMode(.inline)
        .onChange(of: viewModel.joinedClubId) { _, newValue in
            if let clubId = newValue {
                onNavigateToClub(clubId)
                viewModel.onConsumeJoinedClubId()
            }
        }
        .onChange(of: viewModel.needsSignIn) { _, needsSignIn in
            if needsSignIn {
                onNeedsSignIn(viewModel.tokenInput.trimmingCharacters(in: .whitespaces))
                viewModel.onConsumeNeedsSignIn()
            }
        }
    }
}

#Preview {
    JoinView(onNavigateToClub: { _ in }, onNeedsSignIn: { _ in })
}
