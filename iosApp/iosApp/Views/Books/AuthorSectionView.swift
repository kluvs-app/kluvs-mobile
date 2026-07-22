//
//  AuthorSectionView.swift
//  iosApp
//
import SwiftUI
import Shared

/// "About the Author" body: photo + name row, then bio below full-width (mirrors web's
/// vertical stack). Shows a shimmer while loading, and is silently omitted entirely if
/// [author] is null once loading finishes — same graceful-degradation semantics as web's
/// `BooksPage.tsx`. The section eyebrow header and surrounding divider are owned by the caller.
struct AuthorSectionView: View {
    let isLoading: Bool
    let author: Shared.Author?

    var body: some View {
        if isLoading {
            AuthorSectionShimmer()
        } else if let author, author.name != nil || author.bio != nil {
            VStack(alignment: .leading, spacing: 12) {
                HStack(alignment: .center, spacing: 12) {
                    if let imageUrl = author.imageUrl, let url = URL(string: imageUrl) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let image):
                                image.resizable().aspectRatio(contentMode: .fill)
                            default:
                                Circle().fill(Color.secondary.opacity(0.2))
                            }
                        }
                        .frame(width: 48, height: 48)
                        .clipShape(Circle())
                    }
                    if let name = author.name {
                        Text(name)
                            .font(.ebGaramond(size: 18))
                            .foregroundColor(.primary)
                    }
                }
                if let bio = author.bio {
                    Text(bio)
                        .font(.kluvsBody)
                        .foregroundColor(.primary)
                }
            }
        }
    }
}

private struct AuthorSectionShimmer: View {
    @State private var animate = false

    private var shimmerColor: Color { Color.secondary.opacity(animate ? 0.3 : 0.15) }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Circle()
                    .fill(shimmerColor)
                    .frame(width: 48, height: 48)
                RoundedRectangle(cornerRadius: 4)
                    .fill(shimmerColor)
                    .frame(width: 120, height: 16)
            }
            RoundedRectangle(cornerRadius: 4)
                .fill(shimmerColor)
                .frame(height: 12)
            RoundedRectangle(cornerRadius: 4)
                .fill(shimmerColor)
                .frame(height: 12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.trailing, 60)
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 0.8).repeatForever(autoreverses: true)) {
                animate = true
            }
        }
    }
}

#Preview {
    AuthorSectionView(
        isLoading: false,
        author: Author(name: "J.R.R. Tolkien", imageUrl: nil, bio: "English writer and philologist, best known as the author of The Hobbit and The Lord of the Rings.")
    )
    .padding()
}
