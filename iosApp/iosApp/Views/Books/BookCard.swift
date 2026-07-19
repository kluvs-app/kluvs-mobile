//
//  BookCard.swift
//  iosApp
//
import SwiftUI
import Shared

private let assignableShelfStatuses: [Shared.ShelfStatus] = [.currentlyReading, .read, .wantToRead, .notFinished]

/// A single book tile: cover (with a read-ribbon badge for Kluvs-session books), title,
/// author, year, and a shelf-status menu.
///
/// The shelf menu lives directly on the card since there is no book detail screen yet.
struct BookCard: View {
    let book: Shared.Book
    let shelfStatus: Shared.ShelfStatus?
    var shelfSource: Shared.ShelfSource? = nil
    let onShelfChange: (Shared.ShelfStatus?) -> Void
    var onTap: () -> Void = {}

    /// Search results straight from Google Books have no local DB id yet (book detail /
    /// registration flow is a separate ticket), so shelving isn't available until then.
    private var isRegistered: Bool { Int(book.id) != nil }

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

            HStack(alignment: .top, spacing: 4) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(book.title)
                        .font(.kluvsCardHeading)
                        .foregroundColor(.primary)
                        .lineLimit(2)
                    Text(book.author)
                        .font(.kluvsBody)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                    if let year = book.year {
                        Text("\(year)")
                            .font(.kluvsBody)
                            .foregroundColor(.secondary)
                    }
                }

                if isRegistered {
                    Spacer(minLength: 0)
                    Menu {
                        Button {
                            onShelfChange(nil)
                        } label: {
                            if shelfStatus == nil {
                                Label(String(localized: "shelf_none"), systemImage: "checkmark")
                            } else {
                                Text(String(localized: "shelf_none"))
                            }
                        }
                        ForEach(assignableShelfStatuses, id: \.ordinal) { status in
                            Button {
                                onShelfChange(status)
                            } label: {
                                if shelfStatus == status {
                                    Label(shelfLabel(status), systemImage: "checkmark")
                                } else {
                                    Text(shelfLabel(status))
                                }
                            }
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .foregroundColor(.secondary)
                            .frame(width: 24, height: 24)
                    }
                }
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

private func shelfLabel(_ status: Shared.ShelfStatus) -> String {
    switch status {
    case .currentlyReading: return String(localized: "shelf_currently_reading")
    case .read: return String(localized: "shelf_read")
    case .wantToRead: return String(localized: "shelf_want_to_read")
    case .notFinished: return String(localized: "shelf_not_finished")
    default: return ""
    }
}

#Preview {
    BookCard(
        book: Book(id: "42", title: "The Hobbit", author: "J.R.R. Tolkien", edition: nil, year: 1937, isbn: "978-0-395-07122-1", pageCount: nil, imageUrl: nil, externalGoogleId: nil),
        shelfStatus: .currentlyReading,
        shelfSource: .session,
        onShelfChange: { _ in }
    )
    .padding()
}
