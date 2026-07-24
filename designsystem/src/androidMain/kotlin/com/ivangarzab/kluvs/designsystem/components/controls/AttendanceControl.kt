package com.ivangarzab.kluvs.designsystem.components.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.R
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.statusDanger
import com.ivangarzab.kluvs.designsystem.theme.statusDangerSubtle
import com.ivangarzab.kluvs.designsystem.theme.statusSuccess
import com.ivangarzab.kluvs.designsystem.theme.statusSuccessSubtle

/** Hollow tri-state RSVP option — decoupled from the app's `AttendanceStatus` domain enum. */
enum class AttendanceOption { YES, MAYBE, NO }

private val SEGMENTS = listOf(AttendanceOption.YES, AttendanceOption.MAYBE, AttendanceOption.NO)

/**
 * RSVP control for a single discussion — mirrors web's `AttendanceControl`.
 *
 * A 3-segment icon pill (yes, maybe, no) plus a summary line. Hollow — takes plain
 * counts/selection instead of the app's `AttendanceRoster`/`AttendanceStatus` domain types;
 * callers compute those from their own model before calling this.
 *
 * @param counts per-option response count, e.g. `mapOf(AttendanceOption.YES to 4)` — missing keys
 * render as 0.
 * @param selected the signed-in member's current RSVP, or null if they haven't responded.
 */
@Composable
fun AttendanceControl(
    counts: Map<AttendanceOption, Int>,
    selected: AttendanceOption?,
    disabled: Boolean,
    onSelect: (AttendanceOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, KluvsTheme.colors.divider, RoundedCornerShape(50))
                .alpha(if (disabled) 0.7f else 1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SEGMENTS.forEachIndexed { index, option ->
                AttendanceSegment(
                    option = option,
                    isSelected = selected == option,
                    disabled = disabled,
                    isFirst = index == 0,
                    onClick = { onSelect(option) }
                )
            }
        }
        Text(
            text = "${counts[AttendanceOption.YES] ?: 0} yes · ${counts[AttendanceOption.NO] ?: 0} no · ${counts[AttendanceOption.MAYBE] ?: 0} maybe",
            // Caption, not Eyebrow — plain metadata, matches design-system/docs/typography.md's
            // own "1 yes · 0 no · 0 maybe" example directly.
            style = KluvsTheme.typography.caption,
            color = KluvsTheme.colors.contentMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun AttendanceSegment(
    option: AttendanceOption,
    isSelected: Boolean,
    disabled: Boolean,
    isFirst: Boolean,
    onClick: () -> Unit
) {
    val (background, tint) = when {
        isSelected && option == AttendanceOption.YES -> statusSuccessSubtle to statusSuccess
        isSelected && option == AttendanceOption.NO -> statusDangerSubtle to statusDanger
        isSelected -> KluvsTheme.colors.cardAlt to KluvsTheme.colors.content
        else -> KluvsTheme.colors.card to KluvsTheme.colors.contentMuted
    }

    Row(
        modifier = Modifier
            .size(28.dp)
            .then(
                if (!isFirst) {
                    Modifier.border(
                        width = 1.dp,
                        color = KluvsTheme.colors.divider
                    )
                } else Modifier
            )
            .background(background)
            .clickable(enabled = !disabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (option) {
            AttendanceOption.YES -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "RSVP yes",
                tint = tint,
                modifier = Modifier.size(13.dp)
            )

            AttendanceOption.MAYBE -> Icon(
                painter = painterResource(R.drawable.ic_help),
                contentDescription = "RSVP maybe",
                tint = tint,
                modifier = Modifier.size(13.dp)
            )

            AttendanceOption.NO -> Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "RSVP no",
                tint = tint,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_AttendanceControl() = KluvsTheme {
    var selected by remember { mutableStateOf<AttendanceOption?>(AttendanceOption.YES) }
    AttendanceControl(
        counts = mapOf(AttendanceOption.YES to 1, AttendanceOption.MAYBE to 1),
        selected = selected,
        disabled = false,
        onSelect = { selected = it }
    )
}
