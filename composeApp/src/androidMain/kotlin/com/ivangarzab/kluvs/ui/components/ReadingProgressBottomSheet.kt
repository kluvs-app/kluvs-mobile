package com.ivangarzab.kluvs.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.feature
import kotlin.math.roundToInt

/**
 * Bottom sheet for tracking/updating the signed-in member's reading progress.
 *
 * Shared component: used by the Clubs screen (session progress) and intended
 * for reuse by the Me screen. Mirrors the web app's ReadingProgressModal —
 * Page/Percent toggle, value input, auto "mark as finished" when the value
 * reaches the end of the book, and a manual finished switch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingProgressBottomSheet(
    bookTitle: String,
    pageCount: Int?,
    initialType: ProgressType = ProgressType.PAGE,
    initialCurrentPage: Int? = null,
    initialPercentComplete: Float? = null,
    initialMarkFinished: Boolean = false,
    onSave: (type: ProgressType, currentPage: Int?, percentComplete: Float?, markFinished: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var progressType by rememberSaveable { mutableStateOf(initialType) }
    var currentPageText by rememberSaveable {
        mutableStateOf(initialCurrentPage?.toString() ?: "")
    }
    var percentText by rememberSaveable {
        mutableStateOf(initialPercentComplete?.let { formatPercentInput(it) } ?: "")
    }
    var markFinished by rememberSaveable { mutableStateOf(initialMarkFinished) }
    // Tracks the last value that auto-toggled the switch, so a manual override
    // sticks until the value changes again (same semantics as the web modal)
    var lastAutoTriggerValue by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun autoToggleFinished(newValue: String) {
        if (newValue == lastAutoTriggerValue) return
        val atEnd = when {
            progressType == ProgressType.PAGE && pageCount != null && pageCount > 0 ->
                (newValue.toIntOrNull() ?: 0) >= pageCount
            progressType == ProgressType.PERCENT ->
                (newValue.toFloatOrNull() ?: 0f) >= 100f
            else -> return
        }
        if (atEnd != markFinished) {
            markFinished = atEnd
            lastAutoTriggerValue = newValue
        }
    }

    val previewPercent = if (progressType == ProgressType.PAGE && pageCount != null && pageCount > 0) {
        val page = currentPageText.toIntOrNull()
        page?.let { minOf(100, (it * 100f / pageCount).roundToInt()) }
    } else null

    val canSave = when (progressType) {
        ProgressType.PAGE -> currentPageText.toIntOrNull() != null
        ProgressType.PERCENT -> percentText.toFloatOrNull() != null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (initialCurrentPage != null || initialPercentComplete != null) {
                    "Update Progress"
                } else {
                    "Track Progress"
                },
                style = KluvsTheme.typography.title.medium
            )

            Text(
                text = bookTitle,
                // Title + feature (italic) — this is a book title, design-system's confirmed
                // pattern (was plain bodyMedium before; a real visual change, not just an
                // accessor rename, since this call site fits the model too cleanly to leave as-is).
                style = KluvsTheme.typography.title.medium.feature(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Track By toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrackByButton(
                    label = "Page",
                    selected = progressType == ProgressType.PAGE,
                    modifier = Modifier.weight(1f),
                    onClick = { progressType = ProgressType.PAGE }
                )
                TrackByButton(
                    label = "Percent",
                    selected = progressType == ProgressType.PERCENT,
                    modifier = Modifier.weight(1f),
                    onClick = { progressType = ProgressType.PERCENT }
                )
            }

            if (progressType == ProgressType.PAGE) {
                OutlinedTextField(
                    value = currentPageText,
                    onValueChange = { value ->
                        currentPageText = value.filter { it.isDigit() }
                        autoToggleFinished(currentPageText)
                    },
                    label = { Text("Current Page" + (pageCount?.let { " (of $it)" } ?: "")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = previewPercent?.let {
                        { Text("That's about $it% complete.") }
                    }
                )
            } else {
                OutlinedTextField(
                    value = percentText,
                    onValueChange = { value ->
                        percentText = value.filter { it.isDigit() || it == '.' }
                        autoToggleFinished(percentText)
                    },
                    label = { Text("Percent Complete") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("%") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mark as finished",
                    style = KluvsTheme.typography.body.medium
                )
                Switch(
                    checked = markFinished,
                    onCheckedChange = { markFinished = it }
                )
            }

            Button(
                onClick = {
                    val page = if (progressType == ProgressType.PAGE) currentPageText.toIntOrNull() else null
                    val percent = if (progressType == ProgressType.PERCENT) {
                        percentText.toFloatOrNull()?.coerceIn(0f, 100f)
                    } else null
                    onSave(progressType, page, percent, markFinished)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave
            ) {
                Text(
                    text = "Save Progress",
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

@Composable
private fun TrackByButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors()
        ) {
            Text(text = label, color = MaterialTheme.colorScheme.background)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(text = label)
        }
    }
}

private fun formatPercentInput(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()

@PreviewLightDark
@Composable
fun Preview_ReadingProgressBottomSheet() = KluvsTheme {
    ReadingProgressBottomSheet(
        bookTitle = "1984",
        pageCount = 328,
        initialCurrentPage = 42,
        onSave = { _, _, _, _ -> },
        onDismiss = {}
    )
}
