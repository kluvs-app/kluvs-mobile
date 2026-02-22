import SwiftUI
import Shared

struct MembersTab: View {
    let members: [Shared.MemberListItemInfo]
    var currentUserId: String = ""
    var userRole: Shared.Role? = nil
    var onChangeRole: (String) -> Void = { _ in }
    var onRemoveMember: (String) -> Void = { _ in }

    @State private var showRoleInfo = false

    private var isAdminOrAbove: Bool { userRole == .owner || userRole == .admin }

    var body: some View {
        ScrollView {
            if members.isEmpty {
                NoTabData(text: String(localized: "empty_no_members"))
            } else {
                VStack(alignment: .leading, spacing: 0) {
                    // Header with member count and info button
                    HStack {
                        Text(String(format: NSLocalizedString("label_members_section", comment: ""), Int32(members.count)))
                            .font(.headline)

                        Spacer()

                        Button(action: { showRoleInfo = true }) {
                            Image("ic_info")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 20, height: 20)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(8)

                    ForEach(Array(members.enumerated()), id: \.offset) { index, member in
                        MemberListItem(
                            member: member,
                            showAdminActions: isAdminOrAbove && member.userId != currentUserId,
                            onChangeRole: { onChangeRole(member.memberId) },
                            onRemoveMember: { onRemoveMember(member.memberId) }
                        )

                        if index < members.count - 1 {
                            Divider()
                                .padding(.vertical, 4)
                        }
                    }
                }
                .padding()
            }
        }
        .sheet(isPresented: $showRoleInfo) {
            RoleInfoDialog(onDismiss: { showRoleInfo = false })
                .presentationDetents([.height(280)])
                .presentationDragIndicator(.visible)
        }
    }
}

// MARK: - Role Info Dialog
struct RoleInfoDialog: View {
    let onDismiss: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Member Roles")
                .font(.title3)
                .fontWeight(.semibold)

            VStack(alignment: .leading, spacing: 12) {
                RoleInfoItem(
                    role: .owner,
                    description: "Club owner with full control and permissions"
                )
                RoleInfoItem(
                    role: .admin,
                    description: "Club administrator with elevated permissions"
                )
                RoleInfoItem(
                    role: .member,
                    description: "Regular club member"
                )
            }

            HStack {
                Spacer()
                Button("Got it", action: onDismiss)
                    .fontWeight(.medium)
            }
        }
        .padding(20)
    }
}

struct RoleInfoItem: View {
    let role: Role
    let description: String

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            MemberAvatar(avatarUrl: nil, size: 40, role: role)

            VStack(alignment: .leading, spacing: 4) {
                Text(role.name.capitalized)
                    .font(.headline)

                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
        }
    }
}

// MARK: - Member List Item
struct MemberListItem: View {
    let member: Shared.MemberListItemInfo
    var showAdminActions: Bool = false
    var onChangeRole: () -> Void = {}
    var onRemoveMember: () -> Void = {}

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            MemberAvatar(
                avatarUrl: member.avatarUrl,
                size: 40,
                role: member.role
            )

            VStack(alignment: .leading, spacing: 2) {
                Text(member.name)
                    .font(.body)
                    .fontWeight(.medium)

                Text(member.handle)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }

            Spacer()

            if showAdminActions {
                Menu {
                    Button("Change Role") { onChangeRole() }
                    Button("Remove from Club", role: .destructive) { onRemoveMember() }
                } label: {
                    Image(systemName: "ellipsis")
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(.vertical, 12)
    }
}

#Preview {
    MembersTab(members: [])
}
