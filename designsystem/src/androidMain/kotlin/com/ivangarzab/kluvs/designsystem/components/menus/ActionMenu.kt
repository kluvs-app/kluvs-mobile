package com.ivangarzab.kluvs.designsystem.components.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.buttons.IconButton
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * One action in an [ActionMenu] — a plain, single-line, optionally-destructive label. Not a
 * general list-item type; real usage across the app is uniformly 1-3 plain-text actions
 * (Edit/Delete/Share, Change Role/Remove, Edit/End Session) with no icons or subtext. If a
 * richer menu (icons, descriptions, many items) is ever genuinely needed, that's a different
 * component — a bottom sheet action list — not a mode of this one. See design-system/docs/
 * navigation.md.
 */
data class ActionMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false,
)

/**
 * DS-styled overflow menu — a "..." trigger opening a small popover list of [items]. Extracted
 * from five near-identical hand-rolled `IconButton` + `DropdownMenu` pairs (Clubs, Members,
 * Sessions, Discussions, Me), same shape, same own-expanded-state pattern [Dropdown] already
 * uses.
 */
@Composable
fun ActionMenu(
    items: List<ActionMenuItem>,
    modifier: Modifier = Modifier,
    icon: IconType = IconType.MoreVert,
    contentDescription: String? = "More options",
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            type = icon,
            contentDescription = contentDescription,
            onClick = { expanded = true },
            tint = KluvsTheme.colors.contentMuted,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(8.dp),
            containerColor = KluvsTheme.colors.cardAlt,
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.label.uppercase(),
                            style = KluvsTheme.typography.eyebrow,
                            color = if (item.isDestructive) KluvsTheme.colors.danger else KluvsTheme.colors.content,
                        )
                    },
                    onClick = {
                        expanded = false
                        item.onClick()
                    },
                )
            }
        }
    }
}

/**
 * Static previews can't render [DropdownMenu]'s actual popup content (same Compose Preview
 * limitation as [com.ivangarzab.kluvs.designsystem.components.modals.ConfirmationDialog]/
 * [com.ivangarzab.kluvs.designsystem.components.modals.BottomSheet] — Popup-based composables
 * don't render statically) — this reconstructs the same visual shape directly, including the
 * real [DropdownMenu]'s own default width clamp (112dp min, 280dp max — Material 3's own menu
 * spec; [ActionMenu] doesn't override it). Use Android Studio's Interactive Mode, or a real
 * device/emulator, to preview [ActionMenu] itself.
 */
@Composable
private fun ActionMenuPreviewShape(items: List<ActionMenuItem>) {
    Column(
        modifier = Modifier
            .widthIn(min = 112.dp, max = 280.dp)
            .background(KluvsTheme.colors.cardAlt, RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp),
    ) {
        items.forEach { item ->
            Text(
                text = item.label.uppercase(),
                style = KluvsTheme.typography.eyebrow,
                color = if (item.isDestructive) KluvsTheme.colors.danger else KluvsTheme.colors.content,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {}
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_ActionMenu() = KluvsTheme {
    ActionMenuPreviewShape(
        items = listOf(
            ActionMenuItem(label = "Share", onClick = {}),
            ActionMenuItem(label = "Edit", onClick = {}),
            ActionMenuItem(label = "Delete", onClick = {}, isDestructive = true),
        ),
    )
}

/** Real two-word labels (Members tab) — checking Eyebrow's tracking doesn't feel cramped on
 * longer strings, not just the single-word Share/Edit/Delete case above. */
@PreviewLightDark
@Composable
private fun Preview_ActionMenu_LongerLabels() = KluvsTheme {
    ActionMenuPreviewShape(
        items = listOf(
            ActionMenuItem(label = "Change Role", onClick = {}),
            ActionMenuItem(label = "Remove Member", onClick = {}, isDestructive = true),
        ),
    )
}

/** A single very short label — real content alone would render narrower than this; the 112dp
 * floor is what actually gives it this width. */
@PreviewLightDark
@Composable
private fun Preview_ActionMenu_MinWidth() = KluvsTheme {
    ActionMenuPreviewShape(items = listOf(ActionMenuItem(label = "Edit", onClick = {})))
}

/** A deliberately long, hypothetical label (nothing this long exists in real usage today) —
 * checking the 280dp ceiling actually clamps and wraps rather than overflowing. */
@PreviewLightDark
@Composable
private fun Preview_ActionMenu_MaxWidth() = KluvsTheme {
    ActionMenuPreviewShape(
        items = listOf(
            ActionMenuItem(label = "Report Inappropriate Content", onClick = {}),
            ActionMenuItem(label = "Edit", onClick = {}),
        ),
    )
}
