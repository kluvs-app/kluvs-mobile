//
//  BookDetailsViewModelWrapper.swift
//  iosApp
//
import Swift
import Shared

@MainActor
class BookDetailsViewModelWrapper: ObservableObject {
    @Published var book: Shared.Book? = nil
    @Published var isLoadingEnrichment: Bool = true
    @Published var enrichment: Shared.BookEnrichment? = nil
    @Published var shelfStatus: Shared.ShelfStatus? = nil
    @Published var shelfSource: Shared.ShelfSource? = nil
    @Published var isLiked: Bool = false
    @Published var isMutationInProgress: Bool = false
    @Published var operationError: String? = nil

    private let helper: BookDetailsViewModelHelper
    private var cancellables: [Shared.Closeable] = []

    var isRegistered: Bool { helper.isRegistered }

    init() {
        self.helper = BookDetailsViewModelHelper()
        startObserving()
    }

    private func startObserving() {
        let stateCancellable = helper.observeState { [weak self] state in
            Task { @MainActor [weak self] in
                guard let self else { return }
                self.book = state.book
                self.isLoadingEnrichment = state.isLoadingEnrichment
                self.enrichment = state.enrichment
                self.shelfStatus = state.shelfStatus
                self.shelfSource = state.shelfSource
                self.isLiked = state.isLiked
                self.isMutationInProgress = state.isMutationInProgress
                self.operationError = state.operationError
            }
        }
        cancellables.append(stateCancellable)
    }

    func load(book: Shared.Book, shelfStatus: Shared.ShelfStatus?, shelfSource: Shared.ShelfSource?) {
        helper.load(book: book, shelfStatus: shelfStatus, shelfSource: shelfSource)
    }
    func onAssignShelf(_ shelf: Shared.ShelfStatus) { helper.onAssignShelf(shelf: shelf) }
    func onRemoveFromShelf() { helper.onRemoveFromShelf() }
    func onToggleLike() { helper.onToggleLike() }
    func onConsumeOperationError() { helper.onConsumeOperationError() }

    deinit {
        cancellables.forEach { $0.close() }
    }
}
