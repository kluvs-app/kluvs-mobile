@file:OptIn(ExperimentalMaterial3Api::class)

package com.ivangarzab.kluvs.designsystem.components.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.buttons.PrimaryButton
import com.ivangarzab.kluvs.designsystem.components.buttons.TextButton
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Three-zone modal shell — design-system "Modal" (see design-system/docs/modal.md): a header
 * with an eyebrow label, free-form body content, and an optional footer. Reserved for edit/
 * create forms and multi-field flows — confirm-only actions belong in [ConfirmationDialog]
 * instead.
 *
 * @param isDestructiveHeader tints the header label danger-red — for a sheet whose primary
 * content is itself a destructive action, not for the common case (leave false; a destructive
 * *secondary* action inside an edit sheet uses [DangerZoneBox] within [content] instead).
 * @param footer typically a [BottomSheetFooter] call — left as a free slot so a sheet with no
 * footer (e.g. a single scrollable list) isn't forced to have one.
 */
@Composable
fun BottomSheet(
    header: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructiveHeader: Boolean = false,
    sheetState: SheetState = rememberModalBottomSheetState(),
    footer: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val accentColor = if (isDestructiveHeader) KluvsTheme.colors.danger else KluvsTheme.colors.accent
    val containerColor = KluvsTheme.colors.bar

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = containerColor,
    ) {
        Column {
            Text(
                text = header.uppercase(),
                style = KluvsTheme.typography.eyebrow,
                color = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp),
            )
            HorizontalDivider(color = KluvsTheme.colors.divider)

            Column(modifier = Modifier.padding(24.dp)) {
                content()
            }

            if (footer != null) {
                HorizontalDivider(color = KluvsTheme.colors.divider)
                footer()
            }
        }
    }
}

/** Standard Cancel/Action row for [BottomSheet]'s footer — Cancel always leading, the primary
 * action always trailing, per design-system/docs/modal.md's Footer spec. */
@Composable
fun BottomSheetFooter(
    actionLabel: String,
    onAction: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    cancelLabel: String = "Cancel",
    actionEnabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(text = cancelLabel, onClick = onCancel)
        PrimaryButton(text = actionLabel, onClick = onAction, enabled = actionEnabled)
    }
}

/**
 * Static previews can't render [ModalBottomSheet]'s actual window content (same Compose Preview
 * limitation as [ConfirmationDialog]) — this reconstructs the same visual shape directly,
 * including the container background [BottomSheet] itself applies via `containerColor`. Use
 * Android Studio's Interactive Mode, or a real device/emulator, to preview [BottomSheet] itself.
 */
@PreviewLightDark
@Composable
private fun Preview_BottomSheet() = KluvsTheme {
    val containerColor = KluvsTheme.colors.bar
    Column(
        modifier = Modifier.background(containerColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
    ) {
        Text(
            text = "EDIT CLUB",
            style = KluvsTheme.typography.eyebrow,
            color = KluvsTheme.colors.accent,
            modifier = Modifier.padding(24.dp),
        )
        HorizontalDivider(color = KluvsTheme.colors.divider)
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "{form fields would go here}", style = KluvsTheme.typography.body.medium)
        }
        HorizontalDivider(color = KluvsTheme.colors.divider)
        BottomSheetFooter(actionLabel = "Save", onAction = {}, onCancel = {})
    }
}
