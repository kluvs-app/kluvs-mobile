import SwiftUI
import DesignSystem

private let appStoreURL = URL(string: "https://apps.apple.com/app/id6757938950")!

/// Hard block shown when the installed app is older than the backend supports
/// (`NavigationState.UpdateRequired`). The only way forward is the App Store.
/// Mirrors Android's `ForceUpdateScreen` composable.
struct ForceUpdateView: View {
    @Environment(\.openURL) private var openURL

    var body: some View {
        EmptyState(
            heading: String(localized: "force_update_heading"),
            body: String(localized: "force_update_body")
        ) {
            PrimaryButton(
                text: String(localized: "force_update_button"),
                action: { openURL(appStoreURL) }
            )
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

#Preview {
    ForceUpdateView()
}
