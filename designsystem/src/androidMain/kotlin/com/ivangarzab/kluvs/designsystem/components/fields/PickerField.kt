package com.ivangarzab.kluvs.designsystem.components.fields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Read-only field that opens a picker or dialog on tap instead of accepting keyboard input —
 * e.g. a date/time chooser. Hollow: [onClick] is entirely the caller's responsibility (launch
 * a `DatePickerDialog`, a bottom sheet, whatever), and [value] is whatever already-formatted
 * string the caller wants displayed — this component has no idea what kind of picker it's
 * fronting. Shares [InputField]'s visual chrome (radius, border, label colors) but drops the
 * raised input background in favor of the surrounding surface, so it reads as inert rather
 * than editable.
 */
@Composable
fun PickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = true,
            label = { Text(label) },
            isError = error != null,
            supportingText = (error ?: helperText)?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled, onClick = onClick),
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview_PickerField() = KluvsTheme {
    var date by remember { mutableStateOf("Jan 15, 2026") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PickerField(label = "Date", value = date, onClick = { date = "Feb 2, 2026" })
        PickerField(label = "Date", value = "Jan 15, 2026", onClick = {}, enabled = false)
        PickerField(label = "Date", value = "", onClick = {}, error = "Pick a date.")
    }
}
