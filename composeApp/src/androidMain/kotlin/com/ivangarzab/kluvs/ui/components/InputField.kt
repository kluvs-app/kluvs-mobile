package com.ivangarzab.kluvs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.components.icons.Icon
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

@Composable
fun InputField(
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: IconType,
    iconDescription: String,
    supportingText: String,
    supportingTextColor: Color = Color.Gray,
    keyboardOptions: KeyboardOptions? = null,
    keyboardActions: KeyboardActions? = null
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label) },
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = keyboardOptions ?: KeyboardOptions(),
        keyboardActions = keyboardActions ?: KeyboardActions(),
        leadingIcon = {
            Icon(
                type = icon,
                contentDescription = iconDescription
            )
        },
        supportingText = {
            Text(
                text = supportingText,
                color = supportingTextColor
            )
        },
    )
}

@PreviewLightDark
@Composable
fun Preview_InputField() = KluvsTheme {
    InputField(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(8.dp),
        label = "Test input",
        value = "",
        onValueChange = { },
        icon = IconType.Clubs,
        iconDescription = "Test icon description",
        supportingText = "Some supporting text",
    )
}

@PreviewLightDark
@Composable
fun Preview_PasswordInputField() = KluvsTheme {
    InputField(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(8.dp),
        isPassword = true,
        label = "Test password input",
        value = "",
        onValueChange = { },
        icon = IconType.Clubs,
        iconDescription = "Test icon description",
        supportingText = "Some supporting text",
    )
}