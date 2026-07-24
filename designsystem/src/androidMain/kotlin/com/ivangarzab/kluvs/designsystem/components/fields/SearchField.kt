package com.ivangarzab.kluvs.designsystem.components.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.buttons.IconButton
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Icon(type = IconType.Search, contentDescription = null)
            }
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    type = IconType.Close,
                    contentDescription = "Clear search",
                    onClick = { onValueChange("") },
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.background,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
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
