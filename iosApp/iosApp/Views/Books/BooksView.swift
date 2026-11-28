//
//  BooksView.swift
//  iosApp
//
import SwiftUI
import Shared
import DesignSystem

private let shelfSections: [Shared.ShelfStatus] = [.currentlyReading, .read, .wantToRead, .notFinished]
private let searchDebounceNanoseconds: UInt64 = 400_000_000

/// Carries the tapped `Book` value itself through the `NavigationPath`, rather than pushing
/// just its id and having the destination closure read a separately-set `@State` — that split
/// is a real race: the id push and the state mutation are two separate updates, and
/// `NavigationStack` can resolve the destination from a snapshot that predates the second one,
/// most visibly on the very first push in a session (shows a blank destination with no top bar
/// until a second tap "catches up"). `Shared.Book` isn't `Hashable` on the Swift side, so this
/// wraps it and forwards equality/hash to its `id`.
private struct BookRoute: Hashable {
    let book: Shared.Book
    static func == (lhs: BookRoute, rhs: BookRoute) -> Bool { lhs.book.id == rhs.book.id }
    func hash(into hasher: inout Hasher) { hasher.combine(book.id) }
}

struct BooksView: View {
    @StateObject private var viewModel = BooksViewModelWrapper()
    @State private var isSearchActive = false
    @State private var searchTask: Task<Void, Never>?
    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            VStack(spacing: 0) {
                BooksTopBar(
                    isSearchActive: Binding(
                        get: { isSearchActive },
                        set: { newValue in
                            isSearchActive = newValue
                            // Closing search also clears the query — matches the old
                            // onBackClick behavior this binding replaces.
                            if !newValue { viewModel.onQueryChange("") }
                        }
                    ),
                    isSearching: viewModel.isSearching,
                    query: Binding(
                        get: { viewModel.query },
                        set: { viewModel.onQueryChange($0) }
                    )
                )

                if viewModel.isMutationInProgress {
                    ProgressView()
                        .progressViewStyle(LinearProgressViewStyle())
                        .tint(.brandOrange)
                }

                if isSearchActive {
                    SearchContent(viewModel: viewModel, onBookTap: { book in
                        path.append(BookRoute(book: book))
                    })
                } else {
                    ShelfContent(
                        viewModel: viewModel,
                        onBookTap: { book in
                            path.append(BookRoute(book: book))
                        },
                        onSearchBooks: { isSearchActive = true }
                    )
                }
            }
            .navigationDestination(for: BookRoute.self) { route in
                let book = route.book
                let shelfEntry = viewModel.shelfEntries.first { $0.book.id == book.id }
                BookDetailView(
                    book: book,
                    initialShelfStatus: shelfEntry?.shelf,
                    initialShelfSource: shelfEntry?.source,
                    onNavigateToBook: { nextBook in
                        path.append(BookRoute(book: nextBook))
                    }
                )
            }
            // BooksTopBar already provides this screen's own header (mirrors Android's
            // MainView comment: every tab owns its top bar, the system one is unused). Left
            // showing, its empty large-title bar still reserves space and appears to be what
            // was fighting our custom bar's own height animation when search opened the
            // keyboard — the visible symptom being the whole bar jumping up toward the status
            // bar.
            .toolbar(.hidden, for: .navigationBar)
        }
        .onAppear { viewModel.loadShelf() }
        .onChange(of: viewModel.query) { _, query in
            searchTask?.cancel()
            guard isSearchActive else { return }
            searchTask = Task {
                try? await Task.sleep(nanoseconds: searchDebounceNanoseconds)
                guard !Task.isCancelled else { return }
                viewModel.search(query)
            }
        }
        .screenEffects(viewModel.screenEffectSource)
    }
}

// MARK: - Shelf

private struct ShelfContent: View {
    @ObservedObject var viewModel: BooksViewModelWrapper
    let onBookTap: (Shared.Book) -> Void
    var onSearchBooks: () -> Void = {}

    var body: some View {
        switch viewModel.shelfScreenState {
        case .loading:
            BooksShelfSkeleton()
        case .error(let message):
            ErrorView(message: message, onRetry: { viewModel.loadShelf() })
        case .empty:
            EmptyState(
                heading: "Nothing shelved yet.",
                body: "Search for a book and add it to Want to Read, Read, or Not Finished."
            ) {
                SecondaryButton(text: "Search Books", action: onSearchBooks)
            }
        case .content:
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    ForEach(shelfSections, id: \.ordinal) { section in
                        let entries = viewModel.shelfEntries.filter { $0.shelf == section }
                        if !entries.isEmpty {
                            ShelfSectionView(section: section, entries: entries, onBookTap: onBookTap)
                        }
                    }
                }
                .padding(.bottom, 16)
            }
            .kluvsPullToRefresh(isRefreshing: viewModel.isRefreshingShelf) {
                viewModel.loadShelf(forceRefresh: true)
            }
        }
    }
}

