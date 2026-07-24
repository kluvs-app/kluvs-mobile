package com.ivangarzab.kluvs.designsystem.components.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Editable form-field primitive covering every real editable shape found across the app
 * (design-system "Inputs", see design-system/docs/inputs.md) — plain text, prefix/suffix-
 * decorated (e.g. "#" page number, "%" percentage), and multiline — as parameter combinations
 * on one component, not separate variant types. For a read-only field that opens a picker or
 * dialog instead of accepting keyboard input, see [PickerField].
 *
 * @param error non-null shows a red border/label and this text below the field (no "Error:"
 * prefix added here — callers supply the full message, matching real usage).
 * @param helperText muted hint text below the field, distinct from [error] — ignored if
 * [error] is also set.
 */
@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    error: String? = null,
    helperText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        prefix = prefix?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        isError = error != null,
        supportingText = (error ?: helperText)?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = KluvsTheme.colors.card,
            unfocusedContainerColor = KluvsTheme.colors.card,
            disabledContainerColor = KluvsTheme.colors.background,
            errorContainerColor = KluvsTheme.colors.card,
            focusedBorderColor = KluvsTheme.colors.accent,
            unfocusedBorderColor = KluvsTheme.colors.divider,
            focusedLabelColor = KluvsTheme.colors.accent,
            unfocusedLabelColor = KluvsTheme.colors.contentMuted,
            cursorColor = KluvsTheme.colors.accent,
        ),
    )
}

@PreviewLightDark
@Composable
private fun Preview_InputField() = KluvsTheme {
    var text by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InputField(
            label = "Email",
            value = text,
            onValueChange = { text = it },
            helperText = "We'll never share this.",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        InputField(label = "Page", value = "42", onValueChange = {}, prefix = "#", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        InputField(label = "Progress", value = "70", onValueChange = {}, suffix = "%", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        InputField(label = "Note", value = "", onValueChange = {}, singleLine = false, minLines = 3)
        InputField(label = "Email", value = "not-an-email", onValueChange = {}, error = "Enter a valid email address.")
        InputField(label = "Email", value = "disabled@kluvs.app", onValueChange = {}, enabled = false)
    }
}
