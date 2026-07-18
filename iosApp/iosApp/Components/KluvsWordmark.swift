import SwiftUI

/// The "KLUVS" wordmark — typographic, EB Garamond Bold, wide tracking.
/// Matches design-system/assets/kluvs-wordmark-{light,dark}.svg (48px / letter-spacing 8.6).
/// Color is theme-aware: cream on dark, dark chocolate on light.
struct KluvsWordmark: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        Text("KLUVS")
            .font(.ebGaramondBold(size: 24))
            .kerning(24 * 0.18)
            .foregroundColor(colorScheme == .dark ? .foregroundWarmPrimary : .foregroundLightLabelVariant)
    }
}

#Preview {
    KluvsWordmark()
        .padding()
        .background(Color.warmDarkBase)
}
