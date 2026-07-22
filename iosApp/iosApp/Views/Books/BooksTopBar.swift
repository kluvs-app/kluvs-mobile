//
//  BooksTopBar.swift
//  iosApp
//
import SwiftUI

private let titleFadeDuration = 0.15
private let searchUnfurlDuration = 0.2

/// Single animated top bar for the Books tab — mirrors web's header: the title fades out in
/// place while a bordered search field scales in from the right (`origin-right` scale-x, not a
/// slide), landing where the search button used to be. Books owns its own top bar/navigation
/// rather than using the shared `MaterialTopBar`, matching the just-updated Android pattern.
struct BooksTopBar: View {
    let isSearchActive: Bool
    var isSearching: Bool = false
    @Binding var query: String
    let onSearchClick: () -> Void
    let onBackClick: () -> Void

    @Environment(\.safeAreaInsets) private var safeAreaInsets

    var body: some View {
        ZStack {
            // Title row — fades in place, no positional movement.
            HStack {
                Text(String(localized: "books"))
                    .font(.kluvsSectionHeading)
                    .foregroundColor(.primary)
                Spacer()
                Button(action: onSearchClick) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 20))
                        .foregroundColor(.primary)
                }
                .disabled(isSearchActive)
            }
            .padding(.horizontal, 16)
            .opacity(isSearchActive ? 0 : 1)
            .animation(.linear(duration: titleFadeDuration), value: isSearchActive)

            // Search row — unfurls from the right edge (scale-x from the search button's
            // position), landing over the same header area rather than sliding in.
            HStack(spacing: 8) {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18))
                        .foregroundColor(.primary)
                }
                SearchInputBox(query: $query, isSearching: isSearching, shouldFocus: isSearchActive)
            }
            .padding(.horizontal, 8)
            .scaleEffect(x: isSearchActive ? 1 : 0, y: 1, anchor: .trailing)
            .opacity(isSearchActive ? 1 : 0)
            .animation(.easeOut(duration: searchUnfurlDuration), value: isSearchActive)
        }
        .frame(height: 56)
        .padding(.top, safeAreaInsets.top)
        .background(Color.kluvsBackground)
    }
}

/// Outlined search field matching web's `.kluvs-input`-style box: input-bg fill, input-border
/// hairline that lights up copper (brand primary) while focused.
private struct SearchInputBox: View {
    @Binding var query: String
    let isSearching: Bool
    let shouldFocus: Bool

    @Environment(\.colorScheme) private var colorScheme
    @FocusState private var isFocused: Bool

    private var backgroundColor: Color { colorScheme == .dark ? .warmDarkCard : .lightCard }
    private var borderColor: Color {
        isFocused ? .brandOrange : (colorScheme == .dark ? .warmDarkCard2 : .lightDivider)
    }
    private var placeholderColor: Color { colorScheme == .dark ? .foregroundWarmPlaceholder : .foregroundLightPlaceholder }
    private var accentColor: Color { isFocused ? .brandOrange : .secondary }

    var body: some View {
        HStack(spacing: 8) {
            ZStack(alignment: .leading) {
                if query.isEmpty {
                    Text(String(localized: "search_books_hint"))
                        .font(.kluvsBody)
                        .foregroundColor(placeholderColor)
                }
                TextField("", text: $query)
                    .font(.kluvsBody)
                    .focused($isFocused)
                    .submitLabel(.search)
            }

            if isSearching {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: accentColor))
                    .scaleEffect(0.7)
            } else {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 15))
                    .foregroundColor(accentColor)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(backgroundColor)
        .clipShape(RoundedRectangle(cornerRadius: 8)) // radius.input
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(borderColor, lineWidth: 1)
        )
        .animation(.linear(duration: 0.15), value: isFocused)
        .onChange(of: shouldFocus) { _, active in
            isFocused = active
        }
    }
}

#Preview {
    VStack {
        BooksTopBar(isSearchActive: false, query: .constant(""), onSearchClick: {}, onBackClick: {})
        BooksTopBar(isSearchActive: true, isSearching: true, query: .constant("Klara"), onSearchClick: {}, onBackClick: {})
    }
}
