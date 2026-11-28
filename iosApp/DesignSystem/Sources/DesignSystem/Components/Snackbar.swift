import SwiftUI

/// Which of the design-system-approved looks a snackbar renders in — see
/// design-system/docs/states.md § Snackbar. Mirrors Android's `SnackbarVariant`. Each variant
/// owns its own styling below (`backgroundColor`, `icon`) so adding a new case later means
/// adding one branch to each of those two, not threading a new boolean through `Snackbar`.
public enum SnackbarVariant: Hashable {
    case success
    case danger

    /// The solid fill color for this variant's snackbar surface.
    var backgroundColor: Color {
        switch self {
        case .success: KluvsTheme.colors.success
        case .danger: KluvsTheme.colors.danger
        }
    }

    /// The leading icon for this variant.
    var icon: IconType {
        switch self {
        case .success: .check
        case .danger: .error
        }
    }
}

/// The content of a single snackbar message — mirrors Android's `KluvsSnackbarVisuals`. No
/// action slot — nothing in this app currently needs one.
public struct SnackbarData: Equatable, Hashable {
    public let message: String
    public var variant: SnackbarVariant = .success

    public init(message: String, variant: SnackbarVariant = .success) {
        self.message = message
        self.variant = variant
    }
}

/// The design-system-branded snackbar surface — see design-system/docs/states.md § Snackbar.
/// Mirrors Android's `KluvsSnackbar`. Not used directly by screens; apply `.snackbar(_:)` instead,
/// which owns show/auto-dismiss timing the way `SnackbarHostState` does on Android.
///
/// Every variant uses a fully opaque fill with white (`onAccent`) text/icon — the same
/// "white on saturated brand/status surface" token PrimaryButton uses for copper — not a
/// translucent wash like `ErrorBanner`'s `dangerSubtle`. A subtle tint is legible sitting on a
/// known, fixed background; a snackbar floats over arbitrary content and needs to read clearly
/// regardless of what's behind it.
public struct Snackbar: View {
    public let data: SnackbarData

    public init(data: SnackbarData) {
        self.data = data
    }

    public var body: some View {
        HStack(spacing: 8) {
            Icon(type: data.variant.icon, contentDescription: nil, tint: KluvsTheme.colors.onAccent)
                .frame(width: 16, height: 16)

            Text(data.message)
                .kluvsStyle(KluvsTheme.typography.caption)
                .foregroundColor(KluvsTheme.colors.onAccent)
                .lineLimit(1)
                .truncationMode(.tail)
        }
        .padding(.horizontal, 12)
        .frame(height: 54)
        // Two-layer frame is deliberate, not redundant: `.frame(maxWidth: .infinity)` makes the
        // row greedy so it stretches to fill the available width (matching Android's
        // `fillMaxWidth()`); the second `.frame(maxWidth: 400)` then caps that at the design
        // spec's max width on wide/tablet layouts. `alignment: .leading` on both is required
        // too — `.frame`'s default alignment is `.center`, which would center the HStack's
        // (narrower) children inside the now-wide frame instead of leaving them packed at the
        // leading edge like Android's `fillMaxWidth()` row.
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(maxWidth: 400, alignment: .leading)
        .background(data.variant.backgroundColor)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

/// Shows `data` as a bottom-anchored snackbar, then clears the binding after `duration` — the
/// standalone-View equivalent of Android's `SnackbarHostState` for screens that aren't already
/// wrapped in a Scaffold-like container. A message that arrives while one is already showing
/// restarts the timer rather than queuing (matches design-system/docs/states.md § Snackbar):
/// `.task(id: data)` cancels the previous message's timer, so it can't cut the new one short.
private struct SnackbarModifier: ViewModifier {
    @Binding var data: SnackbarData?
    var duration: TimeInterval = 4.0

    func body(content: Content) -> some View {
        content.overlay(alignment: .bottom) {
            if let data {
                Snackbar(data: data)
                    .padding(.horizontal, 16)
                    .padding(.bottom, 24)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .id(data)
                    .task(id: data) {
                        try? await Task.sleep(for: .seconds(duration))
                        guard !Task.isCancelled else { return }
                        withAnimation { self.data = nil }
                    }
            }
        }
        .animation(.easeInOut(duration: 0.25), value: data)
    }
}

extension View {
    /// Shows `data` as a transient bottom snackbar, then clears the binding after `duration`.
    public func snackbar(_ data: Binding<SnackbarData?>, duration: TimeInterval = 4.0) -> some View {
        modifier(SnackbarModifier(data: data, duration: duration))
    }
}

#Preview("Success") {
    Snackbar(data: SnackbarData(message: "Club created"))
        .padding(16)
        .background(Color.warmDarkBase)
}

#Preview("Danger") {
    Snackbar(data: SnackbarData(message: "Couldn't save changes. Please try again.", variant: .danger))
        .padding(16)
        .background(Color.warmDarkBase)
}
