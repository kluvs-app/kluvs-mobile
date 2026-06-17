import SwiftUI

enum CustomIcon: String {
    case club = "ic_club"
    case clubs = "ic_clubs"
    case user = "ic_user"
    case book = "ic_book"
    case location = "ic_location"
    case settings = "ic_settings"
    case help = "ic_help"
    case checkmark = "ic_checkmark"
    case logout = "ic_logout"
    case email = "ic_email"
    case password = "ic_password"
    case edit = "ic_edit"
    case back = "ic_back"
    case info = "ic_info"
    case crown = "ic_crown"
    case shield = "ic_shield"
    
    case discord = "ic_discord"
    case google = "ic_google"
    case apple = "apple.logo" // SF Symbol

    var image: Image {
        // Use SF Symbol for apple, custom assets for others
        if self == .apple {
            return Image(systemName: self.rawValue)
                .renderingMode(.template)
        } else {
            return Image(self.rawValue)
                .renderingMode(.template)
        }
    }
}

extension Image {
    static func custom(_ icon: CustomIcon) -> Image {
        icon.image
    }
}
