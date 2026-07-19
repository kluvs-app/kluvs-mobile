//
//  AppCoordinatorWrapper.swift
//  iosApp
//
//  iOS wrapper for shared AppCoordinator
//
import Swift
import Shared

@MainActor
class AppCoordinatorWrapper: ObservableObject {
    @Published var navigationState: NavigationStateWrapper = .initializing

    private let helper: AppCoordinatorHelper
    private var cancellable: Shared.Closeable?

    init() {
        self.helper = AppCoordinatorHelper()
        startObserving()
    }

    private func startObserving() {
        cancellable = helper.observeNavigationState { [weak self] state in
            Task { @MainActor [weak self] in
                guard let self else { return }
                self.navigationState = NavigationStateWrapper.from(state)
            }
        }
    }

    deinit {
        cancellable?.close()
    }
}

// Swift-friendly enum wrapper for NavigationState
enum NavigationStateWrapper: Equatable {
    case initializing
    case unauthenticated
    case authenticated(userId: String)

    static func from(_ kotlinState: Shared.NavigationState) -> NavigationStateWrapper {
        if kotlinState is Shared.NavigationState.Initializing {
            return .initializing
        } else if kotlinState is Shared.NavigationState.Unauthenticated {
            return .unauthenticated
        } else if let authenticated = kotlinState as? Shared.NavigationState.Authenticated {
            return .authenticated(userId: authenticated.userId)
        }
        return .initializing
    }
}
