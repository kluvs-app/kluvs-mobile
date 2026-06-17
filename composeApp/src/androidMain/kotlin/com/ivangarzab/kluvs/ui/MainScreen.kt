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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.clubs.ClubsScreen
import com.ivangarzab.kluvs.ui.me.MeScreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    userId: String,
    onNavigateToSettings: () -> Unit = {},
) {
    MainScreenContent(
        modifier = modifier,
        userId = userId,
        onNavigateToSettings = onNavigateToSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    userId: String,
    onNavigateToSettings: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        pageCount = { 2 },
        initialPage = 0
    )

    val titles = listOf(
        stringResource(R.string.clubs),
        stringResource(R.string.me)
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = pagerState.currentPage,
                        transitionSpec = {
                            slideInVertically { -it } togetherWith slideOutVertically { it }
                        },
                        label = "title_animation"
                    ) { page ->
                        Text(titles[page])
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background
            ) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                val clubScale by animateFloatAsState(
                    targetValue = if (pagerState.currentPage == 0) 1f else 0.85f,
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
                            painter = painterResource(R.drawable.ic_club),
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(R.string.clubs)) },
                    selected = pagerState.currentPage == 0,
                    colors = itemColors,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )

                val meScale by animateFloatAsState(
                    targetValue = if (pagerState.currentPage == 1) 1f else 0.85f,
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
                            painter = painterResource(R.drawable.ic_user),
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(R.string.me)) },
                    selected = pagerState.currentPage == 1,
                    colors = itemColors,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
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
                0 -> ClubsScreen(
                    modifier = contentModifier,
                    userId = userId,
                )
                1 -> /*HomeScreen(contentModifier)
                2 ->*/ MeScreen(
                    modifier = contentModifier,
                    userId = userId,
                    onNavigateToSettings = onNavigateToSettings,
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