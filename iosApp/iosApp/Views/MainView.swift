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
                // Books owns its own top bar/search UI (see BooksTopBar), so the shared
                // Material-style TopAppBar is skipped for that tab — this starts the trend
                // Clubs/Me are expected to follow.
                if selectedTab != 2 {
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

                MaterialBottomNavBar(selectedTab: $selectedTab)
            }
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
            .background(Color(UIColor.systemBackground))
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
