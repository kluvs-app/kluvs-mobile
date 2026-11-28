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
    @Published var isRefreshingShelf: Bool = false
    @Published var shelfEntries: [Shared.ShelfEntry] = []
    @Published var likedBookIds: Set<String> = []
    @Published var query: String = ""
    @Published var isSearching: Bool = false
    @Published var searchError: String? = nil
    @Published var searchResults: [Shared.Book] = []
    @Published var isLoadingMore: Bool = false
    @Published var hasMoreSearchResults: Bool = false
    @Published var isMutationInProgress: Bool = false

    private let helper: BooksViewModelHelper
    /// What `.screenEffects(_:)` listens to: this screen's snackbars.
    var screenEffectSource: ScreenEffectSource { helper }
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
                    if !state.shelfEntries.isEmpty { return .content }
                    if state.isLoadingShelf { return .loading }
                    if let error = state.shelfError { return .error(error) }
                    return .empty
                }()
                self.isRefreshingShelf = state.isLoadingShelf
                self.shelfEntries = state.shelfEntries
                self.likedBookIds = Set(state.likedBookIds)
                self.query = state.query
                self.isSearching = state.isSearching
                self.searchError = state.searchError
                self.searchResults = state.searchResults
                self.isLoadingMore = state.isLoadingMore
                self.hasMoreSearchResults = state.hasMoreSearchResults
                self.isMutationInProgress = state.isMutationInProgress
            }
        }
        cancellables.append(stateCancellable)
    }

    func loadShelf(forceRefresh: Bool = false) { helper.loadShelf(forceRefresh: forceRefresh) }
    func onQueryChange(_ query: String) { helper.onQueryChange(query: query) }
    func search(_ query: String) { helper.search(query: query) }
    func loadMoreSearchResults() { helper.loadMoreSearchResults() }
    func onAssignShelf(bookId: String, shelf: Shared.ShelfStatus) {
        helper.onAssignShelf(bookId: bookId, shelf: shelf)
    }
    func onRemoveFromShelf(bookId: String) { helper.onRemoveFromShelf(bookId: bookId) }
    func onToggleLike(bookId: String) { helper.onToggleLike(bookId: bookId) }

    deinit {
        cancellables.forEach { $0.close() }
    }
}
