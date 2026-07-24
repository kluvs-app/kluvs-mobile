package com.ivangarzab.kluvs.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.components.icons.Icon
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
    onNavigateToSettings: () -> Unit = {},
    onNavigateToJoin: () -> Unit = {},
) {
    MainScreenContent(
        modifier = modifier,
        userId = userId,
        initialClubId = initialClubId,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToJoin = onNavigateToJoin,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    userId: String,
    initialClubId: String? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToJoin: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        pageCount = { 3 },
        initialPage = if (initialClubId != null) 1 else 0
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
                    onNavigateToJoin = onNavigateToJoin,
                )
                2 -> BooksScreen(
                    modifier = contentModifier,
                )
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