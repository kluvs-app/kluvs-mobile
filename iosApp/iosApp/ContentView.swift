import SwiftUI
import Shared

struct ContentView: View {
    @StateObject private var appCoordinator = AppCoordinatorWrapper()
    @StateObject private var pendingJoinCoordinator = PendingJoinCoordinatorWrapper()
    @State private var navigationPath = NavigationPath()
    // Only set on a successful auto-join after sign-in; consumed by MainView to open
    // straight into that club (see PendingJoinCoordinator).
    @State private var autoJoinedClubId: String? = nil
    @State private var autoJoinErrorMessage: String? = nil

    var body: some View {
        NavigationStack(path: $navigationPath) {
            Group {
                switch appCoordinator.navigationState {
                case .initializing:
                    LoadingView()
                case .unauthenticated:
                    AuthView(
                        onNavigateToForgotPassword: {
                            navigationPath.append(AuthRoute.forgotPassword)
                        }
                    )
                case .authenticated(let userId):
                    MainView(
                        userId: userId,
                        initialClubId: autoJoinedClubId,
                        onNavigateToJoin: {
                            navigationPath.append(MainRoute.join)
                        }
                    )
                }
            }
            .navigationDestination(for: AuthRoute.self) { route in
                switch route {
                case .forgotPassword:
                    ForgotPasswordView()
                }
            }
            .navigationDestination(for: MainRoute.self) { route in
                switch route {
                case .join:
                    JoinView(
                        onNavigateToClub: { clubId in
                            autoJoinedClubId = clubId
                            navigationPath.removeLast(navigationPath.count)
                        },
                        onNeedsSignIn: { token in
                            pendingJoinCoordinator.setPendingToken(token)
                            navigationPath.removeLast(navigationPath.count)
                        }
                    )
                }
            }
        }
        .onChange(of: appCoordinator.navigationState) { _, newState in
            // Clear navigation stack when auth state changes
            if case .authenticated = newState {
                navigationPath = NavigationPath()
            } else if case .unauthenticated = newState {
                navigationPath = NavigationPath()
            }
        }
        .onChange(of: pendingJoinCoordinator.autoJoinResult) { _, result in
            switch result {
            case .success(let clubId):
                autoJoinedClubId = clubId
            case .failure(let message):
                autoJoinErrorMessage = message ?? "Failed to join club"
            case nil:
                break
            }
            pendingJoinCoordinator.onConsumeAutoJoinResult()
        }
        .toast(message: $autoJoinErrorMessage)
    }
}

enum AuthRoute: Hashable {
    case forgotPassword
}

enum MainRoute: Hashable {
    case join
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
