package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.clubs.presentation.DiscussionNoteInfo
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

private const val NOTE_MAX_LENGTH = 4000

/**
 * Bottom sheet for viewing, creating, editing, or deleting the signed-in
 * member's note on a discussion.
 *
 * A null [note] means it hasn't finished loading yet. A non-null [note] with
 * [DiscussionNoteInfo.noteId] null means no note exists yet, so the sheet
 * opens straight into an editable/create state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionNoteSheet(
    note: DiscussionNoteInfo?,
    onSave: (content: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var isEditing by remember(note?.noteId) { mutableStateOf(note?.noteId == null) }
    var content by remember(note?.content) { mutableStateOf(note?.content ?: "") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                text = "Note",
                style = MaterialTheme.typography.titleMedium
            )

            when {
                note == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                isEditing -> {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { if (it.length <= NOTE_MAX_LENGTH) content = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        maxLines = 10
                    )

                    val errorMessage = note.error
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (note.noteId != null) {
                            TextButton(onClick = {
                                content = note.content
                                isEditing = false
                            }) { Text("Cancel") }
                            Spacer(Modifier.width(8.dp))
                        }
                    }

                    val canSave = content.isNotBlank() && content.trim() != note.content.trim()
                    Button(
                        onClick = { onSave(content.trim()) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canSave && !note.isSaving
                    ) {
                        Text(
                            text = if (note.isSaving) "Saving…" else "Save",
                            color = MaterialTheme.colorScheme.background
                        )
                    }
                }

                else -> {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { isEditing = true }) { Text("Edit") }
                        TextButton(
                            onClick = { showDeleteConfirmation = true }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            title = "Delete Note",
            message = "Are you sure you want to delete this note?",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteConfirmation = false
                onDelete()
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@PreviewLightDark
@Composable
fun Preview_DiscussionNoteSheet() = KluvsTheme {
    DiscussionNoteSheet(
        note = DiscussionNoteInfo(noteId = "n1", content = "Bring snacks next time and discuss chapter 5."),
        onSave = {},
        onDelete = {},
        onDismiss = {}
    )
}
