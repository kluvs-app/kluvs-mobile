import SwiftUI
import Shared

struct ActiveSessionTab: View {
    let sessionDetails: Shared.ActiveSessionDetails?
    var userRole: Shared.Role? = nil
    var onCreateSession: () -> Void = {}
    var onCreateDiscussion: () -> Void = {}
    var onEditDiscussion: (String) -> Void = { _ in }
    var onDeleteDiscussion: (String) -> Void = { _ in }
    var onOpenNote: (String) -> Void = { _ in }
    var discussionRosters: [String: Shared.AttendanceRoster] = [:]
    var onLoadAttendanceRoster: (String) -> Void = { _ in }
    var onSetAttendance: (String, Shared.AttendanceStatus) -> Void = { _, _ in }

    private var isOwner: Bool { userRole == .owner }
    private var isAdminOrAbove: Bool { userRole == .owner || userRole == .admin }

    var body: some View {
        ScrollView {
            if let session = sessionDetails {
                VStack(alignment: .leading, spacing: 0) {
                    HStack {
                        if !session.discussions.isEmpty {
                            Text("\(session.discussions.count) scheduled")
                                .font(.ebGaramondItalic(size: 16))
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        if isAdminOrAbove {
                            GhostButton(text: "+ Add", onClick: onCreateDiscussion)
                        }
                    }
                    .padding(.bottom, 12)

                    if session.discussions.isEmpty {
                        Text("No discussions scheduled yet.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .padding(.vertical, 12)
                    } else {
                        ForEach(Array(session.discussions.enumerated()), id: \.offset) { index, discussion in
                            DiscussionTimelineItem(
                                discussion: discussion,
                                isFirst: index == 0,
                                isLast: index == session.discussions.count - 1,
                                showAdminActions: isAdminOrAbove,
                                onEdit: { onEditDiscussion(discussion.id) },
                                onDelete: { onDeleteDiscussion(discussion.id) },
                                onOpenNote: { onOpenNote(discussion.id) },
                                attendanceRoster: discussionRosters[discussion.id],
                                onLoadRoster: { onLoadAttendanceRoster(discussion.id) },
                                onSetAttendance: { status in onSetAttendance(discussion.id, status) }
                            )
                        }
                    }
                }
                .padding(16)
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
    var onOpenNote: () -> Void = {}
    var attendanceRoster: Shared.AttendanceRoster? = nil
    var onLoadRoster: () -> Void = {}
    var onSetAttendance: (Shared.AttendanceStatus) -> Void = { _ in }

    // A dot/line is "lit" (copper) once its discussion is past or is the current
    // next one — this is what makes the rail read as a continuous copper thread
    // through completed items.
    private var isLit: Bool { discussion.isPast || discussion.isNext }
    private var litLineColor: Color { Color.brandOrange.opacity(0.4) }
    private var neutralLineColor: Color { Color.gray.opacity(0.3) }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // Rail: stretches to the row's actual height (whatever the content
            // column ends up needing, attendance pill included) so the connecting
            // line never falls short.
            VStack(spacing: 0) {
                // Fixed offset down to the dot — lit if this discussion itself is
                // past/next (the line leading in from a completed/current dot above).
                if !isFirst {
                    Rectangle()
                        .fill(isLit ? litLineColor : neutralLineColor)
                        .frame(width: 2, height: 38)
                } else {
                    Spacer().frame(height: 38)
                }

                dot

                // Fills the rest of the row's height, down to the next dot — lit
                // only if this discussion is past; the line out of "next" stays neutral.
                Rectangle()
                    .fill(isLast ? Color.clear : (discussion.isPast ? litLineColor : neutralLineColor))
                    .frame(width: 2)
                    .frame(maxHeight: .infinity)
            }
            .frame(width: 32)
            .frame(maxHeight: .infinity)

            // Discussion content
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    if discussion.isNext {
                        Text("UP NEXT")
                            .font(.plexSansMedium(size: 11))
                            .foregroundColor(.brandOrange)
                    }
                    Text(discussion.title)
                        .font(discussion.isNext ? .ebGaramondMediumItalic(size: 22) : .body)
                        .fontWeight(discussion.isNext ? .regular : .medium)
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

                    AttendanceControl(
                        roster: attendanceRoster,
                        disabled: discussion.isPast,
                        onSetAttendance: onSetAttendance
                    )
                    .padding(.top, 4)
                }
                .padding(.vertical, 12)
                .frame(maxWidth: .infinity, alignment: .leading)

                HStack(spacing: 0) {
                    Button(action: onOpenNote) {
                        Image(systemName: "pencil")
                            .foregroundColor(.secondary)
                            .padding(.vertical, 12)
                    }

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
        .opacity(discussion.isPast ? 0.5 : 1.0)
        .task(id: discussion.id) { onLoadRoster() }
    }

    @ViewBuilder
    private var dot: some View {
        ZStack {
            if discussion.isNext {
                Circle()
                    .fill(Color.brandOrange.opacity(0.10))
                    .frame(width: 34, height: 34)
            }
            ZStack {
                Circle()
                    .fill(dotFillColor)
                    .frame(width: discussion.isNext ? 24 : 16, height: discussion.isNext ? 24 : 16)
                if !discussion.isPast && !discussion.isNext {
                    Circle()
                        .strokeBorder(Color(uiColor: .separator), lineWidth: 1)
                        .frame(width: 16, height: 16)
                }
                if discussion.isPast {
                    Image.custom(.checkmark)
                        .font(.system(size: 8, weight: .bold))
                        .foregroundColor(Color(UIColor.systemBackground))
                }
            }
        }
    }

    private var dotFillColor: Color {
        if discussion.isPast { return .avatarStackOverflowBg }
        if discussion.isNext { return .brandOrange }
        return .clear
    }
}

#Preview {
    ActiveSessionTab(sessionDetails: nil)
}
