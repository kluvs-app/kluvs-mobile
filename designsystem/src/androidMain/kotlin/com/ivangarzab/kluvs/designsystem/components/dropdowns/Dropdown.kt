package com.ivangarzab.kluvs.designsystem.components.dropdowns

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.icons.Icon
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Pill-shaped value selector — a trigger showing the current selection (or [placeholder]
 * when none) that opens a popover list of [options] on tap. Distinct from the Pill family
 * (`TriggerPill`/`TogglePill`) and from `ToggleControl`: those render every option inline,
 * this renders one value and reveals the rest in an overlay. Extracted from a private,
 * single-caller `ShelfPill` in `BookDetailActions`.
 *
 * Owns its own open/closed state — callers don't track `expanded` themselves.
 *
 * @param clearLabel if non-null, shows a leading option using this label that calls
 * [onSelect] with `null` — e.g. "None" to unset the current selection. Omit to make a
 * selection mandatory once set.
 */
@Composable
fun <T> Dropdown(
    options: List<T>,
    selected: T?,
    onSelect: (T?) -> Unit,
    label: (T) -> String,
    placeholder: String,
    modifier: Modifier = Modifier,
    clearLabel: String? = null,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val tint = if (selected != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "DropdownChevronRotation")

    Row(
        modifier = modifier
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = { expanded = true },
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(if (enabled) 1f else 0.4f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = selected?.let(label) ?: placeholder,
            style = KluvsTheme.typography.label,
            color = tint,
        )
        Icon(
            type = IconType.ChevronDown,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp).rotate(chevronRotation),
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (clearLabel != null && selected != null) {
                DropdownMenuItem(
                    text = { Text(clearLabel) },
                    onClick = {
                        expanded = false
                        onSelect(null)
                    },
                )
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option)) },
                    trailingIcon = {
                        if (option == selected) {
                            Icon(type = IconType.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_Dropdown() = KluvsTheme {
    var selected by remember { mutableStateOf<String?>(null) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Dropdown(
            options = listOf("Currently Reading", "Read", "Want to Read"),
            selected = selected,
            onSelect = { selected = it },
            label = { it },
            placeholder = "Add to Shelf",
            clearLabel = "None",
        )
        Dropdown(
            options = listOf("Currently Reading", "Read", "Want to Read"),
            selected = "Read",
            onSelect = {},
            label = { it },
            placeholder = "Add to Shelf",
            enabled = false,
        )
    }
}
