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
    @Published var shelf: [Shared.ShelfItem] = []
    @Published var upNext: Shared.UpNextItem? = nil
    @Published var showLogoutConfirmation: Bool = false
    @Published var snackbarError: String? = nil
    @Published var isUploadingAvatar: Bool = false
    @Published var readingLog: Shared.ReadingLog? = nil
    @Published var isReadingLogLoading: Bool = false
    @Published var showReadingLog: Bool = false

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
                self.shelf = state.shelf
                self.upNext = state.upNext
                self.showLogoutConfirmation = state.showLogoutConfirmation
                self.snackbarError = state.snackbarError
                self.isUploadingAvatar = state.isUploadingAvatar
                self.readingLog = state.readingLog
                self.isReadingLogLoading = state.isReadingLogLoading
                self.showReadingLog = state.showReadingLog
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

    /// `percentComplete` is a plain Int (0-100) — the KMP helper converts it to Float internally.
    func onSaveProgress(sessionId: String, type: Shared.ProgressType, currentPage: Int32?, percentComplete: Int32?, markFinished: Bool) {
        helper.onSaveProgress(
            sessionId: sessionId,
            type: type,
            currentPage: currentPage.map { KotlinInt(int: $0) },
            percentComplete: percentComplete.map { KotlinInt(int: $0) },
            markFinished: markFinished
        )
    }

    func onReadingLogClicked() {
        helper.onReadingLogClicked()
    }

    func onReadingLogDismissed() {
        helper.onReadingLogDismissed()
    }

    deinit {
        cancellables.forEach { $0.close() }
    }
}
