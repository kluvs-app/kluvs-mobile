//
//  BookDetailActionsView.swift
//  iosApp
//
import SwiftUI
import Shared

private let assignableShelfStatuses: [Shared.ShelfStatus] = [.currentlyReading, .read, .wantToRead, .notFinished]

/// Shelf status selector + like toggle for the book detail screen, styled to match web's
/// `LikePill`/`ShelfPill`: fully rounded outline pills, copper border when active, neutral
/// otherwise. Hidden entirely for unregistered books, mirroring [BookCard]'s guard.
struct BookDetailActionsView: View {
    let isRegistered: Bool
    let shelfStatus: Shared.ShelfStatus?
    let isLiked: Bool
    let isMutationInProgress: Bool
    let onShelfChange: (Shared.ShelfStatus?) -> Void
    let onToggleLike: () -> Void

    var body: some View {
        if isRegistered {
            HStack(spacing: 10) {
                LikeToggle(isLiked: isLiked, enabled: !isMutationInProgress, onTap: onToggleLike)
                ShelfPill(shelfStatus: shelfStatus, enabled: !isMutationInProgress, onShelfChange: onShelfChange)
            }
        }
    }
}

private struct LikeToggle: View {
    let isLiked: Bool
    let enabled: Bool
    let onTap: () -> Void

    private var tint: Color { isLiked ? .brandOrange : .secondary }
    private var borderColor: Color { isLiked ? .brandOrange : Color.secondary.opacity(0.4) }

    var body: some View {
        Button(action: onTap) {
            Image(systemName: isLiked ? "heart.fill" : "heart")
                .font(.system(size: 16))
                .foregroundColor(tint)
                .frame(width: 36, height: 36)
                .overlay(Circle().stroke(borderColor, lineWidth: 1))
        }
        .disabled(!enabled)
        .accessibilityLabel(String(localized: isLiked ? "unlike_book" : "like_book"))
    }
}

private struct ShelfPill: View {
    let shelfStatus: Shared.ShelfStatus?
    let enabled: Bool
    let onShelfChange: (Shared.ShelfStatus?) -> Void

    private var active: Bool { shelfStatus != nil }
    private var tint: Color { active ? .brandOrange : .secondary }
    private var borderColor: Color { active ? .brandOrange : Color.secondary.opacity(0.4) }

    var body: some View {
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
            HStack(spacing: 8) {
                Text(shelfStatus.map(shelfLabel) ?? String(localized: "shelf_add_to_shelf"))
                    .font(.kluvsButtonSecondary)
                    .foregroundColor(tint)
                Image(systemName: "chevron.down")
                    .font(.system(size: 11))
                    .foregroundColor(tint)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .overlay(Capsule().stroke(borderColor, lineWidth: 1))
        }
        .disabled(!enabled)
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
    BookDetailActionsView(
        isRegistered: true,
        shelfStatus: .currentlyReading,
        isLiked: true,
        isMutationInProgress: false,
        onShelfChange: { _ in },
        onToggleLike: {}
    )
    .padding()
}
