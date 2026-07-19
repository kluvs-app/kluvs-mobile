//
//  MeViewModelWrapper.swift
//  iosApp
//
//  Created by Ivan Garza Bermea on 12/4/25.
//
import Swift
import Shared


@MainActor
class MeViewModelWrapper: ObservableObject {
    @Published var isLoading: Bool = false
    @Published var error: String? = nil
    @Published var profile: Shared.UserProfile? = nil
    @Published var statistics: Shared.UserStatistics? = nil
    @Published var currentlyReading: [Shared.CurrentlyReadingBook] = []
    @Published var showLogoutConfirmation: Bool = false
    @Published var snackbarError: String? = nil
    @Published var isUploadingAvatar: Bool = false

    private let helper: MeViewModelHelper
    private var cancellables: [Shared.Closeable] = []

    init() {
        self.helper = MeViewModelHelper()
        startObserving()
    }

    private func startObserving() {
        let stateCancellable = helper.observeState { [weak self] state in
            Task { @MainActor [weak self] in
                guard let self else { return }
                self.isLoading = state.isLoading
                self.error = state.error
                self.profile = state.profile
                self.statistics = state.statistics
                self.currentlyReading = state.currentlyReading
                self.showLogoutConfirmation = state.showLogoutConfirmation
                self.snackbarError = state.snackbarError
                self.isUploadingAvatar = state.isUploadingAvatar
            }
        }
        cancellables.append(stateCancellable)
    }

    func loadUserData(userId: String) {
        helper.loadUserData(userId: userId)
    }

    func refresh() {
        helper.refresh()
    }

    func onSignOutClicked() {
        helper.showLogoutConfirmation()
    }

    func onSignOutDialogDismissed() {
        helper.hideLogoutConfirmation()
    }

    func onSignOutDialogConfirmed() {
        helper.confirmLogout()
    }

    func uploadAvatar(imageData: Data) {
        let byteArray = KotlinByteArray(size: Int32(imageData.count))
        imageData.withUnsafeBytes { (bytes: UnsafeRawBufferPointer) in
            if let baseAddress = bytes.baseAddress {
                for i in 0..<imageData.count {
                    byteArray.set(index: Int32(i), value: Int8(bitPattern: baseAddress.load(fromByteOffset: i, as: UInt8.self)))
                }
            }
        }
        helper.uploadAvatar(imageData: byteArray)
    }

    func clearAvatarError() {
        helper.clearAvatarError()
    }

    deinit {
        cancellables.forEach { $0.close() }
    }
}
