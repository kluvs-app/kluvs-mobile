import SwiftUI
import Shared

/**
 * Displays a member's avatar image with fallback to placeholder.
 *
 * Shows a colored rim and role icon overlay for OWNER (gold crown) and ADMIN (blue shield).
 * - Crown sits at the top edge of the avatar
 * - Shield sits at the bottom edge of the avatar
 */
struct MemberAvatar: View {
    let avatarUrl: String?
    let size: CGFloat
    var role: Role? = nil
    var isLoading: Bool = false
    var onClick: (() -> Void)? = nil

    private var rimColor: Color? {
        guard let role = role else { return nil }
        switch role {
        case .owner: return Color(red: 0xEF/255.0, green: 0xBF/255.0, blue: 0x04/255.0) // gold
        case .admin: return Color(red: 0x00/255.0, green: 0x67/255.0, blue: 0x81/255.0) // blue
        default: return nil
        }
    }

    private var roleIcon: String? {
        guard let role = role else { return nil }
        switch role {
        case .owner: return "ic_crown"
        case .admin: return "ic_shield"
        default: return nil
        }
    }

    var body: some View {
        let iconSize = size * 0.28
        // Offset from ZStack center to align icon at top or bottom edge, then shift outward by 5pt
        let iconYOffset: CGFloat = {
            guard let role = role else { return 0 }
            let edgeFromCenter = size / 2 - iconSize / 2
            switch role {
            case .owner: return -(edgeFromCenter + 5)
            case .admin: return  (edgeFromCenter + 5)
            default: return 0
            }
        }()

        ZStack {
            // Avatar with optional rim
            avatarView

            // Role icon overlay (no background — bare tinted icon)
            if let iconName = roleIcon, let color = rimColor {
                Image(iconName)
                    .resizable()
                    .scaledToFit()
                    .frame(width: iconSize, height: iconSize)
                    .foregroundColor(color)
                    .offset(y: iconYOffset)
            }
        }
        .padding(4)
        .frame(width: size + 8, height: size + 8) // account for 4pt padding on each side
        .onTapGesture {
            onClick?()
        }
    }

    @ViewBuilder
    private var avatarView: some View {
        ZStack {
            if let urlString = avatarUrl, !urlString.isEmpty, let url = URL(string: urlString) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        placeholderView
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                            .frame(width: size, height: size)
                            .clipShape(Circle())
                    case .failure:
                        placeholderView
                    @unknown default:
                        placeholderView
                    }
                }
            } else {
                placeholderView
            }

            if isLoading {
                Circle()
                    .fill(Color.black.opacity(0.5))
                    .frame(width: size, height: size)

                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    .scaleEffect(size / 60)
            }
        }
        .frame(width: size, height: size)
        .overlay(
            Circle()
                .strokeBorder(rimColor ?? Color.clear, lineWidth: 2)
        )
    }

    private var placeholderView: some View {
        Image("img_fallback")
            .resizable()
            .scaledToFill()
            .frame(width: size, height: size)
            .clipShape(Circle())
    }
}

#Preview {
    VStack(spacing: 20) {
        HStack(spacing: 16) {
            MemberAvatar(avatarUrl: nil, size: 60, role: .member)
            MemberAvatar(avatarUrl: nil, size: 60, role: .admin)
            MemberAvatar(avatarUrl: nil, size: 60, role: .owner)
        }
        MemberAvatar(avatarUrl: nil, size: 60, isLoading: true)
    }
    .padding()
}
