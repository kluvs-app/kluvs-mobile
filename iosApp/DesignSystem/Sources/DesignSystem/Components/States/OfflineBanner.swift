import SwiftUI
import UIKit

/// Persistent banner telling the user the app is offline and showing saved data — see
/// design-system/docs/states.md § Offline Banner. Mirrors Android's `OfflineBanner`.
///
/// Place it at the top of the main tab shell, above the content, so it pushes the content down
/// rather than covering it. It has no icon, no action and no dismiss: it mirrors live state,
/// shown while `visible` is true. The host animates the change: apply
/// `.animation(OfflineBanner.animation, value: visible)` to the container that holds it, so the
/// content below slides with it. Announced to VoiceOver once, when it appears.
///
/// By default the band sits below the status bar. Pass `extendsUnderStatusBar: true` to carry the
/// wine fill up under it, which also needs the status-bar icons to be light on the light theme
/// (they are dark there, unreadable on wine) — SwiftUI has no way to set that without UIKit.
public struct OfflineBanner: View {
    /// 250ms on the design system's identity easing `cubic-bezier(0.2, 0.8, 0.2, 1)`.
    public static let animation = Animation.timingCurve(0.2, 0.8, 0.2, 1, duration: 0.25)

    private let visible: Bool
    private let text: String
    private let extendsUnderStatusBar: Bool

    public init(visible: Bool, text: String, extendsUnderStatusBar: Bool = false) {
        self.visible = visible
        self.text = text
        self.extendsUnderStatusBar = extendsUnderStatusBar
    }

    public var body: some View {
        if visible {
            band
                .transition(.move(edge: .top).combined(with: .opacity))
        }
    }

    private var band: some View {
        Text(text)
            .kluvsStyle(KluvsTheme.typography.label)
            .foregroundColor(KluvsTheme.colors.onOffline)
            .multilineTextAlignment(.center)
            .lineLimit(1)
            .truncationMode(.tail)
            .padding(.horizontal, 16)
            .frame(maxWidth: .infinity)
            .frame(height: 32)
            .background(KluvsTheme.colors.offline, ignoresSafeAreaEdges: extendsUnderStatusBar ? .top : [])
            .onAppear {
                UIAccessibility.post(notification: .announcement, argument: text)
            }
    }
}

#Preview {
    VStack(spacing: 0) {
        OfflineBanner(visible: true, text: "You're offline. Showing saved data.")
        Spacer()
    }
    .background(KluvsTheme.colors.background)
}
