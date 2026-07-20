import SwiftUI

/// Minimal member shape `AvatarStack` needs — decoupled from any specific UI model.
struct AvatarStackMember: Identifiable {
    let id: String
    let name: String
    let avatarUrl: String?
}

/// Overlapping row of avatars with a "+N" overflow chip. Mirrors web's `AvatarStack` /
/// Android's `AvatarStack`: first avatar on top, ~1/3-size overlap, up to `max` shown.
struct AvatarStack: View {
    let members: [AvatarStackMember]
    var size: CGFloat = 24
    var max: Int = 3
    var ringColor: Color = Color(uiColor: .systemBackground)

    private var shown: [AvatarStackMember] { Array(members.prefix(max)) }
    private var extra: Int { members.count - shown.count }
    private var overlap: CGFloat { size / 3 }

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(shown.enumerated()), id: \.element.id) { index, member in
                Avatar(name: member.name, avatarUrl: member.avatarUrl, size: size, memberId: member.id)
                    .overlay(Circle().strokeBorder(ringColor, lineWidth: 2))
                    .zIndex(Double(shown.count - index))
                    .offset(x: -overlap * CGFloat(index))
            }
            if extra > 0 {
                ZStack {
                    Circle().fill(Color.avatarStackOverflowBg)
                    Text("+\(extra)")
                        .font(.plexSansMedium(size: size * 0.4))
                        .foregroundColor(.white)
                }
                .frame(width: size, height: size)
                .overlay(Circle().strokeBorder(ringColor, lineWidth: 2))
                .zIndex(0)
                .offset(x: -overlap * CGFloat(shown.count))
            }
        }
        .padding(.trailing, extra > 0 ? overlap * CGFloat(shown.count) : overlap * CGFloat(Swift.max(shown.count - 1, 0)))
    }
}

#Preview {
    AvatarStack(members: [
        AvatarStackMember(id: "1", name: "Ana Silva", avatarUrl: nil),
        AvatarStackMember(id: "2", name: "Ben Choi", avatarUrl: nil),
        AvatarStackMember(id: "3", name: "Cara Doyle", avatarUrl: nil),
        AvatarStackMember(id: "4", name: "Dev Patel", avatarUrl: nil),
    ])
    .padding()
}
