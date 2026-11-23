// Posts an Xcode Cloud failure summary to the GitHub PR that triggered the build.
//
// Xcode Cloud only shows logs in App Store Connect. This mirrors the useful part of a failure
// (stage, failing tests, Gradle's "What went wrong") onto the PR, so it can be read with `gh`.
//
// The repo is public, so only failure details are posted, never raw log tails, and anything
// that looks like a secret is redacted.
//
// One comment per PR: it is updated in place on each failing build instead of piling up.
//
// Never fails the build. It exits 0 when it isn't a PR build, when GITHUB_PR_COMMENT_TOKEN
// isn't set (store it as a secret environment variable on the Xcode Cloud workflow), or on any error.
//
// Usage:
//   xcrun swift report_failure.swift --stage "<what failed>" [--gradle-log FILE] [--xcresult PATH] [--dry-run]
//
// --dry-run prints the comment instead of posting it (no token or network needed).

import Foundation

let repo = "kluvs-app/kluvs-mobile"
let marker = "<!-- xcode-cloud-failure-report -->"
let maxSectionChars = 6000

// MARK: - Redaction

let secretPatterns = [
    #"eyJ[\w-]+\.[\w-]+\.[\w-]+"#,              // JWTs (e.g. Supabase keys)
    #"\b(?:gh[pousr]_|github_pat_)\w{20,}"#,    // GitHub tokens
]
// key=value / key: value pairs whose name suggests a secret; keeps the name, hides the value.
let secretAssignment = #"(?i)\b(\w*(?:key|token|secret|password)\w*)(\s*[=:]\s*)\S+"#

func replacing(_ text: String, _ pattern: String, with template: String) -> String {
    guard let regex = try? NSRegularExpression(pattern: pattern) else { return text }
    let range = NSRange(text.startIndex..., in: text)
    return regex.stringByReplacingMatches(in: text, range: range, withTemplate: template)
}

func redact(_ text: String) -> String {
    var output = text
    for pattern in secretPatterns {
        output = replacing(output, pattern, with: "[redacted]")
    }
    return replacing(output, secretAssignment, with: "$1$2[redacted]")
}

func truncate(_ text: String) -> String {
    text.count <= maxSectionChars ? text : String(text.prefix(maxSectionChars)) + "\n… (truncated)"
}

// MARK: - Failure details

/// Failing test lines (plus the two lines after each), Kotlin compile errors, and Gradle's
/// "What went wrong" block.
func gradleFailures(logPath: String) -> String {
    guard let data = FileManager.default.contents(atPath: logPath) else { return "" }
    let lines = String(decoding: data, as: UTF8.self)
        .components(separatedBy: "\n")
        .map { $0.hasSuffix("\r") ? String($0.dropLast()) : $0 }

    var picked: [String] = []
    for (index, line) in lines.enumerated() {
        if line.hasSuffix(" FAILED") {
            picked += lines[index..<min(index + 3, lines.count)]
        } else if line.hasPrefix("e: ") {
            picked.append(line)
        }
    }

    var wentWrong: [String] = []
    var capturing = false
    for line in lines {
        if line.hasPrefix("* What went wrong:") {
            capturing = true
        } else if line.hasPrefix("* Try:") {
            capturing = false
        }
        if capturing { wentWrong.append(line) }
    }

    let all = picked + (picked.isEmpty || wentWrong.isEmpty ? [] : [""]) + wentWrong
    return all.joined(separator: "\n").trimmingCharacters(in: .whitespacesAndNewlines)
}

