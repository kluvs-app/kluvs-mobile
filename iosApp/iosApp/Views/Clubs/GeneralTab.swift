import SwiftUI
import Shared

struct GeneralTab: View {
    let clubDetails: Shared.ClubDetails?
    var userRole: Shared.Role? = nil
    var onEditClub: () -> Void = {}
    var onDeleteClub: () -> Void = {}

    private var isOwner: Bool { userRole == .owner }

    var body: some View {
        ScrollView {
            if let clubDetails = clubDetails {
                VStack(spacing: 12) {
                    // Club Info Card
                    VStack(alignment: .leading, spacing: 8) {
                        if isOwner {
                            HStack {
                                Spacer()
                                Menu {
                                    Button("Edit Club Name") { onEditClub() }
                                    Button("Delete Club", role: .destructive) { onDeleteClub() }
                                } label: {
                                    Image(systemName: "ellipsis.circle")
                                        .foregroundColor(.secondary)
                                }
                            }
                        }

                        if let foundedYear = clubDetails.foundedYear {
                            Text(String(format: NSLocalizedString("label_founded_in", comment: ""), foundedYear))
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }

                        Text(String(format: NSLocalizedString("label_members_count", comment: ""), clubDetails.memberCount))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()

                    Divider()

                    // Current Book Card
                    VStack(alignment: .leading, spacing: 8) {
                        Text("section_current_book")
                            .font(.headline)
                            .foregroundColor(.secondary)

                        if let book = clubDetails.currentBook {
                            Text(book.title)
                                .font(.body)
                                .fontWeight(.medium)

                            Text(book.author)
                                .font(.subheadline)
                                .foregroundColor(.secondary)

                            HStack(spacing: 4) {
                                Text(book.year ?? String(localized: "label_not_available"))
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)

                                if let pageCount = book.pageCount {
                                    Text("•")
                                        .foregroundColor(.secondary)
                                    Text(String(format: NSLocalizedString("label_pages", comment: ""), pageCount.int32Value))
                                        .font(.subheadline)
                                        .foregroundColor(.secondary)
                                }
                            }
                        } else {
                            NoSectionData(text: String(localized: "empty_no_book_data"))
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()

                    Divider()

                    // Next Discussion Card
                    VStack(alignment: .leading, spacing: 8) {
                        Text("section_next_discussion")
                            .font(.headline)
                            .foregroundColor(.secondary)

                        if let discussion = clubDetails.nextDiscussion {
                            NextDiscussionCard(
                                title: discussion.title,
                                location: discussion.location,
                                formattedDate: discussion.formattedDate
                            )
                        } else {
                            NoSectionData(text: String(localized: "empty_no_upcoming_discussion"))
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                }
                .padding()
            } else {
                NoTabData(text: String(localized: "empty_no_club_details"))
            }
        }
    }
}

#Preview {
    GeneralTab(clubDetails: nil)
}
