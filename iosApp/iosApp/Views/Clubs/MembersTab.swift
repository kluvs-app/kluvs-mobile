import SwiftUI
import Shared

struct MembersTab: View {
    let members: [Shared.MemberListItemInfo]
    var participants: [Shared.SessionParticipantInfo] = []
    var currentUserId: String = ""
    var userRole: Shared.Role? = nil
    var onChangeRole: (String) -> Void = { _ in }
    var onRemoveMember: (String) -> Void = { _ in }
    var onInviteMember: () -> Void = {}

    private var isAdminOrAbove: Bool { userRole == .owner || userRole == .admin }
    private var isOwner: Bool { userRole == .owner }
    private var readingByMemberId: [String: Bool] {
        Dictionary(uniqueKeysWithValues: participants.map { ($0.memberId, $0.isReading) })
    }

    var body: some View {
        ScrollView {
            if members.isEmpty {
                NoTabData(text: String(localized: "empty_no_members"))
            } else {
                VStack(alignment: .leading, spacing: 0) {
                    HStack {
                        Text("\(members.count) members")
                            .font(.ebGaramondItalic(size: 16))
                            .foregroundColor(.secondary)
                        Spacer()
                        if isAdminOrAbove {
                            GhostButton(text: "+ Invite", onClick: onInviteMember)
                        }
                    }
                    .padding(.bottom, 12)

                    ForEach(Array(members.enumerated()), id: \.element.memberId) { index, member in
                        let isSelf = member.userId == currentUserId
                        MemberListItem(
                            member: member,
                            isSelf: isSelf,
                            isReading: readingByMemberId[member.memberId],
                            showAdminActions: isAdminOrAbove && (!isSelf || isOwner),
                            showRemove: isOwner && !isSelf && member.role != .owner,
                            onChangeRole: { onChangeRole(member.memberId) },
                            onRemove: { onRemoveMember(member.memberId) }
                        )

                        if index < members.count - 1 {
                            Divider()
                        }
                    }

                    if members.count <= 1 && isAdminOrAbove {
                        VStack(spacing: 16) {
                            Text("Invite others to get the conversation going.")
                                .font(.ebGaramondMediumItalic(size: 20))
                                .foregroundColor(.secondary)
                                .multilineTextAlignment(.center)
                            Button(action: onInviteMember) {
                                Text("Invite Members")
                                    .font(.kluvsButtonPrimary)
                                    .padding(.horizontal, 20)
                                    .padding(.vertical, 10)
                                    .background(Color.brandOrange)
                                    .foregroundColor(.white)
                                    .cornerRadius(12)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.top, 20)
                    }
                }
                .padding(16)
            }
        }
    }
}

// MARK: - Member List Item

private struct MemberListItem: View {
    let member: Shared.MemberListItemInfo
    var isSelf: Bool = false
    var isReading: Bool? = nil
    var showAdminActions: Bool = false
    var showRemove: Bool = false
    var onChangeRole: () -> Void = {}
    var onRemove: () -> Void = {}

    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            Avatar(name: member.name, avatarUrl: member.avatarUrl, size: 40, memberId: member.memberId, isOwn: isSelf)

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 8) {
                    Text(member.name)
                        .font(.kluvsBodyLg)
                    if isSelf {
                        Text("YOU")
                            .font(.plexSansMedium(size: 11))
                            .foregroundColor(.brandOrange)
                    }
                }
                Text(member.handle)
                    .font(.kluvsBody)
                    .foregroundColor(.secondary)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 6) {
                HStack(spacing: 4) {
                    RoleEyebrow(role: member.role)
                    if showAdminActions || showRemove {
                        Menu {
                            if showAdminActions {
                                Button("Change Role", action: onChangeRole)
                            }
                            if showRemove {
                                Button("Remove", role: .destructive, action: onRemove)
                            }
                        } label: {
                            Image(systemName: "ellipsis")
                                .font(.system(size: 14))
                                .foregroundColor(.secondary)
                        }
                    }
                }
                if let isReading {
                    Text(isReading ? "Reading" : "Skipping")
                        .font(.plexSansMedium(size: 11))
                        .foregroundColor(isReading ? .brandOrange : .secondary)
                }
            }
        }
        .padding(.vertical, 12)
    }
}

#Preview {
    MembersTab(members: [])
}
