import SwiftUI

/// Low-emphasis outlined action button — mirrors web's `GhostButton` (design-system
/// "Secondary / Outlined" style). Used for supporting actions like "Join this Read" /
/// "Opt out" or "Update" progress.
struct GhostButton: View {
    let text: String
    let onClick: () -> Void
    var isEnabled: Bool = true

    var body: some View {
        Button(action: onClick) {
            Text(text)
                .font(.kluvsButtonSecondary)
                .foregroundColor(.primary)
                .padding(.vertical, 8)
                .padding(.horizontal, 12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .strokeBorder(Color(uiColor: .separator), lineWidth: 1)
                )
        }
        .disabled(!isEnabled)
        .opacity(isEnabled ? 1 : 0.5)
    }
}

#Preview {
    GhostButton(text: "Join this Read", onClick: {})
        .padding()
}