private struct ShelfSectionView: View {
    let section: Shared.ShelfStatus
    let entries: [Shared.ShelfEntry]
    let onBookTap: (Shared.Book) -> Void

    @Environment(\.colorScheme) private var colorScheme

    private var eyebrowColor: Color { colorScheme == .dark ? Color(hex: 0xB0B0B0) : .foregroundLightSecondary }
    private var countColor: Color { colorScheme == .dark ? .foregroundWarmTertiary : .foregroundLightTertiary }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(alignment: .center, spacing: 8) {
                // Eyebrow — design-system component.eyebrow: IBM Plex Sans 11px/500, uppercase, 0.14em tracking
                Text(sectionLabel(section).uppercased())
                    .kluvsStyle(KluvsTheme.typography.eyebrow)
                    .foregroundColor(eyebrowColor)
                Text("\(entries.count)")
                    .font(.plexSans(size: 10))
                    .foregroundColor(countColor)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 4)

            ScrollView(.horizontal, showsIndicators: false) {
                // .top, not the default .center — otherwise a 2-line title pushes that card
                // taller, and HStack's default center alignment drops its cover out of line with
                // the shorter, 1-line-title cards next to it.
                HStack(alignment: .top, spacing: 12) {
                    ForEach(entries, id: \.book.id) { entry in
                        BookCard(
                            book: entry.book,
                            shelfSource: entry.source,
                            onTap: { onBookTap(entry.book) }
                        )
                    }
                }
                .padding(.horizontal, 16)
            }
        }
    }
}

private func sectionLabel(_ status: Shared.ShelfStatus) -> String {
    switch status {
    case .currentlyReading: return String(localized: "shelf_currently_reading")
    case .read: return String(localized: "shelf_read")
    case .wantToRead: return String(localized: "shelf_want_to_read")
    case .notFinished: return String(localized: "shelf_not_finished")
    default: return ""
    }
}

// MARK: - Search

private struct SearchContent: View {
    @ObservedObject var viewModel: BooksViewModelWrapper
    let onBookTap: (Shared.Book) -> Void

    private let gridColumns = [GridItem(.adaptive(minimum: 120), spacing: 12)]

    var body: some View {
        Group {
            if viewModel.query.trimmingCharacters(in: .whitespaces).isEmpty {
                SearchEmptyState(
                    heading: String(localized: "start_typing"),
                    bodyText: String(localized: "start_typing_hint")
                )
            } else if viewModel.isSearching && viewModel.searchResults.isEmpty {
                LoadingView()
            } else if let error = viewModel.searchError {
                ErrorView(message: error, onRetry: { viewModel.search(viewModel.query) })
            } else if viewModel.searchResults.isEmpty {
                SearchEmptyState(
                    heading: String(localized: "no_matches"),
                    bodyText: String(format: String(localized: "no_books_found_for_x"), viewModel.query)
                )
            } else {
                ScrollView {
                    LazyVGrid(columns: gridColumns, spacing: 12) {
                        ForEach(viewModel.searchResults, id: \.id) { book in
                            BookCard(
                                book: book,
                                onTap: { onBookTap(book) }
                            )
                            .onAppear {
                                if book.id == viewModel.searchResults.last?.id {
                                    viewModel.loadMoreSearchResults()
                                }
                            }
                        }
                    }
                    .padding(16)

                    if viewModel.isLoadingMore {
                        LoadingSpinner(size: 24)
                            .padding(.vertical, 16)
                    }
                }
                // The only other way to dismiss the keyboard is the search bar's back arrow,
                // which also exits search — this lets scrolling the results dismiss it instead.
                .scrollDismissesKeyboard(.immediately)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// Neither the "haven't searched yet" nor the "no matches" variant has a real destination to
// send the user to, so this carries no action.
private struct SearchEmptyState: View {
    let heading: String
    let bodyText: String

    var body: some View {
        EmptyState(heading: heading, body: bodyText)
    }
}

#Preview {
    BooksView()
}
