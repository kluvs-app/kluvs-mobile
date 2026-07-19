import SwiftUI

struct MaterialBottomNavBar: View {
    @Binding var selectedTab: Int

    var body: some View {
        HStack(spacing: 0) {
            MaterialNavBarItem(
                icon: .club,
                label: String(localized: "tab_clubs"),
                isSelected: selectedTab == 0,
                action: { selectedTab = 0 }
            )

            MaterialNavBarItem(
                icon: .user,
                label: String(localized: "tab_me"),
                isSelected: selectedTab == 1,
                action: { selectedTab = 1 }
            )

            MaterialNavBarItem(
                icon: .book,
                label: String(localized: "tab_books"),
                isSelected: selectedTab == 2,
                action: { selectedTab = 2 }
            )
        }
        .frame(height: 80)
        .background(Color(UIColor.systemBackground))
        .overlay(
            Rectangle()
                .fill(Color.gray.opacity(0.2))
                .frame(height: 0.5),
            alignment: .top
        )
    }
}

struct MaterialNavBarItem: View {
    let icon: CustomIcon
    let label: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 4) {
                ZStack {
                    // Indicator background (like Android's indicatorColor)
                    if isSelected {
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color.brandOrange.opacity(0.2))
                            .frame(width: 64, height: 32)
                    }

                    // Icon
                    Image.custom(icon)
                        .font(.system(size: 24))
                        .foregroundColor(isSelected ? .brandOrange : .secondary)
                        .scaleEffect(isSelected ? 1.0 : 0.85)
                        .animation(.spring(response: 0.3, dampingFraction: 0.6), value: isSelected)
                }

                // Label
                Text(label)
                    .font(.caption)
                    .foregroundColor(isSelected ? .brandOrange : .secondary)
            }
            .frame(maxWidth: .infinity)
            .contentShape(Rectangle())
        }
        .buttonStyle(PlainButtonStyle())
    }
}

#Preview {
    VStack {
        Spacer()
        MaterialBottomNavBar(selectedTab: .constant(0))
    }
}
