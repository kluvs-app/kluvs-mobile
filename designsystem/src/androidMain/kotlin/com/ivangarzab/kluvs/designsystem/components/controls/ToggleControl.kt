package com.ivangarzab.kluvs.designsystem.components.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Filled two-or-more-way toggle (design-system "Segmented Control", filled/Track-By variant —
 * see `.kluvs-segmented` in colors_and_type.css, and design-system/docs/buttons.md), e.g.
 * Page/Percent. A single pill-shaped container with a hairline divider between segments (none
 * before the first); the selected segment fills solid primary with `onPrimary` text.
 *
 * Extracted from a private, single-caller implementation in `ReadingProgressBottomSheet` that
 * (incorrectly) rendered each option as two separate, gapped rounded buttons rather than one
 * contiguous pill — this is the corrected shape.
 */
@Composable
fun <T> ToggleControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .border(1.dp, KluvsTheme.colors.divider, RoundedCornerShape(50)),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (index != 0) {
                            Modifier.border(1.dp, KluvsTheme.colors.divider)
                        } else Modifier
                    )
                    .background(if (isSelected) KluvsTheme.colors.accent else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = label(option),
                    style = KluvsTheme.typography.label,
                    color = if (isSelected) KluvsTheme.colors.onAccent else KluvsTheme.colors.contentMuted,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_ToggleControl() = KluvsTheme {
    var selected by remember { mutableStateOf("Page") }
    ToggleControl(
        options = listOf("Page", "Percent"),
        selected = selected,
        onSelect = { selected = it },
        label = { it },
    )
}
