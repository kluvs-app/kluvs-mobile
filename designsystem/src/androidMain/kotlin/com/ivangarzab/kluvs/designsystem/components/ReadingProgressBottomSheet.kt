package com.ivangarzab.kluvs.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import com.ivangarzab.kluvs.designsystem.components.controls.ToggleControl
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.feature
import kotlin.math.roundToInt

/** Hollow tracking-mode option — decoupled from the app's `ProgressType` domain enum. */
enum class ProgressTrackingMode { PAGE, PERCENT }

/**
 * Bottom sheet for tracking/updating the signed-in member's reading progress.
 *
 * Shared component: used by the Clubs screen (session progress) and the Me screen (shelf rows).
 * Mirrors the web app's ReadingProgressModal — Page/Percent toggle, value input, auto "mark as
 * finished" when the value reaches the end of the book, and a manual finished switch.
 *
 * Hollow — takes [ProgressTrackingMode] instead of the app's `ProgressType`; [onSave] reports back
 * in the same hollow currency, so callers translate at the boundary.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingProgressBottomSheet(
    bookTitle: String,
    pageCount: Int?,
    initialType: ProgressTrackingMode = ProgressTrackingMode.PAGE,
    initialCurrentPage: Int? = null,
    initialPercentComplete: Float? = null,
    initialMarkFinished: Boolean = false,
    onSave: (type: ProgressTrackingMode, currentPage: Int?, percentComplete: Float?, markFinished: Boolean) -> Unit,
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
            progressType == ProgressTrackingMode.PAGE && pageCount != null && pageCount > 0 ->
                (newValue.toIntOrNull() ?: 0) >= pageCount
            progressType == ProgressTrackingMode.PERCENT ->
                (newValue.toFloatOrNull() ?: 0f) >= 100f
            else -> return
        }
        if (atEnd != markFinished) {
            markFinished = atEnd
            lastAutoTriggerValue = newValue
        }
    }

    val previewPercent = if (progressType == ProgressTrackingMode.PAGE && pageCount != null && pageCount > 0) {
        val page = currentPageText.toIntOrNull()
        page?.let { minOf(100, (it * 100f / pageCount).roundToInt()) }
    } else null

    val canSave = when (progressType) {
        ProgressTrackingMode.PAGE -> currentPageText.toIntOrNull() != null
        ProgressTrackingMode.PERCENT -> percentText.toFloatOrNull() != null
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
                // Title + feature (italic) — this is a book title, design-system's confirmed pattern.
                style = KluvsTheme.typography.title.medium.feature(),
                color = KluvsTheme.colors.contentMuted
            )

            // Track By toggle
            ToggleControl(
                options = listOf(ProgressTrackingMode.PAGE, ProgressTrackingMode.PERCENT),
                selected = progressType,
                onSelect = { progressType = it },
                label = { if (it == ProgressTrackingMode.PAGE) "Page" else "Percent" },
                modifier = Modifier.fillMaxWidth()
            )

            if (progressType == ProgressTrackingMode.PAGE) {
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
                    val page = if (progressType == ProgressTrackingMode.PAGE) currentPageText.toIntOrNull() else null
                    val percent = if (progressType == ProgressTrackingMode.PERCENT) {
                        percentText.toFloatOrNull()?.coerceIn(0f, 100f)
                    } else null
                    onSave(progressType, page, percent, markFinished)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave
            ) {
                Text(
                    text = "Save Progress",
                    color = KluvsTheme.colors.background
                )
            }
        }
    }
}

private fun formatPercentInput(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()

@PreviewLightDark
@Composable
private fun Preview_ReadingProgressBottomSheet() = KluvsTheme {
    ReadingProgressBottomSheet(
        bookTitle = "1984",
        pageCount = 328,
        initialCurrentPage = 42,
        onSave = { _, _, _, _ -> },
        onDismiss = {}
    )
}
