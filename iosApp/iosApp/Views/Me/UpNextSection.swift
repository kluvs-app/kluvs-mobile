import SwiftUI
import Shared

/// "Up Next" section: the nearest upcoming discussion across all of the
/// member's clubs. Flat section matching the rest of the Me screen — no card
/// fill/border. Read-only; attendance/RSVP is a separate ticket. Renders
/// nothing when there's no upcoming discussion.
struct UpNextSection: View {
    let upNext: Shared.UpNextItem?

    var body: some View {
        if let upNext {
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(String(localized: "up_next_eyebrow").uppercased())
                        .font(.kluvsEyebrow)
                        .foregroundColor(.brandOrange)
                    Spacer()
                    Text(upNext.date)
                        .font(.kluvsEyebrow)
                        .foregroundColor(.brandOrange)
                }

                Text(upNext.title)
                    .font(.ebGaramondItalic(size: 20))

                Text([upNext.clubName, upNext.location].compactMap { $0 }.joined(separator: " — "))
                    .font(.kluvsBody)
                    .foregroundColor(.secondary)
            }
            .padding()
        }
    }
}

#Preview {
    UpNextSection(
        upNext: Shared.UpNextItem(
            title: "End-of-Year Check-in",
            clubName: "Showcase Kluv",
            location: "Online",
            date: "December 31, 2026"
        )
    )
}
