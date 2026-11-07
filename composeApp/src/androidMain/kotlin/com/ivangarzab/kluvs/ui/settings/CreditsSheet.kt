package com.ivangarzab.kluvs.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.components.modals.BottomSheet
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.components.CreditText

/**
 * Third-party attribution for book and author data, as an informational bottom sheet (no footer —
 * dismissed by swipe or scrim tap). Mirrors iOS's credits sheet under Settings > About.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsSheet(onDismiss: () -> Unit) {
    BottomSheet(
        header = stringResource(R.string.credits_title),
        onDismiss = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CreditText(
                resId = R.string.credits_book_data,
                style = KluvsTheme.typography.body.medium,
                color = KluvsTheme.colors.content,
            )
            CreditText(
                resId = R.string.credits_author_data,
                style = KluvsTheme.typography.body.medium,
                color = KluvsTheme.colors.content,
            )
        }
    }
}
