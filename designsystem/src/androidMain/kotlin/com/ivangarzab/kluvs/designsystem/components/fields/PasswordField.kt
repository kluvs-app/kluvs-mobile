package com.ivangarzab.kluvs.designsystem.components.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.buttons.IconButton
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Editable form-field primitive for password entry (design-system "Inputs", see
 * design-system/docs/inputs.md) — always single-line, masked via [PasswordVisualTransformation]
 * by default, with a trailing show/hide reveal toggle. This is a categorically different input
 * mode from [InputField] (masked keyboard input vs. plain text), matching the same split this
 * package already draws between [InputField] and [PickerField].
 *
 * Reveal state is local to each instance (starts masked, not persisted, not hoisted), so every
 * password field in a form toggles independently.
 *
 * @param error non-null shows a red border/label and this text below the field (no "Error:"
 * prefix added here — callers supply the full message, matching real usage).
 * @param helperText muted hint text below the field, distinct from [error] — ignored if
 * [error] is also set.
 */
@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var revealed by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        isError = error != null,
        supportingText = (error ?: helperText)?.let { { Text(it) } },
        singleLine = true,
        visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(
                type = if (revealed) IconType.VisibilityOff else IconType.Visibility,
                contentDescription = if (revealed) "Hide password" else "Show password",
                onClick = { revealed = !revealed },
                enabled = enabled,
                tint = KluvsTheme.colors.contentMuted,
            )
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
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
private fun Preview_PasswordField() = KluvsTheme {
    var password by remember { mutableStateOf("") }
    var badPassword by remember { mutableStateOf("short") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PasswordField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            helperText = "Minimum 8 characters.",
        )
        PasswordField(
            label = "Password",
            value = badPassword,
            onValueChange = { badPassword = it },
            error = "Must be at least 8 characters.",
        )
    }
}
