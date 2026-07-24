package com.ivangarzab.kluvs.designsystem.components.fields

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.icons.Icon
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Label-less filter-as-you-type field — e.g. filtering an already-visible list in a top app
 * bar. Distinct from [InputField]: no label, always shows a leading search icon (or a spinner
 * in its place while [isLoading]), and shows a trailing clear button whenever [value] isn't
 * empty. Distinct from the (separate, unbuilt) search-and-select combobox: this only filters
 * what's already on screen, it never triggers a network search or produces a "selected result"
 * state — see design-system/docs/inputs.md.
 *
 * Built on [BasicTextField] with hand-drawn border/background rather than wrapping
 * `OutlinedTextField` (as [InputField]/[PickerField] do) — M3's text field composables enforce
 * an internal ~56dp minimum height that can't be shrunk by squeezing the outer container, and
 * this field needs to be genuinely compact for use inside [com.ivangarzab.kluvs.designsystem.components.appbars.SearchTopAppBar]'s
 * collapsed single-row height.
 */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) KluvsTheme.colors.accent else KluvsTheme.colors.divider,
        animationSpec = tween(150),
        label = "SearchFieldBorderColor",
    )
    val accentColor = if (isFocused) KluvsTheme.colors.accent else KluvsTheme.colors.contentMuted

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(KluvsTheme.colors.card, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = accentColor)
        } else {
            Icon(type = IconType.Search, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = KluvsTheme.typography.body.medium,
                    color = KluvsTheme.colors.contentMuted,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true,
                textStyle = KluvsTheme.typography.body.medium.copy(color = KluvsTheme.colors.content),
                interactionSource = interactionSource,
                cursorBrush = SolidColor(KluvsTheme.colors.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
        }

        if (value.isNotEmpty()) {
            Icon(
                type = IconType.Close,
                contentDescription = "Clear search",
                tint = accentColor,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(enabled = enabled) { onValueChange("") },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_SearchField() = KluvsTheme {
    var query by remember { mutableStateOf("") }
    SearchField(value = query, onValueChange = { query = it }, placeholder = "Search books")
}

@PreviewLightDark
@Composable
private fun Preview_SearchField_WithText() = KluvsTheme {
    var query by remember { mutableStateOf("One Hundred Years") }
    SearchField(value = query, onValueChange = { query = it }, placeholder = "Search books")
}

@PreviewLightDark
@Composable
private fun Preview_SearchField_Loading() = KluvsTheme {
    var query by remember { mutableStateOf("Klara") }
    SearchField(value = query, onValueChange = { query = it }, placeholder = "Search books", isLoading = true)
}
