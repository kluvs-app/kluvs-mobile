package com.ivangarzab.kluvs.designsystem.components.avatars

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.avatarStackOverflowBg

/** Minimal member shape [AvatarStack] needs — decoupled from any specific UI model. */
data class AvatarStackMember(
    val id: String,
    val name: String,
    val avatarUrl: String?,
)

/**
 * Overlapping row of avatars with a "+N" overflow chip. Mirrors web's `AvatarStack`:
 * first avatar on top, -8px overlap (at 24dp size), up to 3 shown, ring matching the
 * surrounding surface.
 */
@Composable
fun AvatarStack(
    members: List<AvatarStackMember>,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    max: Int = 3,
    ringColor: Color = KluvsTheme.colors.background,
) {
    val shown = members.take(max)
    val extra = members.size - shown.size
    val overlap = size / 3f // ~ -8px at 24dp per design-system spec

    Row(modifier = modifier) {
        shown.forEachIndexed { index, member ->
            Avatar(
                name = member.name,
                memberId = member.id,
                avatarUrl = member.avatarUrl,
                size = size,
                modifier = Modifier
                    .offset(x = -overlap * index)
                    .zIndex((shown.size - index).toFloat())
                    .border(width = 2.dp, color = ringColor, shape = CircleShape)
            )
        }
        if (extra > 0) {
            Box(
                modifier = Modifier
                    .offset(x = -overlap * shown.size)
                    .zIndex(0f)
                    .size(size)
                    .clip(CircleShape)
                    .background(color = avatarStackOverflowBg)
                    .border(width = 2.dp, color = ringColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$extra",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = (size.value * 0.4f).sp
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_AvatarStack() = KluvsTheme {
    Box(
        modifier = Modifier
            .background(color = KluvsTheme.colors.background)
    ) {
        AvatarStack(
            members = listOf(
                AvatarStackMember("1", "Ana Silva", null),
                AvatarStackMember("2", "Ben Choi", null),
                AvatarStackMember("3", "Cara Doyle", null),
                AvatarStackMember("4", "Dev Patel", null),
                AvatarStackMember("5", "Eve Kim", null),
            )
        )
    }
}
