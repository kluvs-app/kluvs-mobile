package com.ivangarzab.kluvs.designsystem.components.appbars

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.buttons.IconButton
import com.ivangarzab.kluvs.designsystem.components.fields.SearchField
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

private const val CONTENT_FADE_MS = 150
private const val SEARCH_UNFURL_MS = 200
private val TitleRowHeight = 64.dp

/**
 * [TopAppBar] with search baked in — a search action (in [action]'s position) unfurls a
 * [SearchField] in from the right (scale-x, matching the original hand-rolled `BooksTopBar`
 * this generalizes), fading the header/title content out and shrinking the bar down to a
 * single row height for the duration of the search, regardless of whether [title] would
 * otherwise put it in two-row mode.
 *
 * Caller owns [isSearchActive]/[searchQuery] (same convention as the screen this pattern is
 * lifted from) — this component only renders the transition, it doesn't own search state or
 * perform the search itself.
 */
@Composable
fun SearchTopAppBar(
    header: String,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    action: @Composable () -> Unit = {},
    isSearchLoading: Boolean = false,
    searchPlaceholder: String = "Search",
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (isSearchActive) 0f else 1f,
        animationSpec = tween(CONTENT_FADE_MS),
        label = "SearchTopAppBarContentAlpha",
    )
    val searchProgress by animateFloatAsState(
        targetValue = if (isSearchActive) 1f else 0f,
        animationSpec = tween(SEARCH_UNFURL_MS, easing = FastOutSlowInEasing),
        label = "SearchTopAppBarSearchProgress",
    )
    val targetHeight = if (isSearchActive || title == null) TopAppBarRowHeight else TopAppBarRowHeight + TitleRowHeight
    val barHeight by animateDpAsState(targetHeight, tween(SEARCH_UNFURL_MS), label = "SearchTopAppBarHeight")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight),
    ) {
        Box(modifier = Modifier.fillMaxSize().alpha(contentAlpha)) {
            TopAppBar(
                header = header,
                title = title,
                onNavigateBack = onNavigateBack,
                action = {
                    action()
                    IconButton(
                        type = IconType.Search,
                        contentDescription = "Search",
                        onClick = { onSearchActiveChange(true) },
                        enabled = !isSearchActive,
                    )
                },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TopAppBarRowHeight)
                .graphicsLayer {
                    scaleX = searchProgress
                    alpha = searchProgress
                    transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
                }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                type = IconType.ArrowBack,
                contentDescription = "Close search",
                onClick = { onSearchActiveChange(false) },
                tint = MaterialTheme.colorScheme.onSurface,
            )
            SearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = searchPlaceholder,
                isLoading = isSearchLoading,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_SearchTopAppBar_Full() = KluvsTheme {
    var isSearchActive by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    SearchTopAppBar(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        header = "Library",
        title = "My Shelf",
        onNavigateBack = {},
        isSearchActive = isSearchActive,
        onSearchActiveChange = { isSearchActive = it },
        searchQuery = query,
        onSearchQueryChange = { query = it },
        searchPlaceholder = "Search books",
    )
}

@PreviewLightDark
@Composable
private fun Preview_SearchTopAppBar_Active() = KluvsTheme {
    var query by remember { mutableStateOf("Kluvs") }
    SearchTopAppBar(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        header = "Library",
        title = "My Shelf",
        onNavigateBack = {},
        isSearchActive = true,
        onSearchActiveChange = {},
        searchQuery = query,
        onSearchQueryChange = { query = it },
        searchPlaceholder = "Search books",
    )
}