func xcresultFailures(bundlePath: String) -> String {
    let process = Process()
    process.executableURL = URL(fileURLWithPath: "/usr/bin/xcrun")
    process.arguments = ["xcresulttool", "get", "test-results", "summary", "--path", bundlePath]
    let pipe = Pipe()
    process.standardOutput = pipe
    process.standardError = FileHandle.nullDevice
    do {
        try process.run()
    } catch {
        return "(couldn't read the result bundle: \(error.localizedDescription))"
    }
    let data = pipe.fileHandleForReading.readDataToEndOfFile()
    process.waitUntilExit()

    guard process.terminationStatus == 0,
          let summary = (try? JSONSerialization.jsonObject(with: data)) as? [String: Any]
    else { return "(couldn't read the result bundle)" }

    let header = "\(summary["result"] as? String ?? "?"): "
        + "\(summary["passedTests"] as? Int ?? 0) passed, "
        + "\(summary["failedTests"] as? Int ?? 0) failed, "
        + "\(summary["skippedTests"] as? Int ?? 0) skipped"
    let rows = (summary["testFailures"] as? [[String: Any]] ?? []).map { failure in
        let target = failure["targetName"] as? String ?? "?"
        let test = failure["testName"] as? String ?? "?"
        let text = (failure["failureText"] as? String ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        return "- \(target) › \(test): \(text)"
    }
    return ([header] + rows).joined(separator: "\n")
}

// MARK: - GitHub

struct HTTPError: Error, CustomStringConvertible {
    let status: Int
    var description: String { "GitHub API returned HTTP \(status)" }
}

final class ResponseBox: @unchecked Sendable {
    var result: Result<(Data, Int), Error> = .failure(URLError(.unknown))
}

@discardableResult
func github(_ method: String, _ url: String, token: String, body: [String: Any]? = nil) throws -> Any? {
    var request = URLRequest(url: URL(string: url)!, timeoutInterval: 30)
    request.httpMethod = method
    request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
    request.setValue("application/vnd.github+json", forHTTPHeaderField: "Accept")
    request.setValue("2022-11-28", forHTTPHeaderField: "X-GitHub-Api-Version")
    if let body {
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    }

    let box = ResponseBox()
    let done = DispatchSemaphore(value: 0)
    URLSession.shared.dataTask(with: request) { data, response, error in
        if let error {
            box.result = .failure(error)
        } else {
            box.result = .success((data ?? Data(), (response as? HTTPURLResponse)?.statusCode ?? 0))
        }
        done.signal()
    }.resume()
    done.wait()

    let (data, status) = try box.result.get()
    guard (200..<300).contains(status) else { throw HTTPError(status: status) }
    return data.isEmpty ? nil : try JSONSerialization.jsonObject(with: data)
}

// MARK: - Main

func argument(_ name: String, in arguments: [String]) -> String? {
    guard let index = arguments.firstIndex(of: name), index + 1 < arguments.count else { return nil }
    return arguments[index + 1]
}

func run() throws {
    let arguments = Array(CommandLine.arguments.dropFirst())
    let env = ProcessInfo.processInfo.environment
    let stage = argument("--stage", in: arguments) ?? "unknown"
    let dryRun = arguments.contains("--dry-run")

    let prNumber = env["CI_PULL_REQUEST_NUMBER"] ?? ""
    let token = env["GITHUB_PR_COMMENT_TOKEN"] ?? ""
    if !dryRun && (prNumber.isEmpty || token.isEmpty) {
        print("report_failure: not a PR build or no GITHUB_PR_COMMENT_TOKEN; skipping")
        return
    }

    var details = ""
    if let logPath = argument("--gradle-log", in: arguments) {
        details = gradleFailures(logPath: logPath)
    } else if let bundlePath = argument("--xcresult", in: arguments) {
        details = xcresultFailures(bundlePath: bundlePath)
    }

    let commit = String((env["CI_COMMIT"] ?? "").prefix(7))
    let meta = [
        env["CI_WORKFLOW"],
        env["CI_BUILD_NUMBER"].map { "build \($0)" },
        commit.isEmpty ? nil : "commit \(commit)",
    ].compactMap { $0 }.joined(separator: " · ")
    let buildLink = env["CI_BUILD_URL"].map { "[Build in App Store Connect](\($0))" }
    let section = truncate(redact(details))

    let body = [
        marker,
        "### ❌ Xcode Cloud failure",
        "**Stage:** `\(stage)`",
        meta.isEmpty ? nil : meta,
        buildLink,
        "",
        "```",
        section.isEmpty ? "(no failure details found; check the build log)" : section,
        "```",
        "",
        "_Updated in place by `iosApp/ci_scripts/report_failure.swift` on each failing build._",
    ].compactMap { $0 }.joined(separator: "\n")

    if dryRun {
        print(body)
        return
    }

    let commentsURL = "https://api.github.com/repos/\(repo)/issues/\(prNumber)/comments"
    let comments = try github("GET", "\(commentsURL)?per_page=100", token: token) as? [[String: Any]] ?? []
    if let existing = comments.first(where: { ($0["body"] as? String ?? "").contains(marker) }),
       let existingURL = existing["url"] as? String {
        try github("PATCH", existingURL, token: token, body: ["body": body])
        print("report_failure: updated PR #\(prNumber) comment")
    } else {
        try github("POST", commentsURL, token: token, body: ["body": body])
        print("report_failure: commented on PR #\(prNumber)")
    }
}

do {
    try run()
} catch {
    print("report_failure: skipped (\(error))")
}
exit(0)
