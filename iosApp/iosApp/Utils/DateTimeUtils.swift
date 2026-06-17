import Foundation

extension Date {
    /// Converts a Swift Date to an ISO-8601 string compatible with KMP's LocalDateTime.parse().
    /// Example output: "2025-01-15T19:00:00"
    func toIsoString() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        formatter.locale = Locale(identifier: "en_US_POSIX")
        return formatter.string(from: self)
    }
}

extension String {
    /// Converts an ISO-8601 string from KMP's LocalDateTime.toString() to a Swift Date.
    /// Falls back to the current date if the string cannot be parsed.
    func toSwiftDate() -> Date {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        formatter.locale = Locale(identifier: "en_US_POSIX")
        return formatter.date(from: self) ?? Date()
    }
}
