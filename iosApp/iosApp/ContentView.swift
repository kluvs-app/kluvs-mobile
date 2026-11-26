import SwiftUI
import Shared
import DesignSystem

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
                    AuthView()
                case .authenticated(let userId):
                    MainView(
                        userId: userId,
                        initialClubId: autoJoinedClubId,
                        onNavigateToJoin: {
                            navigationPath.append(MainRoute.join(token: nil))
                        }
                    )
                case .updateRequired:
                    ForceUpdateView()
                }
            }
            .navigationDestination(for: MainRoute.self) { route in
                switch route {
                case .join(let token):
                    JoinView(
                        initialToken: token,
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
        .onOpenURL { url in
            handleIncomingURL(url)
        }
        .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
            // Universal Links normally surface through onOpenURL, but arrive as a browsing
            // user activity in some launch paths — handle both so neither is missed.
            if let url = activity.webpageURL {
                handleIncomingURL(url)
            }
        }
        .onChange(of: appCoordinator.navigationState) { _, newState in
            // Clear navigation stack when auth state changes
            if case .authenticated = newState {
                navigationPath = NavigationPath()
            } else if case .unauthenticated = newState {
                navigationPath = NavigationPath()
            } else if case .updateRequired = newState {
                // Hard block: drop anything pushed on top so only the update screen is reachable
                navigationPath = NavigationPath()
            }
            // Re-route afterwards: a deep link that landed before auth resolved would
            // otherwise be wiped by the reset above.
            routePendingInviteIfNeeded()
        }
        .onChange(of: pendingJoinCoordinator.incomingInviteToken) { _, _ in
            routePendingInviteIfNeeded()
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
        .snackbar(Binding(
            get: { autoJoinErrorMessage.map { SnackbarData(message: $0, variant: .danger) } },
            set: { if $0 == nil { autoJoinErrorMessage = nil } }
        ))
        .kluvsDismissKeyboardOnTap()
    }

    /// Invite links are checked first; anything else falls through to the OAuth handler, which
    /// is what received every URL before deep links existed. The two can't collide — invite
    /// links are https, OAuth callbacks use the `kluvs` scheme.
    private func handleIncomingURL(_ url: URL) {
        if pendingJoinCoordinator.onInviteLinkOpened(url) { return }
        OAuthCallbackHandler.shared.handleCallback(url)
    }

    /// Opens the Join screen for a deep-linked invite once auth has resolved. Works signed in
    /// or out — JoinView handles both, handing off to auth itself when the user taps Join.
    private func routePendingInviteIfNeeded() {
        guard let token = pendingJoinCoordinator.incomingInviteToken else { return }
        if case .initializing = appCoordinator.navigationState { return }
        // Leave the token pending: an outdated app can't open Join
        if case .updateRequired = appCoordinator.navigationState { return }

        navigationPath.append(MainRoute.join(token: token))
        pendingJoinCoordinator.onConsumeIncomingInviteToken()
    }
}

enum MainRoute: Hashable {
    /// `token` is non-nil when the screen was opened from an invite Universal Link, in which
    /// case JoinView pre-fills and previews it instead of showing an empty code field.
    case join(token: String?)
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
