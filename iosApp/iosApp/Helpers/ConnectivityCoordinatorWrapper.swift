//
//  ConnectivityCoordinatorWrapper.swift
//  iosApp
//
//  iOS wrapper for shared ConnectivityCoordinator
//
import Swift
import SwiftUI
import Shared
import DesignSystem

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
                // Animate only this change. A container-wide `.animation(value:)` would also
                // animate whatever else updates in the same moment (going offline makes screens
                // re-render from cache), which is costly and looks busy.
                withAnimation(OfflineBanner.animation) {
                    self?.isOffline = offline
                }
            }
        }
    }

    deinit {
        cancellable?.close()
    }
}
