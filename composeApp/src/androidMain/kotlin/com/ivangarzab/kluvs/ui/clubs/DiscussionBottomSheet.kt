package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import kotlinx.datetime.LocalDateTime

/**
 * Bottom sheet for creating or editing a discussion.
 *
 * Used for both create (empty fields) and edit (pre-filled title/location) modes.
 * The date and time are selected via pickers rather than free-form text entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionBottomSheet(
    initialTitle: String = "",
    initialLocation: String = "",
    initialDate: LocalDateTime? = null,
    onSave: (title: String, location: String, date: LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialDateMillis = remember { initialDate?.let { localDateTimeToDateMillis(it) } }
    var title by remember { mutableStateOf(initialTitle) }
    var location by remember { mutableStateOf(initialLocation) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(initialDateMillis) }
    var selectedHour by remember { mutableStateOf(initialDate?.hour ?: 19) }
    var selectedMinute by remember { mutableStateOf(initialDate?.minute ?: 0) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val dateDisplayText = selectedDateMillis?.let { formatDateMillis(it) } ?: ""
    val timeDisplayText = selectedDateMillis?.let {
        "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}"
    } ?: ""

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
                text = "Discussion",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_location),
                        contentDescription = null
                    )
                }
            )

            // Read-only field that opens the date picker on tap
            Box {
                OutlinedTextField(
                    value = dateDisplayText,
                    onValueChange = {},
                    label = { Text("Date") },
                    placeholder = { Text("Select date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true
                )
                TextButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.matchParentSize()
                ) { }
            }

            // Read-only field that opens the time picker, shown only after a date is picked
            if (selectedDateMillis != null) {
                Box {
                    OutlinedTextField(
                        value = timeDisplayText,
                        onValueChange = {},
                        label = { Text("Time") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        singleLine = true
                    )
                    TextButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.matchParentSize()
                    ) { }
                }
            }

            val canSave = title.isNotBlank() && location.isNotBlank() && selectedDateMillis != null
            val hasChanges = title.trim() != initialTitle ||
                location.trim() != initialLocation ||
                selectedDateMillis != initialDateMillis ||
                selectedHour != (initialDate?.hour ?: 19) ||
                selectedMinute != (initialDate?.minute ?: 0)
            Button(
                onClick = {
                    val millis = selectedDateMillis ?: return@Button
                    val dateTime = buildLocalDateTime(millis, selectedHour, selectedMinute)
                    onSave(title.trim(), location.trim(), dateTime)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave && hasChanges
            ) {
                Text(
                    text = "Save",
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}

@PreviewLightDark
@Composable
fun Preview_DiscussionBottomSheet() = KluvsTheme {
    DiscussionBottomSheet(
        initialTitle = "Discussion title",
        initialLocation = "Discussion location",
        onSave = { _, _, _ -> },
        onDismiss = {}
    )
}
