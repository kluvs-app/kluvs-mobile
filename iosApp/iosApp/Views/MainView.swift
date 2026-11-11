import SwiftUI
import DesignSystem

struct MainView: View {
    let userId: String
    var initialClubId: String? = nil
    var onNavigateToJoin: () -> Void = {}
    // Tells the user when the app is offline and showing saved data (see OfflineBanner).
    @StateObject private var connectivity = ConnectivityCoordinatorWrapper()
    // Tab order is Me(0) - Clubs(1) - Books(2), but landing on an auto-joined club (deep link /
    // post-signup join) still needs to open straight into the Clubs tab, not the default Me tab.
    @State private var selectedTab: Int

    init(userId: String, initialClubId: String? = nil, onNavigateToJoin: @escaping () -> Void = {}) {
        self.userId = userId
        self.initialClubId = initialClubId
        self.onNavigateToJoin = onNavigateToJoin
        self._selectedTab = State(initialValue: initialClubId != nil ? 1 : 0)
    }

    var body: some View {
        // This app shell (tab content + its own bottom nav) manages its own layout end to end,
        // so it should never resize/reposition when a text field somewhere inside focuses and
        // the keyboard appears. Without this, whichever ancestor above GeometryReader still
        // respects the keyboard safe area shrinks the space *it* proposes to GeometryReader,
        // and everything below — including tab bars positioned well away from the keyboard,
        // like Books' search bar at the very top — shifts to compensate, which is what was
        // making the search bar jump up toward (and partly under) the status bar the moment
        // the keyboard opened.
        GeometryReader { geometry in
            VStack(spacing: 0) {
                // Clubs, Books, and Me each own their own top bar/heading UI (ClubsListView's
                // masthead, BooksTopBar, MeTopBar), so the shared Material-style TopAppBar
                // is unused for all tabs now.

                OfflineBanner(
                    visible: connectivity.isOffline,
                    text: "You're offline. Showing saved data.",
                    // Wine fill carries up under the status bar (as on Android); the icons are
                    // forced light below so they stay readable on it in both themes.
                    extendsUnderStatusBar: true
                )

                // Content area
                Group {
                    if selectedTab == 0 {
                        MeView(userId: userId)
                    } else if selectedTab == 1 {
                        ClubsView(userId: userId, initialClubId: initialClubId, onNavigateToJoin: onNavigateToJoin)
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
            .onChange(of: initialClubId) { _, newValue in
                if newValue != nil { selectedTab = 1 }
            }
        }
        .ignoresSafeArea(.keyboard, edges: .all)
        .lightStatusBar(connectivity.isOffline)
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
