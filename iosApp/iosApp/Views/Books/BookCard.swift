//
//  BookCard.swift
//  iosApp
//
import SwiftUI
import Shared

/// A single book tile: cover (with a read-ribbon badge for Kluvs-session books), title,
/// author, and year. Purely a browsing tile — tapping navigates to the book detail screen,
/// where shelf/like functionality actually lives.
struct BookCard: View {
    let book: Shared.Book
    var shelfSource: Shared.ShelfSource? = nil
    var onTap: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            ZStack(alignment: .topTrailing) {
                coverView
                    .aspectRatio(2.0 / 3.0, contentMode: .fit)
                    .clipShape(RoundedRectangle(cornerRadius: 4)) // radius.sm — design-system/docs/book-cover.md

                if shelfSource == .session {
                    ReadRibbon(size: .lg, contentDescription: String(localized: "kluvs_read_ribbon"))
                }
            }

            VStack(alignment: .leading, spacing: 2) {
                // Reserve full 2-line height regardless of actual title length (real font
                // metrics, not a guessed pixel height), so 1-line and 2-line titles don't
                // produce differently sized cards in the same row.
                Text(book.title)
                    .font(.kluvsCardHeading)
                    .foregroundColor(.primary)
                    .lineLimit(2, reservesSpace: true)
                    .frame(maxWidth: .infinity, alignment: .topLeading)
                Text(book.author)
                    .font(.kluvsBody)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                // Always reserve the year line's height, even when there's no year to show —
                // `book.year` is nullable, and omitting the row entirely made those cards shorter.
                // `book.year` bridges as boxed `KotlinInt` (an NSNumber subclass); interpolating
                // it directly invokes NSNumber's locale-aware `description` (e.g. "2,025") —
                // pull out `.intValue` first for a plain digit string.
                Text(book.year.map { "\($0.intValue)" } ?? " ")
                    .font(.kluvsBody)
                    .foregroundColor(book.year != nil ? .secondary : .clear)
            }
        }
        .frame(width: 120)
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }

    @ViewBuilder
    private var coverView: some View {
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

#Preview {
    BookCard(
        book: Book(id: "42", title: "The Hobbit", author: "J.R.R. Tolkien", edition: nil, year: 1937, isbn: "978-0-395-07122-1", pageCount: nil, imageUrl: nil, externalGoogleId: nil),
        shelfSource: .session
    )
    .padding()
}
