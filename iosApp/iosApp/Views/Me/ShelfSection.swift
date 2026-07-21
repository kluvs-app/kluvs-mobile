import SwiftUI
import Shared

/// "On Your Shelf" section on the Me screen: eyebrow header + book count
/// caption, then one `ShelfRow` per active-session book. Mirrors web's
/// ProfilePage shelf list.
struct ShelfSection: View {
    let shelf: [Shared.ShelfItem]
    let onUpdateProgress: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(alignment: .bottom) {
                Text(String(localized: "on_your_shelf").uppercased())
                    .font(.kluvsEyebrow)
                    .foregroundColor(.secondary)
                Spacer()
                if !shelf.isEmpty {
                    Text(String(format: NSLocalizedString("books_in_progress_x", comment: ""), shelf.count))
                        .font(.ebGaramondItalic(size: 15))
                        .foregroundColor(.secondary)
                }
            }

            VStack(alignment: .leading, spacing: 20) {
                ForEach(shelf, id: \.sessionId) { item in
                    ShelfRow(item: item, onUpdateProgress: onUpdateProgress)
                }
            }
        }
        .padding()
    }
}

#Preview {
    ShelfSection(
        shelf: [
            Shared.ShelfItem(
                sessionId: "s0", bookId: "b0", bookTitle: "How AI Thinks", bookAuthor: "Nigel Toon",
                bookCoverUrl: nil, bookPageCount: 328, clubId: "c0", clubName: "Showcase Kluv",
                nextDiscussionDate: "December 31, 2026", ownProgress: nil
            )
        ],
        onUpdateProgress: { _ in }
    )
}
