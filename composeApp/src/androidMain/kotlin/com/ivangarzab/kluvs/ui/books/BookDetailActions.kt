package com.ivangarzab.kluvs.ui.books

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.components.IconType
import com.ivangarzab.kluvs.designsystem.components.Icon
import com.ivangarzab.kluvs.model.ShelfStatus

/**
 * Shelf status selector + like toggle for the book detail screen, styled to match web's
 * `LikePill`/`ShelfPill`: fully rounded outline pills, copper (`colorScheme.primary`) border
 * when active, neutral otherwise. Hidden entirely for unregistered books (fresh from search,
 * or a "more by this author" entry), which can't be shelved/liked until registered — mirrors
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LikeToggle(
            isLiked = isLiked,
            enabled = !isMutationInProgress,
            onClick = onToggleLike
        )

        Box {
            ShelfPill(
                shelfStatus = shelfStatus,
                expanded = showShelfMenu,
                enabled = !isMutationInProgress,
                onClick = { showShelfMenu = true }
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
    }
}

@Composable
private fun LikeToggle(
    isLiked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .size(36.dp)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            type = if (isLiked) IconType.Favorite else IconType.FavoriteOutline,
            contentDescription = stringResource(if (isLiked) R.string.unlike_book else R.string.like_book),
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ShelfPill(
    shelfStatus: ShelfStatus?,
    expanded: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val active = shelfStatus != null
    val tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "ShelfChevronRotation")

    Row(
        modifier = Modifier
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = shelfStatus?.let { shelfActionLabel(it) } ?: stringResource(R.string.shelf_add_to_shelf),
            style = MaterialTheme.typography.labelLarge,
            color = tint
        )
        Icon(
            type = IconType.ChevronDown,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp).rotate(chevronRotation)
        )
    }
}

@Composable
private fun SelectedCheck() {
    Icon(
        type = IconType.Check,
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
