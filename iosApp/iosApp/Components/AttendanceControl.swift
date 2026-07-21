import SwiftUI
import Shared

/// RSVP control for a single discussion — mirrors web's `AttendanceControl` /
/// Android's `AttendanceControl`. A 3-segment icon pill (yes, maybe, no);
/// tapping the already-selected segment clears the RSVP (handled by the
/// ViewModel, which treats a re-select as a clear). Renders nothing until
/// `roster` is loaded.
struct AttendanceControl: View {
    let roster: Shared.AttendanceRoster?
    let disabled: Bool
    let onSetAttendance: (Shared.AttendanceStatus) -> Void

    private static let segments: [Shared.AttendanceStatus] = [.yes, .maybe, .no]

    var body: some View {
        if let roster {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 0) {
                    ForEach(Array(Self.segments.enumerated()), id: \.offset) { index, status in
                        AttendanceSegment(
                            status: status,
                            isSelected: roster.myStatus == status,
                            disabled: disabled,
                            isFirst: index == 0,
                            onTap: { onSetAttendance(status) }
                        )
                    }
                }
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .strokeBorder(Color(uiColor: .separator), lineWidth: 1)
                )
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .opacity(disabled ? 0.7 : 1.0)

                Text(countsLabel(for: roster))
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .monospacedDigit()
            }
        }
    }

    private func countsLabel(for roster: Shared.AttendanceRoster) -> String {
        let yes = roster.responses.filter { $0.status == .yes }.count
        let no = roster.responses.filter { $0.status == .no }.count
        let maybe = roster.responses.filter { $0.status == .maybe }.count
        return "\(yes) yes · \(no) no · \(maybe) maybe"
    }
}

private struct AttendanceSegment: View {
    let status: Shared.AttendanceStatus
    let isSelected: Bool
    let disabled: Bool
    let isFirst: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            icon
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(tint)
                .frame(width: 28, height: 28)
                .background(background)
                .overlay(alignment: .leading) {
                    if !isFirst {
                        Rectangle()
                            .fill(Color(uiColor: .separator))
                            .frame(width: 1)
                    }
                }
        }
        .disabled(disabled)
    }

    private var icon: Image {
        switch status {
        case .yes: Image(systemName: "checkmark")
        case .maybe: Image(systemName: "questionmark")
        case .no: Image(systemName: "xmark")
        default: Image(systemName: "questionmark")
        }
    }

    private var background: Color {
        guard isSelected else { return .clear }
        switch status {
        case .yes: return .statusSuccessSubtle
        case .no: return .statusDangerSubtle
        default: return Color(uiColor: .secondarySystemFill)
        }
    }

    private var tint: Color {
        guard isSelected else { return .secondary }
        switch status {
        case .yes: return .statusSuccess
        case .no: return .statusDanger
        default: return .primary
        }
    }
}

#Preview {
    AttendanceControl(
        roster: Shared.AttendanceRoster(
            responses: [
                Shared.AttendanceResponse(memberId: "0", name: "Ivan", status: .yes),
                Shared.AttendanceResponse(memberId: "1", name: "Sam", status: .maybe)
            ],
            myStatus: .yes,
            totalMembers: 6
        ),
        disabled: false,
        onSetAttendance: { _ in }
    )
    .padding()
}
