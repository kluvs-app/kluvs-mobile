package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.theme.KluvsTheme

/**
 * Bottom sheet for editing the club name.
 *
 * Pre-fills the text field with the current name.
 * Save is disabled when the field is blank.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClubBottomSheet(
    currentName: String,
    onSave: (newName: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Edit Club Name",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Club Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = { onSave(name.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && name.trim() != currentName
            ) {
                Text(
                    text = "Save",
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_EditClubBottomSheet() = KluvsTheme {
    EditClubBottomSheet(
        currentName = "My Book Club",
        onSave = {},
        onDismiss = {}
    )
}
