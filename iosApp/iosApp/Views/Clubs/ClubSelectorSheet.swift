import SwiftUI
import Shared

struct ClubSelectorSheet: View {
    let clubs: [Shared.ClubListItem]
    let selectedClubId: String?
    let onClubSelected: (String) -> Void

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Switch Club")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 8)

            Divider()

            ForEach(clubs, id: \.id) { club in
                Button(action: {
                    onClubSelected(club.id)
                    dismiss()
                }) {
                    HStack {
                        Text(club.name)
                            .font(.body)
                            .foregroundColor(.primary)

                        Spacer()

                        if club.id == selectedClubId {
                            Image(systemName: "checkmark")
                                .foregroundColor(.brandOrange)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 14)
                }

                Divider()
                    .padding(.leading, 16)
            }
        }
        .presentationDetents([.height(CGFloat(64 + clubs.count * 52))])
        .presentationDragIndicator(.visible)
    }
}

#Preview {
    ClubSelectorSheet(
        clubs: [
            ClubListItem(id: "1", name: "My Book Club", role: Role.admin),
            ClubListItem(id: "2", name: "Sci-Fi Readers", role: Role.admin),
            ClubListItem(id: "3", name: "Classic Literature", role: Role.admin)
        ],
        selectedClubId: "1",
        onClubSelected: { _ in }
    )
}
