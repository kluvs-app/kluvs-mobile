import SwiftUI
import Shared

/// Sheet showing the signed-in member's reading log: sessions grouped into
/// "Currently Reading" and "Read". Mirrors web's ReadingLogModal / Android's
/// ReadingLogBottomSheet.
struct ReadingLogSheet: View {
    let log: Shared.ReadingLog?
    let isLoading: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(String(localized: "reading_log"))
                .font(.kluvsSectionHeading)

            if isLoading {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
                .padding(.vertical, 32)
            } else {
                ReadingLogGroup(title: String(localized: "shelf_currently_reading"), entries: log?.active ?? [])
                Divider()
                ReadingLogGroup(title: String(localized: "shelf_read"), entries: log?.finished ?? [])
            }
        }
        .padding(20)
        .padding(.bottom, 20)
    }
}

private struct ReadingLogGroup: View {
    let title: String
    let entries: [Shared.ReadingLogEntry]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title.uppercased())
                .font(.kluvsEyebrow)
                .foregroundColor(.secondary)

            if entries.isEmpty {
                Text(String(localized: "nothing_here_yet"))
                    .font(.ebGaramondItalic(size: 15))
                    .foregroundColor(.secondary)
            } else {
                VStack(alignment: .leading, spacing: 12) {
                    ForEach(entries, id: \.sessionId) { entry in
                        ReadingLogRow(entry: entry)
                    }
                }
            }
        }
    }
}

private struct ReadingLogRow: View {
    let entry: Shared.ReadingLogEntry

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            BookCoverImage(imageUrl: entry.book?.imageUrl, width: 40)

            VStack(alignment: .leading, spacing: 2) {
                Text(entry.book?.title ?? "")
                    .font(.ebGaramondItalic(size: 16))
                    .lineLimit(1)
                Text(entry.book?.author ?? "")
                    .font(.kluvsBody)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                Text((entry.club?.name ?? "").uppercased())
                    .font(.plexSansMedium(size: 11))
                    .foregroundColor(.secondary)
            }
        }
    }
}

#Preview {
    ReadingLogSheet(log: nil, isLoading: false)
}
