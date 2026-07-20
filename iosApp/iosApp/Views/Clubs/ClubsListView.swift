import SwiftUI
import Shared

/// Entry-point list of the member's clubs — mirrors web's `/clubs` page / Android's
/// `ClubsListScreen`. Tapping a row navigates into the club detail screen. The FAB
/// opens `CreateClubSheet`.
struct ClubsListView: View {
    let clubs: [Shared.ClubListItem]
    let onClubSelected: (String) -> Void
    let onAddClub: () -> Void

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            Color.kluvsBackground.ignoresSafeArea()

            if clubs.isEmpty {
                emptyState
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        header
                        ForEach(clubs, id: \.id) { club in
                            Button(action: { onClubSelected(club.id) }) {
                                ClubListRow(club: club)
                            }
                            .buttonStyle(.plain)
                            Divider()
                        }
                    }
                }
                .background(Color.kluvsBackground)
            }

            Button(action: onAddClub) {
                Image(systemName: "plus")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(.brandOnPrimary)
                    .frame(width: 56, height: 56)
                    .background(Color.brandOrange)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .shadow(radius: 4)
            }
            .padding(16)
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("YOUR")
                .font(.kluvsEyebrow)
                .foregroundColor(.secondary)
            Text("Clubs")
                .font(.kluvsDisplay2)
                .foregroundColor(.primary)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 20)
    }

    private var emptyState: some View {
        VStack(spacing: 8) {
            Text("No clubs yet")
                .font(.kluvsSectionHeading)
            Text("Join a club to get started")
                .font(.kluvsBody)
                .foregroundColor(.secondary)
        }
    }
}

private struct ClubListRow: View {
    let club: Shared.ClubListItem

    var body: some View {
        HStack(spacing: 12) {
            BookCoverImage(imageUrl: club.bookCoverUrl, width: 40)

            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 8) {
                    Text(club.name)
                        .font(.kluvsCardHeading)
                        .foregroundColor(.primary)
                    if let role = club.role {
                        RoleEyebrow(role: role)
                    }
                }

                if let bookTitle = club.bookTitle {
                    Text(bookTitle)
                        .font(.ebGaramondItalic(size: 14))
                        .foregroundColor(.secondary)
                }

                if !club.memberAvatarUrls.isEmpty {
                    AvatarStack(
                        members: club.memberAvatarUrls.map {
                            AvatarStackMember(id: $0.memberId, name: $0.name, avatarUrl: $0.avatarUrl)
                        },
                        size: 20
                    )
                }
            }

            Spacer()

            Image(systemName: "chevron.right")
                .foregroundColor(.secondary)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 16)
        .contentShape(Rectangle())
    }
}

#Preview {
    ClubsListView(
        clubs: [],
        onClubSelected: { _ in },
        onAddClub: {}
    )
}
