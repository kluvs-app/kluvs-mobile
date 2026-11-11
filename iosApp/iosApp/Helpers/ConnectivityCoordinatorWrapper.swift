//
//  ConnectivityCoordinatorWrapper.swift
//  iosApp
//
//  iOS wrapper for shared ConnectivityCoordinator
//
import Swift
import Shared

@MainActor
class ConnectivityCoordinatorWrapper: ObservableObject {
    /// `true` while the network is unreachable, so the shell can show the offline banner.
    @Published var isOffline: Bool = false

    private let helper: ConnectivityCoordinatorHelper
    private var cancellable: Shared.Closeable?

    init() {
        self.helper = ConnectivityCoordinatorHelper()
        startObserving()
    }

    private func startObserving() {
        cancellable = helper.observeIsOffline { [weak self] isOffline in
            let offline = isOffline.boolValue
            Task { @MainActor [weak self] in
                self?.isOffline = offline
            }
        }
    }

    deinit {
        cancellable?.close()
    }
}
