//
//  StackedCoverPlaceholder.swift
//  iosApp
//
import SwiftUI

private struct TiltedCover {
    let tiltDegrees: Double
    let offsetXFraction: CGFloat
}

// tilts per design-system/tokens.json component.empty-state.stacked-covers (lg): left, center, right
private let coverTilts = [
    TiltedCover(tiltDegrees: -7, offsetXFraction: -0.32),
    TiltedCover(tiltDegrees: 4, offsetXFraction: 0),
    TiltedCover(tiltDegrees: 10, offsetXFraction: 0.32)
]

/// "Nothing here" illustration for empty book lists/shelves: three tilted, diagonally-striped
/// placeholder covers fanned out. See design-system tokens component.empty-state.stacked-covers.
struct StackedCoverPlaceholder: View {
    @Environment(\.colorScheme) private var colorScheme

    private var stripeColor: Color { colorScheme == .dark ? .warmDarkCard2 : .lightDivider }

    var body: some View {
        Canvas { context, size in
            let coverSize = CGSize(width: 112, height: 160)
            let stripeWidth: CGFloat = 5
            let center = CGPoint(x: size.width / 2, y: size.height / 2)

            for cover in coverTilts {
                let topLeft = CGPoint(
                    x: center.x - coverSize.width / 2 + cover.offsetXFraction * size.width,
                    y: center.y - coverSize.height / 2
                )
                var rotated = context
                rotated.translateBy(x: center.x, y: center.y)
                rotated.rotate(by: .degrees(cover.tiltDegrees))
                rotated.translateBy(x: -center.x, y: -center.y)
                drawDiagonalStripedRect(in: &rotated, topLeft: topLeft, size: coverSize, color: stripeColor, stripeWidth: stripeWidth)
            }
        }
        .frame(width: 220, height: 200)
    }

    private func drawDiagonalStripedRect(in context: inout GraphicsContext, topLeft: CGPoint, size: CGSize, color: Color, stripeWidth: CGFloat) {
        let rect = CGRect(origin: topLeft, size: size)
        context.clip(to: Path(rect))

        let diagonal = size.width + size.height
        var x = -diagonal
        while x < diagonal {
            var line = Path()
            line.move(to: CGPoint(x: topLeft.x + x, y: topLeft.y - diagonal / 2))
            line.addLine(to: CGPoint(x: topLeft.x + x + diagonal, y: topLeft.y + diagonal / 2 + size.height))
            context.stroke(line, with: .color(color), lineWidth: stripeWidth)
            x += stripeWidth * 2
        }
    }
}

#Preview {
    StackedCoverPlaceholder()
}
