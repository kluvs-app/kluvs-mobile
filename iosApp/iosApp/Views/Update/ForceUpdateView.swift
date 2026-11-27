import SwiftUI
import DesignSystem

private let appStoreURL = URL(string: "https://apps.apple.com/app/id6757938950")!

// design-system tokens.json § component.illustration (size.mobile, gap) and
// § component.empty-state.body (max-width on mobile)
private let illustrationSize: CGFloat = 240
private let illustrationGap: CGFloat = 24
private let bodyMaxWidth: CGFloat = 240

/// Hard block shown when the installed app is older than the backend supports
/// (`NavigationState.UpdateRequired`). The only way forward is the App Store.
/// Mirrors Android's `ForceUpdateScreen` composable.
struct ForceUpdateView: View {
    @Environment(\.openURL) private var openURL

    var body: some View {
        // Scrolls so the only way forward stays reachable on short screens (landscape), while
        // staying vertically centered when everything fits.
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 0) {
                    // Used as-is on light and dark; must sit directly on the screen background
                    // (design-system/docs/illustrations.md)
                    Image("illustration_force_update")
                        .resizable()
                        .scaledToFit()
                        .frame(width: illustrationSize, height: illustrationSize)
                        .accessibilityHidden(true)
                        .padding(.bottom, illustrationGap)
                    VStack(spacing: 10) {
                        Text(String(localized: "force_update_heading"))
                            .kluvsStyle(KluvsTheme.typography.title.large, feature: true)
                            .foregroundColor(KluvsTheme.colors.labelVariant)
                            .multilineTextAlignment(.center)
                        Text(String(localized: "force_update_body"))
                            .kluvsStyle(KluvsTheme.typography.body.medium)
                            .foregroundColor(KluvsTheme.colors.contentMuted)
                            .multilineTextAlignment(.center)
                            .frame(maxWidth: bodyMaxWidth)
                        PrimaryButton(
                            text: String(localized: "force_update_button"),
                            action: { openURL(appStoreURL) },
                            fillWidth: true
                        )
                        .padding(.top, 6)
                    }
                }
                .padding(.horizontal, 40)
                .padding(.vertical, illustrationGap)
                .frame(width: geometry.size.width)
                .frame(minHeight: geometry.size.height)
            }
        }
        // The illustration must sit directly on the background role, never on a card or bar
        .background(KluvsTheme.colors.background.ignoresSafeArea())
    }
}

#Preview {
    ForceUpdateView()
}
