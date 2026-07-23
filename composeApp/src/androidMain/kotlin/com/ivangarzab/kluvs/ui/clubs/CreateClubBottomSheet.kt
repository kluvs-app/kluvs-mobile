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
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Bottom sheet for creating a new club — matches web's `AddClubModal` scope on mobile
 * (name only; Discord server/channel selection needs the `discord-channels` endpoint,
 * which isn't built yet).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubBottomSheet(
    onCreate: (name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
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
                text = "New Club",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Club Name") },
                placeholder = { Text("e.g., Fantasy Book Club") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = { onCreate(name.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(
                    text = "Create Club",
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_CreateClubBottomSheet() = KluvsTheme {
    CreateClubBottomSheet(
        onCreate = {},
        onDismiss = {}
    )
}
