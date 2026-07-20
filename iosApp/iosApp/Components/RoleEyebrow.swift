import SwiftUI
import Shared

/// Uppercase role label with a colored dot for Owner/Admin — mirrors web's `RoleEyebrow` /
/// Android's `RoleEyebrow`. Supersedes the avatar-ring role indicator.
struct RoleEyebrow: View {
    let role: Shared.Role

    @Environment(\.colorScheme) private var colorScheme

    private var color: Color {
        switch role {
        case .owner: return .roleOwner
        // roleAdmin fails AA as text on dark surfaces — roleAdminOnDark is the lighter,
        // dark-surface-safe variant. On light surfaces roleAdmin has proper contrast.
        case .admin: return colorScheme == .dark ? .roleAdminOnDark : .roleAdmin
        default: return .secondary
        }
    }

    var body: some View {
        HStack(spacing: 6) {
            if role != .member {
                Circle()
                    .fill(color)
                    .frame(width: 8, height: 8)
            }
            Text(role.name.uppercased())
                .font(.plexSansMedium(size: 11))
                .foregroundColor(color)
        }
    }
}

#Preview {
    HStack(spacing: 16) {
        RoleEyebrow(role: .owner)
        RoleEyebrow(role: .admin)
        RoleEyebrow(role: .member)
    }
    .padding()
}
