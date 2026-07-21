package com.ivangarzab.kluvs.ui.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.model.ShelfStatus

/**
 * Shelf status selector (an [AssistChip] with a dropdown-affordance chevron) + like toggle
 * for the book detail screen. Hidden entirely for unregistered books (fresh from search, or a
 * "more by this author" entry), which can't be shelved/liked until registered — mirrors
 * [BookCard]'s guard.
 */
@Composable
fun BookDetailActions(
    modifier: Modifier = Modifier,
    isRegistered: Boolean,
    shelfStatus: ShelfStatus?,
    isLiked: Boolean,
    isMutationInProgress: Boolean,
    onShelfChange: (ShelfStatus?) -> Unit,
    onToggleLike: () -> Unit
) {
    if (!isRegistered) return

    var showShelfMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box {
            AssistChip(
                enabled = !isMutationInProgress,
                onClick = { showShelfMenu = true },
                label = { Text(shelfStatus?.let { shelfActionLabel(it) } ?: stringResource(R.string.shelf_none)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                }
            )
            DropdownMenu(
                expanded = showShelfMenu,
                onDismissRequest = { showShelfMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.shelf_none)) },
                    trailingIcon = { if (shelfStatus == null) SelectedCheck() },
                    onClick = {
                        showShelfMenu = false
                        onShelfChange(null)
                    }
                )
                ShelfStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(shelfActionLabel(status)) },
                        trailingIcon = { if (shelfStatus == status) SelectedCheck() },
                        onClick = {
                            showShelfMenu = false
                            onShelfChange(status)
                        }
                    )
                }
            }
        }

        IconButton(
            enabled = !isMutationInProgress,
            onClick = onToggleLike
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(if (isLiked) R.string.unlike_book else R.string.like_book),
                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectedCheck() {
    Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun shelfActionLabel(status: ShelfStatus): String = when (status) {
    ShelfStatus.CURRENTLY_READING -> stringResource(R.string.shelf_currently_reading)
    ShelfStatus.READ -> stringResource(R.string.shelf_read)
    ShelfStatus.WANT_TO_READ -> stringResource(R.string.shelf_want_to_read)
    ShelfStatus.NOT_FINISHED -> stringResource(R.string.shelf_not_finished)
}
