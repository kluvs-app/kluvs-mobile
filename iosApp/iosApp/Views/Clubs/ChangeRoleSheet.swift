import SwiftUI
import Shared

private let assignableRoles: [Shared.Role] = [.admin, .member]

struct ChangeRoleSheet: View {
    let memberName: String
    let currentRole: Shared.Role
    let onSave: (Shared.Role) -> Void
    let onDismiss: () -> Void

    @State private var selectedRole: Shared.Role

    private let initialSelectedRole: Shared.Role

    init(
        memberName: String,
        currentRole: Shared.Role,
        onSave: @escaping (Shared.Role) -> Void,
        onDismiss: @escaping () -> Void
    ) {
        self.memberName = memberName
        self.currentRole = currentRole
        self.onSave = onSave
        self.onDismiss = onDismiss
        let initial = assignableRoles.contains(currentRole) ? currentRole : .member
        self.initialSelectedRole = initial
        _selectedRole = State(initialValue: initial)
    }

    private var hasChanges: Bool { selectedRole != initialSelectedRole }

    var body: some View {
        NavigationView {
            List {
                Section(memberName) {
                    ForEach(assignableRoles, id: \.ordinal) { role in
                        HStack(spacing: 12) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(role.name.capitalized)
                                    .font(.body)
                                Text(role == .admin
                                     ? "Can create and manage sessions and discussions"
                                     : "Regular club member")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            Spacer()
                            if selectedRole == role {
                                Image(systemName: "checkmark")
                                    .foregroundColor(.brandOrange)
                                    .fontWeight(.semibold)
                            }
                        }
                        .contentShape(Rectangle())
                        .onTapGesture { selectedRole = role }
                    }
                }
            }
            .navigationTitle("Change Role")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", action: onDismiss)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { onSave(selectedRole) }
                        .disabled(!hasChanges)
                }
            }
        }
        .presentationDetents([.medium])
    }
}

#Preview {
    ChangeRoleSheet(
        memberName: "Bob Smith",
        currentRole: .member,
        onSave: { _ in },
        onDismiss: {}
    )
}
