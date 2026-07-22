//
//  BookDetailView.swift
//  iosApp
//
import SwiftUI
import Shared

private let languageDisplayNames: [String: String] = [
    "en": "English", "es": "Spanish", "fr": "French", "de": "German",
    "it": "Italian", "pt": "Portuguese", "nl": "Dutch", "ja": "Japanese",
    "zh": "Chinese", "ko": "Korean", "ru": "Russian", "ar": "Arabic",
    "hi": "Hindi", "pl": "Polish", "sv": "Swedish", "tr": "Turkish"
]

private func displayLanguage(_ code: String) -> String {
    languageDisplayNames[code.lowercased()] ?? code
}

private func primaryAuthor(_ author: String) -> String {
    let parts = author.components(separatedBy: try! NSRegularExpression(pattern: "\\s*(?:,|&| and )\\s*", options: .caseInsensitive))
    return parts.first?.trimmingCharacters(in: .whitespaces) ?? author
}

private extension String {
    func components(separatedBy regex: NSRegularExpression) -> [String] {
        let range = NSRange(startIndex..., in: self)
        var result: [String] = []
        var lastEnd = startIndex
        regex.enumerateMatches(in: self, range: range) { match, _, _ in
            guard let match, let matchRange = Range(match.range, in: self) else { return }
            result.append(String(self[lastEnd..<matchRange.lowerBound]))
            lastEnd = matchRange.upperBound
        }
        result.append(String(self[lastEnd...]))
        return result
    }
}

// `book.year`/`book.pageCount` bridge as boxed `KotlinInt` (an NSNumber subclass);
// interpolating them directly invokes NSNumber's locale-aware `description` (e.g. "2,025") —
// pull out `.intValue` first for a plain digit string.
private func metaLine(book: Shared.Book, volumeInfo: Shared.BookVolumeInfo?) -> String {
    var parts: [String] = [book.author]
    if let year = book.year { parts.append("\(year.intValue)") }
    if let pages = book.pageCount { parts.append("\(pages.intValue) pages") }
    if let publisher = volumeInfo?.publisher { parts.append(publisher) }
    return parts.joined(separator: " · ")
}

private struct DetailRowData: Identifiable {
    let id: String
    let label: String
    let value: String
}

private func buildDetailRows(book: Shared.Book, volumeInfo: Shared.BookVolumeInfo?) -> [DetailRowData] {
    var rows: [DetailRowData] = []
    if let year = book.year {
        rows.append(DetailRowData(id: "published", label: String(localized: "book_field_published"), value: "\(year.intValue)"))
    }
    if let pages = book.pageCount {
        rows.append(DetailRowData(id: "pages", label: String(localized: "book_field_pages"), value: "\(pages.intValue)"))
    }
    if let publisher = volumeInfo?.publisher {
        rows.append(DetailRowData(id: "publisher", label: String(localized: "book_field_publisher"), value: publisher))
    }
    let isbn = volumeInfo?.isbn13 ?? volumeInfo?.isbn10 ?? book.isbn
    if let isbn {
        rows.append(DetailRowData(id: "isbn", label: String(localized: "book_field_isbn"), value: isbn))
    }
    if let language = volumeInfo?.language {
        rows.append(DetailRowData(id: "language", label: String(localized: "book_field_language"), value: displayLanguage(language)))
    }
    if let edition = book.edition {
        rows.append(DetailRowData(id: "edition", label: String(localized: "book_field_edition"), value: edition))
    }
    return rows
}

/// Book detail screen: cover header, category chips, shelf/like actions, "About",
/// "Details", "About the Author", and "More by this author". Mirrors web's
/// `BooksPage.tsx` detail panel section order, and Android's `BookDetailScreen`.
struct BookDetailView: View {
    let book: Shared.Book
    let initialShelfStatus: Shared.ShelfStatus?
    let initialShelfSource: Shared.ShelfSource?
    let onNavigateToBook: (Shared.Book) -> Void

