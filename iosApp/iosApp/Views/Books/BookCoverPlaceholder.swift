//
//  BookCoverPlaceholder.swift
//  iosApp
//
import SwiftUI

/// The design system's "no cover available" fallback: a tessellating hexagon
/// hive grid on the warmDarkBar/lightBar surface. See design-system/docs/book-cover.md.
///
/// Pointy-top hexagon tiling where `hexWidth` is the vertex-to-vertex column spacing
/// (matches the web token's 28px tile width at book-cover--md scale).
struct BookCoverPlaceholder: View {
    var hexWidth: CGFloat = 14

    @Environment(\.colorScheme) private var colorScheme

    private var backgroundColor: Color { colorScheme == .dark ? .warmDarkBar : .lightBar }
    private var strokeColor: Color { colorScheme == .dark ? .warmDarkCard2 : .lightDivider }

    var body: some View {
        Canvas { context, size in
            let radius = hexWidth / sqrt(3)
            let hexHeight = radius * 2
            let rowSpacing = hexHeight * 0.75

            var path = Path()
            var row = 0
            var y = -hexHeight
            while y < size.height + hexHeight {
                let xOffset: CGFloat = row % 2 == 1 ? hexWidth / 2 : 0
                var x = -hexWidth + xOffset
                while x < size.width + hexWidth {
                    addHexagon(to: &path, center: CGPoint(x: x, y: y), radius: radius)
                    x += hexWidth
                }
                y += rowSpacing
                row += 1
            }

            context.stroke(path, with: .color(strokeColor), lineWidth: 1.5)
        }
        .background(backgroundColor)
    }

    /// Appends a pointy-top hexagon outline centered at `center` with circumradius `radius`.
    private func addHexagon(to path: inout Path, center: CGPoint, radius: CGFloat) {
        for i in 0...5 {
            let angleDeg = -90.0 + 60.0 * Double(i)
            let angleRad = angleDeg * .pi / 180.0
            let point = CGPoint(
                x: center.x + radius * cos(angleRad),
                y: center.y + radius * sin(angleRad)
            )
            if i == 0 {
                path.move(to: point)
            } else {
                path.addLine(to: point)
            }
        }
        path.closeSubpath()
    }
}

#Preview {
    BookCoverPlaceholder()
        .frame(width: 120, height: 180)
}
