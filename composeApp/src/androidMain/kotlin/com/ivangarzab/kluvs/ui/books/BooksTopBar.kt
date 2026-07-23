package com.ivangarzab.kluvs.ui.books

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.brandPrimary
import com.ivangarzab.kluvs.designsystem.theme.foregroundLightPlaceholder
import com.ivangarzab.kluvs.designsystem.theme.foregroundWarmPlaceholder
import com.ivangarzab.kluvs.designsystem.theme.lightCard
import com.ivangarzab.kluvs.designsystem.theme.lightDivider
import com.ivangarzab.kluvs.designsystem.theme.warmDarkCard
import com.ivangarzab.kluvs.designsystem.theme.warmDarkCard2

private const val TITLE_FADE_MS = 150
private const val SEARCH_UNFURL_MS = 200

/**
 * Single animated top bar for the Books tab — mirrors web's header: the title fades out in
 * place while a bordered search field scales in from the right (`origin-right` scale-x, not a
 * slide), landing where the search button used to be. Each tab now owns its own top bar
 * (MainScreen no longer hosts a shared TopAppBar); this establishes the pattern for Clubs/Me.
 */
@Composable
fun BooksTopBar(
    modifier: Modifier = Modifier,
    isSearchActive: Boolean,
    isSearching: Boolean = false,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        } else {
            keyboardController?.hide()
        }
    }

    val titleAlpha by animateFloatAsState(
        targetValue = if (isSearchActive) 0f else 1f,
        animationSpec = tween(TITLE_FADE_MS),
        label = "BooksTopBarTitleAlpha"
    )
    val searchProgress by animateFloatAsState(
        targetValue = if (isSearchActive) 1f else 0f,
        animationSpec = tween(SEARCH_UNFURL_MS, easing = FastOutSlowInEasing),
        label = "BooksTopBarSearchProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        // Title row — fades in place, no positional movement.
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .alpha(titleAlpha),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.books),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onSearchClick, enabled = !isSearchActive) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search_books)
                )
            }
        }

        // Search row — unfurls from the right edge (scale-x from the search button's position),
        // landing over the same header area rather than sliding in.
        Row(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = searchProgress
                    alpha = searchProgress
                    transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
                }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back)
                )
            }
            SearchInputBox(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                query = query,
                onQueryChange = onQueryChange,
                isSearching = isSearching
            )
        }
    }
}

/**
 * Outlined search field matching web's `.kluvs-input`-style box: input-bg fill, input-border
 * hairline that lights up copper (brand primary) while focused.
 */
@Composable
private fun SearchInputBox(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isDark = isSystemInDarkTheme()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) brandPrimary else if (isDark) warmDarkCard2 else lightDivider,
        animationSpec = tween(150),
        label = "SearchInputBorderColor"
    )
    val accentColor = if (isFocused) brandPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundColor = if (isDark) warmDarkCard else lightCard
    val placeholderColor = if (isDark) foregroundWarmPlaceholder else foregroundLightPlaceholder

    Row(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp)) // radius.input
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_books_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = placeholderColor
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                interactionSource = interactionSource,
                cursorBrush = SolidColor(brandPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
        }

        if (isSearching) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(16.dp),
                strokeWidth = 2.dp,
                color = accentColor
            )
        } else {
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(18.dp),
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = accentColor
            )
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_BooksTopBar_Title() = KluvsTheme {
    BooksTopBar(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        isSearchActive = false,
        query = "",
        onQueryChange = {},
        onSearchClick = {},
        onBackClick = {}
    )
}

@PreviewLightDark
@Composable
fun Preview_BooksTopBar_Search() = KluvsTheme {
    BooksTopBar(
        modifier = Modifier
            .width(360.dp)
            .background(MaterialTheme.colorScheme.background),
        isSearchActive = true,
        query = "Klara",
        onQueryChange = {},
        onSearchClick = {},
        onBackClick = {}
    )
}
