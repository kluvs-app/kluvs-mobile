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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.theme.KluvsTheme
import kotlinx.datetime.LocalDateTime

/**
 * Bottom sheet for creating a new reading session.
 *
 * Collects book title, author, and an optional due date/time.
 * Full book search (via SearchBooksUseCase) is a planned follow-up; for now the user
 * enters book details manually.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionBottomSheet(
    onSave: (book: Book, dueDate: LocalDateTime?) -> Unit,
    onDismiss: () -> Unit,
) {
    var bookTitle by remember { mutableStateOf("") }
    var bookAuthor by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedHour by remember { mutableStateOf(19) }
    var selectedMinute by remember { mutableStateOf(0) }

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
                text = "Create Session",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = bookTitle,
                onValueChange = { bookTitle = it },
                label = { Text("Book Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = bookAuthor,
                onValueChange = { bookAuthor = it },
                label = { Text("Author") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Read-only field that opens the date picker on tap
            Box {
                OutlinedTextField(
                    value = dateDisplayText,
                    onValueChange = {},
                    label = { Text("Due Date (optional)") },
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

            // Time picker field, shown only after a date is picked
            if (selectedDateMillis != null) {
                Box {
                    OutlinedTextField(
                        value = timeDisplayText,
                        onValueChange = {},
                        label = { Text("Time (optional)") },
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

            Button(
                onClick = {
                    val book = Book(
                        id = "",
                        title = bookTitle.trim(),
                        author = bookAuthor.trim(),
                        isbn = null
                    )
                    val dueDate = selectedDateMillis?.let { millis ->
                        buildLocalDateTime(millis, selectedHour, selectedMinute)
                    }
                    onSave(book, dueDate)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = bookTitle.isNotBlank() && bookAuthor.isNotBlank()
            ) {
                Text(
                    text = "Create",
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
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
fun Preview_CreateSessionBottomSheet() = KluvsTheme {
    CreateSessionBottomSheet(
        onSave = { _, _ -> },
        onDismiss = {}
    )
}