    @StateObject private var viewModel = BookDetailsViewModelWrapper()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Button(action: { dismiss() }) {
                        Image(systemName: "chevron.left")
                            .foregroundColor(.primary)
                    }
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.top, 12)
                .padding(.bottom, 8)

                if viewModel.isMutationInProgress {
                    ProgressView()
                        .progressViewStyle(LinearProgressViewStyle())
                        .tint(.brandOrange)
                }

                if let currentBook = viewModel.book {
                    content(for: currentBook)
                        .padding(.horizontal, 16)
                        .padding(.top, 12)
                        .padding(.bottom, 32)
                }
            }
        }
        .background(Color.kluvsBackground)
        .toolbar(.hidden, for: .navigationBar)
        .onAppear {
            viewModel.load(book: book, shelfStatus: initialShelfStatus, shelfSource: initialShelfSource)
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

    @ViewBuilder
    private func content(for currentBook: Shared.Book) -> some View {
        let volumeInfo = viewModel.enrichment?.volumeInfo

        VStack(alignment: .leading, spacing: 20) {
            // Cover header
            HStack(alignment: .top, spacing: 16) {
                coverView(currentBook)
                    .aspectRatio(2.0 / 3.0, contentMode: .fit)
                    .frame(width: 120)
                    .clipShape(RoundedRectangle(cornerRadius: 4))

                VStack(alignment: .leading, spacing: 4) {
                    Text(currentBook.title)
                        .font(.ebGaramondMediumItalic(size: 24))
                        .foregroundColor(.primary)
                    if let subtitle = volumeInfo?.subtitle {
                        Text(subtitle)
                            .font(.ebGaramond(size: 16))
                            .foregroundColor(.secondary)
                    }
                    Text(metaLine(book: currentBook, volumeInfo: volumeInfo))
                        .font(.kluvsBody)
                        .foregroundColor(.secondary)
                }
            }

            if let categories = volumeInfo?.categories, !categories.isEmpty {
                FlowLayout(spacing: 8) {
                    ForEach(Array(categories.prefix(5)), id: \.self) { category in
                        CategoryChip(text: category)
                    }
                }
            }

            BookDetailActionsView(
                isRegistered: viewModel.isRegistered,
                shelfStatus: viewModel.shelfStatus,
                isLiked: viewModel.isLiked,
                isMutationInProgress: viewModel.isMutationInProgress,
                onShelfChange: { shelf in
                    if let shelf {
                        viewModel.onAssignShelf(shelf)
                    } else {
                        viewModel.onRemoveFromShelf()
                    }
                },
                onToggleLike: { viewModel.onToggleLike() }
            )

            Divider()

            // About
            VStack(alignment: .leading, spacing: 12) {
                SectionEyebrow(String(localized: "book_about"))
                // `.description` collides with NSObject's inherited debug description — Kotlin/
                // Native exports the real field as `description_` to avoid the clash.
                if let description = volumeInfo?.description_ {
                    Text(description)
                        .font(.kluvsBodyLg)
                        .foregroundColor(.primary)
                } else {
                    Text(String(localized: "book_no_description"))
                        .font(.kluvsBodyLg)
                        .italic()
                        .foregroundColor(.secondary)
                }
            }

            let detailRows = buildDetailRows(book: currentBook, volumeInfo: volumeInfo)
            if !detailRows.isEmpty {
                Divider()
                VStack(alignment: .leading, spacing: 4) {
                    SectionEyebrow(String(localized: "book_details"))
                    ForEach(Array(detailRows.enumerated()), id: \.element.id) { index, row in
                        if index > 0 { Divider() }
                        DetailRowView(label: row.label, value: row.value)
                    }
                }
            }

            if viewModel.isLoadingEnrichment || viewModel.enrichment?.author != nil {
                Divider()
                VStack(alignment: .leading, spacing: 12) {
                    SectionEyebrow(String(localized: "book_about_the_author"))
                    AuthorSectionView(isLoading: viewModel.isLoadingEnrichment, author: viewModel.enrichment?.author)
                }
            }

            let authorBooks = viewModel.enrichment?.authorBooks ?? []
            if !authorBooks.isEmpty {
                Divider()
                VStack(alignment: .leading, spacing: 12) {
                    SectionEyebrow(String(format: String(localized: "book_more_by_x"), primaryAuthor(currentBook.author)))
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(authorBooks, id: \.id) { authorBook in
                                BookCard(book: authorBook, onTap: { onNavigateToBook(authorBook) })
                            }
                        }
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func coverView(_ book: Shared.Book) -> some View {
        if let urlString = book.imageUrl, let url = URL(string: urlString) {
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
}

private struct SectionEyebrow: View {
    let text: String
    init(_ text: String) { self.text = text }

    var body: some View {
        Text(text.uppercased())
            .font(.kluvsModalLabel)
            .kerning(1.5)
            .foregroundColor(.secondary)
    }
}

private struct CategoryChip: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.kluvsModalLabel)
            .foregroundColor(.secondary)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .overlay(Capsule().stroke(Color.secondary.opacity(0.4), lineWidth: 1))
    }
}

private struct DetailRowView: View {
    let label: String
    let value: String

    var body: some View {
        HStack(alignment: .top, spacing: 24) {
            Text(label)
                .font(.kluvsBody)
                .foregroundColor(.secondary)
                .frame(width: 90, alignment: .leading)
            Text(value)
                .font(.kluvsBody)
                .foregroundColor(.primary)
        }
        .padding(.vertical, 12)
    }
}

/// Minimal wrapping HStack layout for category chips (mirrors Compose's `FlowRow`).
private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.width ?? .infinity
        var rowWidth: CGFloat = 0
        var totalHeight: CGFloat = 0
        var rowHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if rowWidth + size.width > width, rowWidth > 0 {
                totalHeight += rowHeight + spacing
                rowWidth = 0
                rowHeight = 0
            }
            rowWidth += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        totalHeight += rowHeight
        return CGSize(width: width, height: totalHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var rowHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX, x > bounds.minX {
                x = bounds.minX
                y += rowHeight + spacing
                rowHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}

#Preview {
    NavigationStack {
        BookDetailView(
            book: Book(id: "1", title: "The Hobbit", author: "J.R.R. Tolkien", edition: nil, year: 1937, isbn: "978-0-395-07122-1", pageCount: nil, imageUrl: nil, externalGoogleId: nil),
            initialShelfStatus: .currentlyReading,
            initialShelfSource: nil,
            onNavigateToBook: { _ in }
        )
    }
}
