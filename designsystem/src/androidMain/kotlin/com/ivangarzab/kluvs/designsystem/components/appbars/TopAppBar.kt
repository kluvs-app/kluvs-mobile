package com.ivangarzab.kluvs.designsystem.components.appbars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.buttons.IconButton
import com.ivangarzab.kluvs.designsystem.components.buttons.PrimaryButton
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/** Single row's height, shared with [SearchTopAppBar] so the collapse-to-search animation
 * targets an exact, known value rather than measuring. */
internal val TopAppBarRowHeight = 56.dp

/**
 * Editorial page header — an eyebrow-style [header] label (e.g. "Profile", "Club", "Library")
 * always shown in the top row alongside the optional back button and [action], plus an
 * optional big serif [title] (the actual name/value — a profile name, a club name) in a second
 * row underneath. Two modalities, not two components: pass [title] for the full two-row
 * version; omit it and the bar is just the single header row. See design-system/docs/
 * navigation.md.
 *
 * For the animated collapse-into-search variant of this same bar, see [SearchTopAppBar].
 */
@Composable
fun TopAppBar(
    header: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    action: @Composable () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalContentColor provides KluvsTheme.colors.content) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TopAppBarRowHeight)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (onNavigateBack != null) {
                    IconButton(type = IconType.ArrowBack, contentDescription = "Back", onClick = onNavigateBack)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = header.uppercase(),
                    style = KluvsTheme.typography.eyebrow,
                    color = KluvsTheme.colors.contentMuted,
                    modifier = Modifier.weight(1f),
                )
                action()
            }
        }
        if (title != null) {
            Text(
                text = title,
                style = KluvsTheme.typography.headline.small,
                color = KluvsTheme.colors.content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_TopAppBar_Full() = KluvsTheme {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopAppBar(
            modifier = Modifier.background(color = KluvsTheme.colors.background),
            header = "Club",
            title = "The Great Gatsby Book Club",
            onNavigateBack = null,
            action = {
                IconButton(
                    type = IconType.Help,
                    contentDescription = "More",
                    onClick = {}
                )
            },
        )
        TopAppBar(
            modifier = Modifier.background(color = KluvsTheme.colors.background),
            header = "Club",
            title = "The Great Gatsby Book Club",
            onNavigateBack = {},
            action = {
                IconButton(
                    type = IconType.MoreVert,
                    contentDescription = "More",
                    onClick = {}
                )
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview_TopAppBar_SingleRow() = KluvsTheme {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopAppBar(
            modifier = Modifier.background(color = KluvsTheme.colors.background),
            header = "Settings",
            onNavigateBack = {}
        )
        TopAppBar(
            modifier = Modifier.background(color = KluvsTheme.colors.background),
            header = "Settings",
            onNavigateBack = null
        )
    }
}
