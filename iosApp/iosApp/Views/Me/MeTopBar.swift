import SwiftUI

/// Root-mode top bar for the Me tab — mirrors `BooksTopBar`'s self-owned layout
/// (title left, one trailing utility icon right). Reading Log is the screen's
/// single utility action, exposed via the trailing kebab menu.
struct MeTopBar: View {
    var onReadingLogClick: () -> Void = {}

    @Environment(\.safeAreaInsets) private var safeAreaInsets

    var body: some View {
        HStack {
            Text(String(localized: "tab_me"))
                .font(.title2)
                .fontWeight(.bold)
            Spacer()
            Menu {
                Button(String(localized: "reading_log")) {
                    onReadingLogClick()
                }
            } label: {
                Image(systemName: "ellipsis")
                    .foregroundColor(.secondary)
            }
        }
        .padding(.horizontal, 16)
        .frame(height: 56)
        .padding(.top, safeAreaInsets.top)
        .background(Color(UIColor.systemBackground))
    }
}

#Preview {
    MeTopBar()
}
