import SwiftUI
import Shared
//
//  BookCard.swift
//  iosApp
//
import DesignSystem

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
                // Explicit frame, not `.aspectRatio(_, contentMode: .fit)` — `.fit` sizes off
                // whichever branch of `coverView` is active (loaded image vs. placeholder) instead
                // of always landing on exactly 120x180, which is what made cards look inconsistently
                // sized card-to-card. Mirrors Android's `fillMaxWidth().aspectRatio(2f/3f)`, which
                // pins the width first rather than letting content dictate the frame.
                coverView
                    .frame(width: 120, height: 180)
                    .clipShape(RoundedRectangle(cornerRadius: 4)) // radius.sm — design-system/docs/book-cover.md

                if shelfSource == .session {
                    ReadRibbon(size: .lg, contentDescription: String(localized: "kluvs_read_ribbon"))
                }
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(book.title)
                    .kluvsStyle(KluvsTheme.typography.title.small)
                    .foregroundColor(KluvsTheme.colors.content)
                    .lineLimit(2)
                    .frame(maxWidth: .infinity, alignment: .topLeading)
                Text(book.author)
                    .kluvsStyle(KluvsTheme.typography.caption)
                    .foregroundColor(KluvsTheme.colors.contentMuted)
                    .lineLimit(1)
                // Always reserve the year line's height, even when there's no year to show —
                // `book.year` is nullable, and omitting the row entirely made those cards shorter.
                Text(book.year.map(formattedYear) ?? " ")
                    .kluvsStyle(KluvsTheme.typography.caption)
                    .foregroundColor(book.year != nil ? KluvsTheme.colors.contentMuted : .clear)
            }
        }
        .frame(width: 120)
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }

    /// `book.year` bridges as boxed `KotlinInt` (an `NSNumber` subclass). Formatting is delegated
    /// to the shared `formatBookYear` so BCE years ("380 BCE") and the plain, ungrouped digit
    /// string for everything else come out identically to Android — no locale-aware
    /// `NSNumber.description` involved ("2,025"), since the string is built in Kotlin.
    private func formattedYear(_ year: KotlinInt) -> String {
        FormatBookYearKt.formatBookYear(year: year.int32Value)
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
