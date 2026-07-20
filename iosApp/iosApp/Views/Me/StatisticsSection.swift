//
//  StatisticsSection.swift
//  iosApp
//
//  Created by Ivan Garza Bermea on 12/5/25.
//
import SwiftUI
import Shared

/// "Your Statistics" section: a 3-column stat strip (Clubs / Books / Since),
/// separated by hairline dividers. Mirrors web's ProfilePage stats row.
struct StatisticsSection: View {
    let statistics: Shared.UserStatistics
    let joinDate: String?

    var body: some View {
        HStack(spacing: 0) {
            StatColumn(
                value: statistics.clubsCount > 0 ? "\(statistics.clubsCount)" : String(localized: "label_not_available"),
                label: String(localized: "stat_number_of_clubs")
            )
            statDivider
            StatColumn(
                value: statistics.booksRead > 0 ? "\(statistics.booksRead)" : String(localized: "label_not_available"),
                label: String(localized: "stat_books_read")
            )
            statDivider
            StatColumn(
                value: (joinDate?.isEmpty == false ? joinDate! : String(localized: "label_not_available")),
                label: String(localized: "stat_since")
            )
        }
    }

    private var statDivider: some View {
        Rectangle()
            .fill(Color.secondary.opacity(0.3))
            .frame(width: 1, height: 40)
    }
}

private struct StatColumn: View {
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 2) {
            Text(value)
                .font(.kluvsCardHeading)
            Text(label.uppercased())
                .font(.kluvsEyebrow)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
}

#Preview {
    StatisticsSection(
        statistics: Shared.UserStatistics(clubsCount: 3, booksRead: 3),
        joinDate: "2025"
    )
}
