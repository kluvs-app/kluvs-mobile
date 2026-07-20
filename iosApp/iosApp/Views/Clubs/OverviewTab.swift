import SwiftUI
import Shared

/// Overview tab: active-session summary (book, participation, own progress) and an
/// "up next" discussion teaser. Mirrors web's mobile Overview tab / Android's `OverviewTab`.
/// The club masthead (name, meta row) lives above the tab picker in `ClubsView`, not here.
/// The full discussion timeline and end-session flow stay on the Discussions tab.
struct OverviewTab: View {
    let clubDetails: Shared.ClubDetails?
    let sessionDetails: Shared.ActiveSessionDetails?
    var ownProgress: Shared.OwnProgressInfo? = nil
    var userRole: Shared.Role? = nil
    var members: [Shared.MemberListItemInfo] = []
    var currentUserId: String = ""
    var onEditSession: () -> Void = {}
    var onEndSession: () -> Void = {}
    var onUpdateProgress: () -> Void = {}
    var onCreateSession: () -> Void = {}
    var onToggleParticipation: (Bool) -> Void = { _ in }

    private var isAdminOrAbove: Bool { userRole == .owner || userRole == .admin }
    private var currentMemberId: String? { members.first { $0.userId == currentUserId }?.memberId }

    var body: some View {
        ScrollView {
            if clubDetails == nil {
                NoTabData(text: String(localized: "empty_no_club_details"))
            } else if let session = sessionDetails {
                VStack(alignment: .leading, spacing: 20) {
                    sessionSummary(session: session)

                    if let next = session.discussions.first(where: { $0.isNext }) {
                        Divider()
                        upNextTeaser(discussion: next)
                        Divider()
                    }
                }
                .padding(16)
            } else {
                noActiveSessionState
                    .padding(16)
            }
        }
    }

    // MARK: - Session Summary

    private var readingParticipants: [Shared.SessionParticipantInfo] {
        (sessionDetails?.participants ?? []).filter { $0.isReading }
    }

    private var readingMembers: [AvatarStackMember] {
        let matched: [Shared.MemberListItemInfo] = readingParticipants.compactMap { participant in
            members.first { $0.memberId == participant.memberId }
        }
        return matched.map { AvatarStackMember(id: $0.memberId, name: $0.name, avatarUrl: $0.avatarUrl) }
    }

    private func isOwnReading(session: Shared.ActiveSessionDetails) -> Bool {
        guard let currentMemberId else { return false }
        return session.participants.contains { $0.memberId == currentMemberId && $0.isReading }
    }

    private var totalMemberCount: Int {
        Int(clubDetails?.memberCount ?? Int32(members.count))
    }

    @ViewBuilder
    private func sessionSummary(session: Shared.ActiveSessionDetails) -> some View {
        let ownReading = isOwnReading(session: session)
        let canToggle = currentMemberId != nil

        VStack(alignment: .leading, spacing: 12) {
            sessionHeader(session: session)
            participationRow(ownReading: ownReading, canToggle: canToggle)

            if ownReading {
                ownProgressRow(session: session)
            }
        }
    }

    private func sessionHeader(session: Shared.ActiveSessionDetails) -> some View {
        HStack(alignment: .top, spacing: 16) {
            BookCoverImage(imageUrl: session.book.imageUrl, width: 80)

            VStack(alignment: .leading, spacing: 2) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("ACTIVE SESSION")
                            .font(.kluvsEyebrow)
                            .foregroundColor(.brandOrange)

                        Text(session.book.title)
                            .font(.ebGaramondMediumItalic(size: 22))
                    }
                    Spacer()
                    if isAdminOrAbove {
                        sessionOverflowMenu
                    }
                }
                Text(session.book.author)
                    .font(.kluvsBody)
                    .foregroundColor(.secondary)
            }
        }
    }

    @ViewBuilder
    private func participationRow(ownReading: Bool, canToggle: Bool) -> some View {
        HStack {
            if !readingMembers.isEmpty {
                HStack(spacing: 8) {
                    AvatarStack(members: readingMembers, size: 24)
                    Text("\(readingParticipants.count) of \(totalMemberCount) reading")
                        .font(.kluvsHelperSm)
                        .foregroundColor(.secondary)
                }
            } else {
                Text("No participants yet")
                    .font(.kluvsHelperSm)
                    .foregroundColor(.secondary)
            }

            Spacer()

            if canToggle {
                GhostButton(
                    text: ownReading ? "Opt out" : "Join this Read",
                    onClick: { onToggleParticipation(!ownReading) }
                )
            }
        }
    }

    private var sessionOverflowMenu: some View {
        Menu {
            Button("Edit Session", action: onEditSession)
            Button("End Session", role: .destructive, action: onEndSession)
        } label: {
            Image(systemName: "ellipsis")
                .foregroundColor(.secondary)
        }
    }

    // MARK: - Own Progress

    @ViewBuilder
    private func ownProgressRow(session: Shared.ActiveSessionDetails) -> some View {
        let completed = session.discussions.filter { $0.isPast }.count
        let total = session.discussions.count

        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 12) {
                ProgressView(value: Double(ownProgress?.percent ?? 0), total: 100)
                    .tint(.brandOrange)
                GhostButton(text: ownProgress != nil ? "Update" : "Track Progress", onClick: onUpdateProgress)
            }
            HStack {
                Text("\(completed) of \(total) discussions")
                    .font(.ebGaramondItalic(size: 15))
                    .foregroundColor(.secondary)
                Spacer()
                if let label = ownProgress?.label {
                    Text(label)
                        .font(.kluvsHelperSm)
                        .foregroundColor(.brandOrange)
                }
            }
        }
    }

    // MARK: - No Active Session

    private var noActiveSessionState: some View {
        VStack(spacing: 8) {
            Text("NO SESSION YET")
                .font(.kluvsEyebrow)
                .foregroundColor(.secondary)
            Text("Start reading together.")
                .font(.ebGaramondMediumItalic(size: 22))
                .multilineTextAlignment(.center)
            if isAdminOrAbove {
                Button(action: onCreateSession) {
                    Text("Start Session")
                        .font(.kluvsButtonPrimary)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 10)
                        .background(Color.brandOrange)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                }
                .padding(.top, 8)
            }
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - Up Next

    private func upNextTeaser(discussion: Shared.DiscussionTimelineItemInfo) -> some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading, spacing: 4) {
                Text("UP NEXT")
                    .font(.kluvsEyebrow)
                    .foregroundColor(.brandOrange)
                Text(discussion.title)
                    .font(.kluvsCardHeading)
                Text(discussion.location)
                    .font(.kluvsBody)
                    .foregroundColor(.secondary)
            }
            Spacer()
            Text(discussion.date)
                .font(.kluvsEyebrow)
                .foregroundColor(.brandOrange)
        }
    }
}

#Preview {
    OverviewTab(clubDetails: nil, sessionDetails: nil)
}
