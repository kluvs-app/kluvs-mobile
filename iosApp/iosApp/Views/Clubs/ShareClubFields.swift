import SwiftUI
import Shared
import DesignSystem

/// Share Club sheet body — presented via `.kluvsBottomSheet` at the call site (`ClubsView`), not
/// as its own `View` wrapping a `.sheet`. Mirrors web's `ShareClubModal`: "Who can join?" segmented
/// control (`ToggleControl`, matching web's Private/Invite Link pill row exactly, rather than
/// keeping the previous single on/off Toggle), invite URL row with Copy, and a "Copied!" state.
///
/// `onRotate`/"Rotate link" is a mobile-only addition beyond web's modal (web has no regenerate
/// action) — kept as-is since it's pre-existing, already-wired functionality, not something this
/// pass should remove just because web doesn't have it.
///
/// Toggling the join policy and rotating the invite token are owner-only (`canManage`); an admin
/// sees the current link read-only with just the copy/share actions, same as before.
struct ShareClubFields: View {
    let joinPolicy: Shared.JoinPolicy?
    let inviteToken: String?
    let canManage: Bool
    let isOperationInProgress: Bool
    let onTogglePolicy: (Shared.JoinPolicy) -> Void
    let onRotate: () -> Void
    let onCopy: (String) -> Void
    let onShare: (String) -> Void

    @State private var copied = false

    private var isInviteActive: Bool {
        joinPolicy == Shared.JoinPolicy.inviteLink && inviteToken != nil
    }

    private var inviteUrl: String? {
        inviteToken.map { InviteLink.shared.build(token: $0) }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            VStack(alignment: .leading, spacing: 8) {
                Text("WHO CAN JOIN?")
                    .kluvsStyle(KluvsTheme.typography.eyebrow)
                    .foregroundColor(KluvsTheme.colors.contentMuted)

                ToggleControl(
                    options: [Shared.JoinPolicy.private_, Shared.JoinPolicy.inviteLink],
                    selected: joinPolicy ?? Shared.JoinPolicy.private_,
                    onSelect: onTogglePolicy,
                    label: { $0 == Shared.JoinPolicy.inviteLink ? "Invite Link" : "Private" }
                )
                .disabled(!canManage || isOperationInProgress)
                .opacity(canManage && !isOperationInProgress ? 1 : 0.6)
            }

            if isOperationInProgress {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
            } else if isInviteActive, let inviteUrl {
                VStack(alignment: .leading, spacing: 12) {
                    HStack(spacing: 12) {
                        Text(inviteUrl)
                            .kluvsStyle(KluvsTheme.typography.mono)
                            .foregroundColor(KluvsTheme.colors.contentMuted)
                            .lineLimit(1)
                            .truncationMode(.middle)
                        Spacer()
                        Button(copied ? "Copied!" : "Copy") {
                            onCopy(inviteUrl)
                            copied = true
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2) { copied = false }
                        }
                        .foregroundColor(copied ? KluvsTheme.colors.success : KluvsTheme.colors.accent)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(KluvsTheme.colors.cardAlt)
                    .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(KluvsTheme.colors.divider, lineWidth: 1))
                    .clipShape(RoundedRectangle(cornerRadius: 8))

                    HStack {
                        SecondaryButton(text: "Share", action: { onShare(inviteUrl) })
                        Spacer()
                        if canManage {
                            SecondaryButton(text: "Rotate link", action: onRotate, enabled: !isOperationInProgress)
                        }
                    }
                }
            } else if !canManage {
                Text("Invite link sharing is currently off for this club.")
                    .kluvsStyle(KluvsTheme.typography.body.medium)
                    .foregroundColor(KluvsTheme.colors.contentMuted)
            }
        }
    }
}

#Preview {
    ShareClubFields(
        joinPolicy: Shared.JoinPolicy.inviteLink,
        inviteToken: "abc123",
        canManage: true,
        isOperationInProgress: false,
        onTogglePolicy: { _ in },
        onRotate: {},
        onCopy: { _ in },
        onShare: { _ in }
    )
    .padding()
    .background(KluvsTheme.colors.bar)
}
