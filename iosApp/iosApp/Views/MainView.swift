import SwiftUI

struct MainView: View {
    let userId: String
    @State private var selectedTab = 0

    var body: some View {
        GeometryReader { geometry in
            VStack(spacing: 0) {
                // Clubs, Books, and Me each own their own top bar/heading UI (ClubsListView's
                // masthead, BooksTopBar, MeTopBar), so the shared Material-style TopAppBar
                // is unused for all tabs now.

                // Content area
                Group {
                    if selectedTab == 0 {
                        ClubsView(userId: userId)
                    } else if selectedTab == 1 {
                        MeView(userId: userId)
                    } else {
                        BooksView()
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.kluvsBackground)

                MaterialBottomNavBar(selectedTab: $selectedTab)
            }
            .background(Color.kluvsBackground)
            .ignoresSafeArea(edges: .bottom)
        }
    }
}

// Helper to access safe area insets
private struct SafeAreaInsetsKey: EnvironmentKey {
    static var defaultValue: EdgeInsets {
        EdgeInsets()
    }
}

extension EnvironmentValues {
    var safeAreaInsets: EdgeInsets {
        self[SafeAreaInsetsKey.self]
    }
}

#Preview {
    MainView(userId: "1")
}
