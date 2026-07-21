package com.ivangarzab.kluvs.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.model.AttendanceResponse
import com.ivangarzab.kluvs.model.AttendanceRoster
import com.ivangarzab.kluvs.model.AttendanceStatus
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.theme.statusDanger
import com.ivangarzab.kluvs.theme.statusDangerSubtle
import com.ivangarzab.kluvs.theme.statusSuccess
import com.ivangarzab.kluvs.theme.statusSuccessSubtle

private val SEGMENTS = listOf(AttendanceStatus.YES, AttendanceStatus.MAYBE, AttendanceStatus.NO)

/**
 * RSVP control for a single discussion — mirrors web's `AttendanceControl`.
 *
 * A 3-segment icon pill (yes, maybe, no); tapping the already-selected segment
 * clears the RSVP. Renders nothing until [roster] is loaded.
 */
@Composable
fun AttendanceControl(
    roster: AttendanceRoster?,
    disabled: Boolean,
    onSetAttendance: (AttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    if (roster == null) return

    val counts = SEGMENTS.associateWith { status -> roster.responses.count { it.status == status } }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                .alpha(if (disabled) 0.7f else 1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SEGMENTS.forEachIndexed { index, status ->
                AttendanceSegment(
                    status = status,
                    isSelected = roster.myStatus == status,
                    disabled = disabled,
                    isFirst = index == 0,
                    onClick = { onSetAttendance(status) }
                )
            }
        }
        Text(
            text = "${counts[AttendanceStatus.YES]} yes · ${counts[AttendanceStatus.NO]} no · ${counts[AttendanceStatus.MAYBE]} maybe",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun AttendanceSegment(
    status: AttendanceStatus,
    isSelected: Boolean,
    disabled: Boolean,
    isFirst: Boolean,
    onClick: () -> Unit
) {
    val (background, tint) = when {
        isSelected && status == AttendanceStatus.YES -> statusSuccessSubtle to statusSuccess
        isSelected && status == AttendanceStatus.NO -> statusDangerSubtle to statusDanger
        isSelected -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .size(28.dp)
            .then(
                if (!isFirst) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                } else Modifier
            )
            .background(background)
            .clickable(enabled = !disabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            AttendanceStatus.YES -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "RSVP yes",
                tint = tint,
                modifier = Modifier.size(13.dp)
            )

            AttendanceStatus.MAYBE -> Icon(
                painter = painterResource(R.drawable.ic_help),
                contentDescription = "RSVP maybe",
                tint = tint,
                modifier = Modifier.size(13.dp)
            )

            AttendanceStatus.NO -> Icon(
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
    AttendanceControl(
        roster = AttendanceRoster(
            responses = listOf(
                AttendanceResponse(memberId = "0", name = "Ivan", status = AttendanceStatus.YES),
                AttendanceResponse(memberId = "1", name = "Sam", status = AttendanceStatus.MAYBE)
            ),
            myStatus = AttendanceStatus.YES,
            totalMembers = 6
        ),
        disabled = false,
        onSetAttendance = {}
    )
}
