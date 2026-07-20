import SwiftUI

/// Book cover thumbnail with the design system's hexagon-grid fallback (`BookCoverPlaceholder`).
/// Owns its own fixed frame + clip so callers don't have to juggle `.aspectRatio`/`.frame`
/// ordering — a chained `.frame().aspectRatio(.fit)` on the container without an explicit
/// height and `.clipped()` let the fill-mode image overflow/distort in earlier versions.
struct BookCoverImage: View {
    let imageUrl: String?
    let width: CGFloat
    /// width / height
    var aspectRatio: CGFloat = 2.0 / 3.0
    var cornerRadius: CGFloat = 4

    private var height: CGFloat { width / aspectRatio }

    var body: some View {
        Group {
            if let imageUrl, let url = URL(string: imageUrl) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image.resizable().aspectRatio(contentMode: .fill)
                    default:
                        BookCoverPlaceholder()
                    }
                }
            } else {
                BookCoverPlaceholder()
            }
        }
        .frame(width: width, height: height)
        .clipped()
        .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
    }
}

#Preview {
    BookCoverImage(imageUrl: nil, width: 80)
}
