//
//  BooksViewModelWrapper.swift
//  iosApp
//
import Swift
import Shared

enum ShelfScreenState {
    case loading
    case error(String)
    case empty
    case content
}

@MainActor
class BooksViewModelWrapper: ObservableObject {
    @Published var shelfScreenState: ShelfScreenState = .loading
    @Published var shelfEntries: [Shared.ShelfEntry] = []
    @Published var likedBookIds: Set<String> = []
    @Published var query: String = ""
    @Published var isSearching: Bool = false
    @Published var searchError: String? = nil
    @Published var searchResults: [Shared.Book] = []
    @Published var isMutationInProgress: Bool = false
    @Published var operationError: String? = nil

    private let helper: BooksViewModelHelper
    private var cancellables: [Shared.Closeable] = []

    init() {
        self.helper = BooksViewModelHelper()
        startObserving()
    }

    private func startObserving() {
        let stateCancellable = helper.observeState { [weak self] state in
            Task { @MainActor [weak self] in
                guard let self else { return }
                self.shelfScreenState = {
                    if state.isLoadingShelf { return .loading }
                    if !state.shelfEntries.isEmpty { return .content }
                    if let error = state.shelfError { return .error(error) }
                    return .empty
                }()
                self.shelfEntries = state.shelfEntries
                self.likedBookIds = Set(state.likedBookIds)
                self.query = state.query
                self.isSearching = state.isSearching
                self.searchError = state.searchError
                self.searchResults = state.searchResults
                self.isMutationInProgress = state.isMutationInProgress
                self.operationError = state.operationError
            }
        }
        cancellables.append(stateCancellable)
    }

    func loadShelf(forceRefresh: Bool = false) { helper.loadShelf(forceRefresh: forceRefresh) }
    func onQueryChange(_ query: String) { helper.onQueryChange(query: query) }
    func search(_ query: String) { helper.search(query: query) }
    func onAssignShelf(bookId: String, shelf: Shared.ShelfStatus) {
        helper.onAssignShelf(bookId: bookId, shelf: shelf)
    }
    func onRemoveFromShelf(bookId: String) { helper.onRemoveFromShelf(bookId: bookId) }
    func onToggleLike(bookId: String) { helper.onToggleLike(bookId: bookId) }
    func onConsumeOperationError() { helper.onConsumeOperationError() }

    deinit {
        cancellables.forEach { $0.close() }
    }
}
