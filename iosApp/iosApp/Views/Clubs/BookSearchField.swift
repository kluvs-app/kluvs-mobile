import SwiftUI
import Shared
import DesignSystem

/// Search-and-select field for choosing a book when creating a session. Debounced (400ms)
/// free-text search yields a dropdown of results rendered via `.popover` — SwiftUI's own
/// built-in mechanism for floating content anchored to a view, escaping the enclosing
/// hierarchy's clipping/layout proposal. Two earlier hand-rolled attempts (an in-hierarchy
/// `.overlay`, then a manual `UIWindow`) both broke in ways that were never confirmed root-
/// caused, since neither could be debugged live on the physical test device. `.popover` sidesteps
/// that whole class of bug by using Apple's own implementation instead of custom coordinate-space/
/// window-level code. `.presentationCompactAdaptation(.popover)` (iOS 16.4+; this app targets
/// 18.2+) keeps it a true popover on iPhone instead of the default compact-size-class fallback,
/// which is a full-screen sheet. Selecting a result collapses the field into a locked "selected
/// book" row, mirroring web's `BookSearchInput`.
struct BookSearchField: View {
    @Binding var query: String
    let results: [Shared.Book]
    let selectedBook: Shared.Book?
    let isSearching: Bool
    let isRegistering: Bool
    let error: String?
    let onSearch: (String) -> Void
    let onSelect: (Shared.Book) -> Void
    let onClear: () -> Void

    @State private var isPopoverPresented = false

    private var trimmedQuery: String { query.trimmingCharacters(in: .whitespaces) }

    private var shouldShowDropdown: Bool {
        !trimmedQuery.isEmpty && (!results.isEmpty || !isSearching)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let selectedBook {
                SelectedBookRow(book: selectedBook, onClear: onClear)
            } else {
                InputField(
                    label: "Book",
                    value: $query,
                    helperText: (isSearching || isRegistering) ? "Searching…" : nil
                )
                .popover(isPresented: $isPopoverPresented, arrowEdge: .bottom) {
                    dropdownContent
                        .frame(width: 340)
                        .presentationCompactAdaptation(.popover)
                }
                .onChange(of: shouldShowDropdown) { _, newValue in isPopoverPresented = newValue }
                .task(id: query) {
                    guard !trimmedQuery.isEmpty else { return }
                    try? await Task.sleep(nanoseconds: 400_000_000)
                    if !Task.isCancelled { onSearch(query) }
                }
            }

            if let error {
                Text(error)
                    .kluvsStyle(KluvsTheme.typography.caption)
                    .foregroundColor(KluvsTheme.colors.danger)
            }
        }
    }

    @ViewBuilder
    private var dropdownContent: some View {
        if !results.isEmpty {
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(Array(results.enumerated()), id: \.offset) { _, book in
                        BookResultRow(book: book, onTap: { onSelect(book) })
                    }
                }
                .padding(.top, 8)
            }
            .frame(maxHeight: 240)
            .background(KluvsTheme.colors.card)
            .clipShape(RoundedRectangle(cornerRadius: 8))
        } else {
            Text("No books found for \"\(query)\"")
                .kluvsStyle(KluvsTheme.typography.caption)
                .foregroundColor(KluvsTheme.colors.contentMuted)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(12)
                .background(KluvsTheme.colors.card)
                .clipShape(RoundedRectangle(cornerRadius: 8))
        }
    }
}

private struct BookResultRow: View {
    let book: Shared.Book
    let onTap: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            BookThumbnail(imageUrl: book.imageUrl)
            VStack(alignment: .leading, spacing: 2) {
                Text(book.title)
                    .kluvsStyle(KluvsTheme.typography.body.medium)
                    .foregroundColor(KluvsTheme.colors.content)
                    .lineLimit(1)
                Text(book.author)
                    .kluvsStyle(KluvsTheme.typography.caption)
                    .foregroundColor(KluvsTheme.colors.contentMuted)
                    .lineLimit(1)
                if let year = book.year {
                    Text(FormatBookYearKt.formatBookYear(year: year.int32Value))
                        .kluvsStyle(KluvsTheme.typography.caption)
                        .foregroundColor(KluvsTheme.colors.contentMuted)
                }
            }
            Spacer()
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }
}

private struct SelectedBookRow: View {
    let book: Shared.Book
    let onClear: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            BookThumbnail(imageUrl: book.imageUrl)
            VStack(alignment: .leading, spacing: 2) {
                Text(book.title)
                    .kluvsStyle(KluvsTheme.typography.body.medium)
                    .foregroundColor(KluvsTheme.colors.content)
                    .lineLimit(1)
                Text(book.author)
                    .kluvsStyle(KluvsTheme.typography.caption)
                    .foregroundColor(KluvsTheme.colors.contentMuted)
                    .lineLimit(1)
            }
            Spacer()
            Icon(type: .close, contentDescription: "Change book", tint: KluvsTheme.colors.contentMuted)
                .frame(width: 18, height: 18)
                .contentShape(Rectangle())
                .onTapGesture(perform: onClear)
        }
        .padding(12)
        .background(KluvsTheme.colors.card)
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

private struct BookThumbnail: View {
    let imageUrl: String?

    var body: some View {
        Group {
            if let imageUrl, let url = URL(string: imageUrl) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image.resizable().aspectRatio(contentMode: .fill)
                    default:
                        BookCoverPlaceholder()
                    }
                }
            } else {
                BookCoverPlaceholder()
            }
        }
        .frame(width: 40, height: 60)
        .clipShape(RoundedRectangle(cornerRadius: 4))
    }
}

#Preview {
    VStack(spacing: 24) {
        BookSearchField(
            query: .constant("hobbit"),
            results: [
                Book(id: "1", title: "The Hobbit", author: "J.R.R. Tolkien", edition: nil, year: 1937, isbn: nil, pageCount: nil, imageUrl: nil, externalGoogleId: nil),
                Book(id: "2", title: "The Hobbit: Illustrated Edition", author: "J.R.R. Tolkien", edition: nil, year: 1997, isbn: nil, pageCount: nil, imageUrl: nil, externalGoogleId: nil),
            ],
            selectedBook: nil,
            isSearching: false,
            isRegistering: false,
            error: nil,
            onSearch: { _ in },
            onSelect: { _ in },
            onClear: {}
        )
        BookSearchField(
            query: .constant("The Hobbit"),
            results: [],
            selectedBook: Book(id: "1", title: "The Hobbit", author: "J.R.R. Tolkien", edition: nil, year: 1937, isbn: nil, pageCount: nil, imageUrl: nil, externalGoogleId: nil),
            isSearching: false,
            isRegistering: false,
            error: nil,
            onSearch: { _ in },
            onSelect: { _ in },
            onClear: {}
        )
    }
    .padding()
}
