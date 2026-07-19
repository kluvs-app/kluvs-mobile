//
//  BooksView.swift
//  iosApp
//
import SwiftUI
import Shared

private let shelfSections: [Shared.ShelfStatus] = [.currentlyReading, .read, .wantToRead, .notFinished]
private let searchDebounceNanoseconds: UInt64 = 400_000_000

struct BooksView: View {
    @StateObject private var viewModel = BooksViewModelWrapper()
    @State private var isSearchActive = false
    @State private var searchTask: Task<Void, Never>?

    var body: some View {
        VStack(spacing: 0) {
            BooksTopBar(
                isSearchActive: isSearchActive,
                isSearching: viewModel.isSearching,
                query: Binding(
                    get: { viewModel.query },
                    set: { viewModel.onQueryChange($0) }
                ),
                onSearchClick: { isSearchActive = true },
                onBackClick: {
                    isSearchActive = false
                    viewModel.onQueryChange("")
                }
            )

            if viewModel.isMutationInProgress {
                ProgressView()
                    .progressViewStyle(LinearProgressViewStyle())
                    .tint(.brandOrange)
            }

            if isSearchActive {
                SearchContent(viewModel: viewModel)
            } else {
                ShelfContent(viewModel: viewModel)
            }
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
        .alert("Result", isPresented: Binding(
            get: { viewModel.operationError != nil },
            set: { if !$0 { viewModel.onConsumeOperationError() } }
        )) {
            Button("OK") { viewModel.onConsumeOperationError() }
        } message: {
            if let error = viewModel.operationError {
                Text(error)
            }
        }
    }
}

// MARK: - Shelf

private struct ShelfContent: View {
    @ObservedObject var viewModel: BooksViewModelWrapper

    var body: some View {
        switch viewModel.shelfScreenState {
        case .loading:
            LoadingView()
        case .error(let message):
            ErrorView(message: message, onRetry: { viewModel.loadShelf() })
        case .empty:
            VStack {
                Spacer()
                Text(String(localized: "no_books_shelved"))
                    .font(.kluvsBodyLg)
                    .foregroundColor(.secondary)
                Spacer()
            }
        case .content:
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    ForEach(shelfSections, id: \.ordinal) { section in
                        let entries = viewModel.shelfEntries.filter { $0.shelf == section }
                        if !entries.isEmpty {
                            ShelfSectionView(section: section, entries: entries, viewModel: viewModel)
                        }
                    }
                }
                .padding(.bottom, 16)
            }
        }
    }
}

private struct ShelfSectionView: View {
    let section: Shared.ShelfStatus
    let entries: [Shared.ShelfEntry]
    let viewModel: BooksViewModelWrapper

    @Environment(\.colorScheme) private var colorScheme

    private var eyebrowColor: Color { colorScheme == .dark ? Color(hex: 0xB0B0B0) : .foregroundLightSecondary }
    private var countColor: Color { colorScheme == .dark ? .foregroundWarmTertiary : .foregroundLightTertiary }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(alignment: .lastTextBaseline, spacing: 8) {
                // Eyebrow — design-system component.eyebrow: IBM Plex Sans 11px/500, uppercase, 0.14em tracking
                Text(sectionLabel(section).uppercased())
                    .font(.kluvsModalLabel)
                    .kerning(1.5) // ~0.14em at 11pt
                    .foregroundColor(eyebrowColor)
                Text("\(entries.count)")
                    .font(.system(size: 10))
                    .foregroundColor(countColor)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 4)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(entries, id: \.book.id) { entry in
                        BookCard(
                            book: entry.book,
                            shelfStatus: entry.shelf,
                            shelfSource: entry.source,
                            onShelfChange: { newShelf in
                                if let newShelf {
                                    viewModel.onAssignShelf(bookId: entry.book.id, shelf: newShelf)
                                } else {
                                    viewModel.onRemoveFromShelf(bookId: entry.book.id)
                                }
                            }
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
                                shelfStatus: nil,
                                onShelfChange: { newShelf in
                                    if let newShelf {
                                        viewModel.onAssignShelf(bookId: book.id, shelf: newShelf)
                                    } else {
                                        viewModel.onRemoveFromShelf(bookId: book.id)
                                    }
                                }
                            )
                        }
                    }
                    .padding(16)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct SearchEmptyState: View {
    let heading: String
    let bodyText: String

    var body: some View {
        VStack {
            Spacer()
            VStack(spacing: 16) {
                StackedCoverPlaceholder()
                VStack(spacing: 4) {
                    Text(heading)
                        .font(.ebGaramondMediumItalic(size: 28))
                        .foregroundColor(.secondary)
                    Text(bodyText)
                        .font(.kluvsBody)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
            }
            Spacer()
        }
    }
}

#Preview {
    BooksView()
}
