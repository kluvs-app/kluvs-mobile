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
    @Published var snackbarError: String? = nil
    @Published var readingLog: Shared.ReadingLog? = nil
    @Published var isReadingLogLoading: Bool = false
    // Sheets and dialogs — owned by the shared ViewModel, see MeState.activeSheet
    @Published var activeSheet: Shared.MeSheet? = nil
    @Published var activeDialog: Shared.MeDialog? = nil

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
                self.snackbarError = state.snackbarError
                self.readingLog = state.readingLog
                self.isReadingLogLoading = state.isReadingLogLoading
                self.activeSheet = state.activeSheet
                self.activeDialog = state.activeDialog
            }
        }
        cancellables.append(stateCancellable)
    }

    func loadUserData(userId: String) {
        helper.loadUserData(userId: userId)
    }

    func refresh(forceRefresh: Bool = false) {
        helper.refresh(forceRefresh: forceRefresh)
    }

    func onSignOutDialogConfirmed() {
        helper.confirmLogout()
    }

    func clearSnackbarError() {
        helper.clearSnackbarError()
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

    // Sheets and dialogs
    func onOpenSheet(_ sheet: Shared.MeSheet) { helper.onOpenSheet(sheet: sheet) }
    func onDismissSheet() { helper.onDismissSheet() }
    func onOpenDialog(_ dialog: Shared.MeDialog) { helper.onOpenDialog(dialog: dialog) }
    func onDismissDialog() { helper.onDismissDialog() }

    deinit {
        cancellables.forEach { $0.close() }
    }
}
