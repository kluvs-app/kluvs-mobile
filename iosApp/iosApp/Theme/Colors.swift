import SwiftUI
import UIKit

extension Color {
    // Brand colors matching Android theme
    // Note: Same colors for light/dark mode, matching Android's Material Design approach
    static let brandOrange = Color(hex: 0xD16D30)
    static let brandGreen = Color(hex: 0x48A480)
    static let brandBlue = Color(hex: 0x006781)
    static let brandGold = Color(hex: 0xEFBF04)

    // Auth screen colors (matching Android)
    static let discordBlue = Color(hex: 0x5865F2)
    static let googleGray = Color(hex: 0xF2F2F2)
    static let googleTextGray = Color(hex: 0x757575)
}

// Helper extensions
extension UIColor {
    convenience init(hex: UInt, alpha: CGFloat = 1.0) {
        self.init(
            red: CGFloat((hex >> 16) & 0xFF) / 255.0,
            green: CGFloat((hex >> 8) & 0xFF) / 255.0,
            blue: CGFloat(hex & 0xFF) / 255.0,
            alpha: alpha
        )
    }
}

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }
}
