//
//  AuthorSectionView.swift
//  iosApp
//
import SwiftUI
import Shared
import DesignSystem

/// "About the Author" body: photo + name/description row, then bio below full-width (mirrors
/// web's vertical stack). The description is a one-line eyebrow under the name, as on web.
/// Shows a shimmer while loading, and is silently omitted entirely if [author] is null or has
/// no name, description or bio once loading finishes — same graceful-degradation semantics as web's
/// `BooksPage.tsx`. The section eyebrow header and surrounding divider are owned by the caller.
struct AuthorSectionView: View {
    let isLoading: Bool
    let author: Shared.Author?

    var body: some View {
        if isLoading {
            AuthorSectionShimmer()
        } else if let author, author.name != nil || author.description_ != nil || author.bio != nil {
            VStack(alignment: .leading, spacing: 12) {
                HStack(alignment: .center, spacing: 12) {
                    if let imageUrl = author.imageUrl, let url = URL(string: imageUrl) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let image):
                                image.resizable().aspectRatio(contentMode: .fill)
                            default:
                                Circle().fill(KluvsTheme.colors.cardAlt)
                            }
                        }
                        .frame(width: 48, height: 48)
                        .clipShape(Circle())
                    }
                    VStack(alignment: .leading, spacing: 4) {
                        if let name = author.name {
                            Text(name)
                                .kluvsStyle(KluvsTheme.typography.title.medium)
                                .foregroundColor(KluvsTheme.colors.content)
                        }
                        if let description = author.description_ {
                            Text(description.uppercased())
                                .kluvsStyle(KluvsTheme.typography.eyebrow)
                                .foregroundColor(KluvsTheme.colors.contentMuted)
                        }
                    }
                }
                if let bio = author.bio {
                    Text(bio)
                        .kluvsStyle(KluvsTheme.typography.body.medium)
                        .foregroundColor(KluvsTheme.colors.content)
                }
            }
        }
    }
}

private struct AuthorSectionShimmer: View {
    @State private var animate = false

    private var shimmerColor: Color { KluvsTheme.colors.contentMuted.opacity(animate ? 0.7 : 0.3) }

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
        author: Author(name: "J.R.R. Tolkien", description: "English writer", imageUrl: nil, bio: "English writer and philologist, best known as the author of The Hobbit and The Lord of the Rings.")
    )
    .padding()
}
