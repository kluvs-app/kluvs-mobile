//
//  ReadRibbon.swift
//  iosApp
//
import SwiftUI

/// Ribbon size, matching the book-cover sizes it's overlaid on (design-system/docs/book-cover.md).
enum ReadRibbonSize {
    case sm, md, lg

    var size: CGSize {
        switch self {
        case .sm: return CGSize(width: 12, height: 21)
        case .md: return CGSize(width: 16, height: 28)
        case .lg: return CGSize(width: 24, height: 42)
        }
    }
}

/// Corner "bookmark" marking a book as shelved through a Kluvs reading session
/// (as opposed to shelved manually). Mirrors the web `.kluvs-read-ribbon` notched-banner
/// shape (design-system/docs/book-cover.md, component.read-ribbon).
struct ReadRibbon: View {
    var size: ReadRibbonSize = .lg
    let contentDescription: String

    var body: some View {
        let dimensions = size.size
        Canvas { context, canvasSize in
            let w = canvasSize.width
            let h = canvasSize.height
            var path = Path()
            path.move(to: CGPoint(x: w * 0.18, y: 0))
            path.addLine(to: CGPoint(x: w * 0.82, y: 0))
            path.addLine(to: CGPoint(x: w * 0.82, y: h * 0.50))
            path.addLine(to: CGPoint(x: w, y: h * 0.75))
            path.addLine(to: CGPoint(x: w * 0.82, y: h))
            path.addLine(to: CGPoint(x: w * 0.18, y: h))
            path.addLine(to: CGPoint(x: 0, y: h * 0.75))
            path.addLine(to: CGPoint(x: w * 0.18, y: h * 0.50))
            path.closeSubpath()

            context.fill(path, with: .color(.brandOrange))
        }
        .frame(width: dimensions.width, height: dimensions.height)
        .accessibilityLabel(contentDescription)
    }
}

#Preview {
    ReadRibbon(contentDescription: "Read with Kluvs")
        .padding()
        .background(Color.warmDarkBase)
}
