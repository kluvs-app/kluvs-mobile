import SwiftUI
import Shared

struct ActiveSessionTab: View {
    let sessionDetails: Shared.ActiveSessionDetails?
    var userRole: Shared.Role? = nil
    var onCreateSession: () -> Void = {}
    var onEditSession: () -> Void = {}
    var onCreateDiscussion: () -> Void = {}
    var onEditDiscussion: (String) -> Void = { _ in }
    var onDeleteDiscussion: (String) -> Void = { _ in }

    private var isOwner: Bool { userRole == .owner }
    private var isAdminOrAbove: Bool { userRole == .owner || userRole == .admin }

    var body: some View {
        ScrollView {
            if let session = sessionDetails {
                VStack(spacing: 12) {
                    // Session Book Card
                    VStack(alignment: .leading, spacing: 8) {
                        HStack(alignment: .top) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(session.book.title)
                                    .font(.headline)

                                Text(String(format: NSLocalizedString("label_by_author", comment: ""), session.book.author))
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)

                                HStack {
                                    Text("label_due_date")
                                        .fontWeight(.medium)
                                    Text(session.dueDate)
                                }
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                            }

                            if isOwner {
                                Spacer()
                                Button(action: onEditSession) {
                                    Image(systemName: "pencil")
                                        .foregroundColor(.secondary)
                                }
                            }
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()

                    Divider()

                    // Discussion Timeline
                    VStack(alignment: .leading, spacing: 0) {
                        Text("section_discussion_timeline")
                            .font(.headline)
                            .padding(.bottom, 8)

                        ForEach(Array(session.discussions.enumerated()), id: \.offset) { index, discussion in
                            DiscussionTimelineItem(
                                discussion: discussion,
                                isFirst: index == 0,
                                isLast: index == session.discussions.count - 1,
                                showAdminActions: isAdminOrAbove,
                                onEdit: { onEditDiscussion(discussion.id) },
                                onDelete: { onDeleteDiscussion(discussion.id) }
                            )
                        }

                        if isAdminOrAbove {
                            Button(action: onCreateDiscussion) {
                                Label("Add Discussion", systemImage: "plus")
                                    .font(.subheadline)
                                    .fontWeight(.medium)
                            }
                            .padding(.top, 8)
                        }
                    }
                    .padding()
                }
                .padding()
            } else {
                if isOwner {
                    VStack(spacing: 12) {
                        Text(String(localized: "empty_no_session_details"))
                            .font(.subheadline)
                            .foregroundColor(.secondary)

                        Button(action: onCreateSession) {
                            Label("Create Session", systemImage: "plus")
                        }
                        .buttonStyle(.bordered)
                    }
                    .padding()
                } else {
                    NoTabData(text: String(localized: "empty_no_session_details"))
                }
            }
        }
    }
}

// MARK: - Discussion Timeline Item
struct DiscussionTimelineItem: View {
    let discussion: Shared.DiscussionTimelineItemInfo
    let isFirst: Bool
    let isLast: Bool
    var showAdminActions: Bool = false
    var onEdit: () -> Void = {}
    var onDelete: () -> Void = {}

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // Timeline indicator
            VStack(spacing: 0) {
                if !isFirst {
                    Rectangle()
                        .fill(Color.gray.opacity(0.3))
                        .frame(width: 2, height: 40)
                } else {
                    Spacer().frame(height: 40)
                }

                ZStack {
                    Circle()
                        .fill(discussion.isPast || discussion.isNext
                              ? Color.brandOrange.opacity(discussion.isPast ? 0.75 : 1.0)
                              : Color.gray.opacity(0.3))
                        .frame(width: 24, height: 24)

                    if discussion.isPast {
                        Image.custom(.checkmark)
                            .font(.system(size: 8, weight: .bold))
                            .foregroundColor(Color(UIColor.systemBackground))
                    }
                }

                if !isLast {
                    Rectangle()
                        .fill(Color.gray.opacity(0.3))
                        .frame(width: 2, height: 60)
                } else {
                    Spacer().frame(height: 60)
                }
            }
            .frame(width: 32)

            // Discussion content
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 8) {
                    if discussion.isNext {
                        NextDiscussionCard(
                            title: discussion.title,
                            location: discussion.location,
                            formattedDate: discussion.date
                        )
                    } else {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(discussion.title)
                                .font(.body)
                                .fontWeight(.medium)
                                .opacity(discussion.isPast ? 0.5 : 1.0)

                            HStack(spacing: 2) {
                                Image.custom(.location)
                                    .font(.caption)
                                Text(discussion.location)
                                    .font(.subheadline)
                            }
                            .foregroundColor(.secondary)
                            .opacity(discussion.isPast ? 0.5 : 1.0)

                            Text(discussion.date)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                                .opacity(discussion.isPast ? 0.5 : 1.0)
                        }
                        .padding(.vertical, 12)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                if showAdminActions {
                    Menu {
                        Button("Edit") { onEdit() }
                        Button("Delete", role: .destructive) { onDelete() }
                    } label: {
                        Image(systemName: "ellipsis")
                            .foregroundColor(.secondary)
                            .padding(.vertical, 12)
                    }
                }
            }
        }
    }
}

#Preview {
    ActiveSessionTab(sessionDetails: nil)
}
