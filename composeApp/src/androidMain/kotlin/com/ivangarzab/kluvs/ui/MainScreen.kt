package com.ivangarzab.kluvs.ui

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.components.OfflineBanner
import com.ivangarzab.kluvs.designsystem.components.icons.Icon
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.books.BooksScreen
import com.ivangarzab.kluvs.ui.clubs.ClubsScreen
import com.ivangarzab.kluvs.ui.me.MeScreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    userId: String,
    initialClubId: String? = null,
    isOffline: Boolean = false,
    onNavigateToSettings: () -> Unit = {},
) {
    MainScreenContent(
        modifier = modifier,
        userId = userId,
        initialClubId = initialClubId,
        isOffline = isOffline,
        onNavigateToSettings = onNavigateToSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    userId: String,
    initialClubId: String? = null,
    isOffline: Boolean = false,
    onNavigateToSettings: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    // The wine banner sits under the status bar, so its icons must be light while it shows
    // (enableEdgeToEdge picks them from the system theme, which is dark-on-wine in light mode).
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(isOffline) {
            val window = (view.context as? Activity)?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, view) }
            val previous = controller?.isAppearanceLightStatusBars
            if (isOffline) controller?.isAppearanceLightStatusBars = false
            onDispose {
                if (previous != null) controller.isAppearanceLightStatusBars = previous
            }
        }
    }

    val pagerState = rememberPagerState(
        pageCount = { 3 },
        initialPage = if (initialClubId != null) 1 else 0
    )

    Scaffold(
        modifier = modifier,
        // The status-bar inset is applied below instead, so the offline banner can extend under it.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
        bottomBar = {
            NavigationBar(
                containerColor = KluvsTheme.colors.card
            ) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = KluvsTheme.colors.accent,
                    unselectedIconColor = KluvsTheme.colors.contentMuted,
                    selectedTextColor = KluvsTheme.colors.accent,
                    unselectedTextColor = KluvsTheme.colors.contentMuted,
                    indicatorColor = KluvsTheme.colors.accent.copy(alpha = 0.2f)
                )
                val meScale by animateFloatAsState(
                    targetValue = if (pagerState.currentPage == 0) 1f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tab_me"
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .scale(meScale),
                            type = IconType.User,
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(R.string.me)) },
                    selected = pagerState.currentPage == 0,
                    colors = itemColors,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )

                val clubScale by animateFloatAsState(
                    targetValue = if (pagerState.currentPage == 1) 1f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tab_club"
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .scale(clubScale),
                            type = IconType.Club,
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(R.string.clubs)) },
                    selected = pagerState.currentPage == 1,
                    colors = itemColors,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )

                val booksScale by animateFloatAsState(
                    targetValue = if (pagerState.currentPage == 2) 1f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tab_books"
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .scale(booksScale),
                            type = IconType.Book,
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(R.string.books)) },
                    selected = pagerState.currentPage == 2,
                    colors = itemColors,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status-bar area: transparent online (same layout as before), wine while offline so
            // the banner and the status bar read as one band.
            val statusBarFill by animateColorAsState(
                targetValue = if (isOffline) KluvsTheme.colors.offline else Color.Transparent,
                animationSpec = tween(durationMillis = 250),
                label = "status_bar_offline_fill"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusBarFill)
                    .windowInsetsPadding(WindowInsets.statusBars)
            )
            OfflineBanner(
                visible = isOffline,
                text = stringResource(R.string.offline_banner),
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = false // Disable swipe navigation for main tabs
            ) { page ->
                val contentModifier = Modifier
                    .fillMaxSize()
                when (page) {
                    0 -> MeScreen(
                        modifier = contentModifier,
                        userId = userId,
                        onNavigateToSettings = onNavigateToSettings,
                    )
                    1 -> ClubsScreen(
                        modifier = contentModifier,
                        userId = userId,
                        initialClubId = initialClubId,
                    )
                    2 -> BooksScreen(
                        modifier = contentModifier,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_MainScreen() = KluvsTheme {
    MainScreenContent(
        userId = ""
    )
}