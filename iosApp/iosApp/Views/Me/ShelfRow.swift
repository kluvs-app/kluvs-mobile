import SwiftUI
import Shared

/// A single "On Your Shelf" row: the active-session book for one of the
/// member's clubs, with cover, club name, next discussion date, and the
/// shared `OwnProgressRow` for viewing/editing the member's own progress.
/// Mirrors web's ProfilePage `ShelfRow`.
struct ShelfRow: View {
    let item: Shared.ShelfItem
    let onUpdateProgress: (String) -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            BookCoverImage(imageUrl: item.bookCoverUrl, width: 52)

            VStack(alignment: .leading, spacing: 12) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(item.bookTitle)
                            .font(.ebGaramondItalic(size: 17))
                            .lineLimit(1)
                        Text(item.bookAuthor)
                            .font(.kluvsBody)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                    Spacer()
                    Text(item.clubName.uppercased())
                        .font(.kluvsEyebrow)
                        .foregroundColor(.secondary)
                }

                OwnProgressRow(
                    ownProgress: item.ownProgress,
                    onUpdateProgress: { onUpdateProgress(item.sessionId) },
                    leftLabel: item.nextDiscussionDate.map { "Next · \($0)" } ?? "Your progress"
                )
            }
        }
    }
}

#Preview {
    ShelfRow(
        item: Shared.ShelfItem(
            sessionId: "s0", bookId: "b0", bookTitle: "How AI Thinks", bookAuthor: "Nigel Toon",
            bookCoverUrl: nil, bookPageCount: 328, clubId: "c0", clubName: "Showcase Kluv",
            nextDiscussionDate: "December 31, 2026", ownProgress: nil
        ),
        onUpdateProgress: { _ in }
    )
    .padding()
}
