package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.clubs.presentation.ClubListItem
import com.ivangarzab.kluvs.theme.KluvsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubSelectorBottomSheet(
    clubs: List<ClubListItem>,
    selectedClubId: String?,
    onClubSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(clubs) { club ->
                val isSelected = club.id == selectedClubId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClubSelected(club.id) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = club.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_ClubSelectorBottomSheet() = KluvsTheme {
    ClubSelectorBottomSheet(
        clubs = listOf(
            ClubListItem(id = "1", name = "My Book Club"),
            ClubListItem(id = "2", name = "Sci-Fi Readers"),
            ClubListItem(id = "3", name = "Classic Literature")
        ),
        selectedClubId = "1",
        onClubSelected = {},
        onDismiss = {}
    )
}
