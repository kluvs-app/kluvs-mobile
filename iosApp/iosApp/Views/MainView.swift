import SwiftUI

struct MainView: View {
    let userId: String
    @State private var selectedTab = 0

    private let titles = [
        String(localized: "tab_clubs"),
        String(localized: "tab_me"),
        String(localized: "tab_books")
    ]

    var body: some View {
        GeometryReader { geometry in
            VStack(spacing: 0) {
                // Clubs and Books each own their own top bar/heading UI (ClubsListView's
                // masthead, BooksTopBar), so the shared Material-style TopAppBar is skipped
                // for those tabs — Me is expected to follow.
                if selectedTab == 1 {
                    MaterialTopBar(title: titles[selectedTab])
                }

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

// MARK: - Material-style TopAppBar
struct MaterialTopBar: View {
    let title: String
    @Environment(\.safeAreaInsets) private var safeAreaInsets

    var body: some View {
        VStack(spacing: 0) {
            // Title bar
            HStack {
                // Animated title with slot machine effect
                Text(title)
                    .font(.title2)
                    .fontWeight(.bold)
                    .transition(.asymmetric(
                        insertion: .move(edge: .bottom).combined(with: .opacity),
                        removal: .move(edge: .top).combined(with: .opacity)
                    ))
                    .id(title)

                Spacer()
            }
            .frame(height: 56)
            .padding(.horizontal, 16)
            .padding(.top, safeAreaInsets.top)
            .background(Color.kluvsBackground)
            .animation(.easeInOut(duration: 0.3), value: title)
        }
        .background(Color(UIColor.systemBackground))
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
