package com.ivangarzab.kluvs.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.ivangarzab.kluvs.designsystem.components.KluvsSnackbar
import com.ivangarzab.kluvs.designsystem.components.KluvsSnackbarVisuals
import com.ivangarzab.kluvs.designsystem.components.SnackbarVariant
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.app.AppCoordinator
import com.ivangarzab.kluvs.app.ConnectivityCoordinator
import com.ivangarzab.kluvs.app.AutoJoinResult
import com.ivangarzab.kluvs.app.NavigationState
import com.ivangarzab.kluvs.app.PendingJoinCoordinator
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.auth.LoginScreen
import com.ivangarzab.kluvs.ui.auth.SignupScreen
import com.ivangarzab.kluvs.ui.join.JoinScreen
import com.ivangarzab.kluvs.ui.settings.SettingsScreen
import org.koin.android.ext.android.inject
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {

    // Same singleton the NavHost observes — injected here because intents arrive at the
    // Activity, potentially before any composition exists to receive them.
    private val pendingJoinCoordinator: PendingJoinCoordinator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Handle OAuth callback or invite link if app was launched via deep link
        handleDeepLinkIntent(intent)

        setContent {
            KluvsTheme {
                val navController = rememberNavController()
                MainNavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = KluvsTheme.colors.background),
                    navController = navController
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val uri = intent?.data ?: return

        if (uri.scheme == "kluvs" && uri.host == "auth" && uri.path == "/callback") {
            Bark.d("Processing OAuth callback from deep link")
            OAuthCallbackHandler.handleCallback(uri.toString())
            return
        }

        // Invite links are routed through the coordinator rather than navigated to directly:
        // this can run before auth (and the NavHost) has resolved, and the token must survive
        // the navigation reset that every auth-state transition triggers.
        if (pendingJoinCoordinator.onInviteLinkOpened(uri.toString())) return

        Bark.d("Ignoring unrecognized deep link")
    }
}

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val appCoordinator: AppCoordinator = koinViewModel()
    val pendingJoinCoordinator: PendingJoinCoordinator = koinViewModel()
    val connectivityCoordinator: ConnectivityCoordinator = koinViewModel()
    val navState by appCoordinator.navigationState.collectAsState()
    val isOffline by connectivityCoordinator.isOffline.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Only set on a successful auto-join after sign-in; consumed by the MAIN destination
    // to open straight into that club (see PendingJoinCoordinator).
    var autoJoinedClubId by remember { mutableStateOf<String?>(null) }

    // Navigate based on app-level state
    LaunchedEffect(navState) {
        when (navState) {
            is NavigationState.Unauthenticated -> {
                // Only navigate if not already on login — and never off the Join screen, which
                // is a legitimate signed-out destination when it was opened from an invite link.
                // The recipient previews the club there first and hands off to Login themselves
                // via onNeedsSignIn; yanking them to Login here would lose the invite entirely.
                if (navController.currentDestination?.route !in NavDestinations.UNAUTHENTICATED_ROUTES) {
                    navController.navigate(NavDestinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is NavigationState.Authenticated -> {
                // Only reset to MAIN if we're not already somewhere in the authenticated
                // flow. Without this, a config change (e.g. rotation) recreates the
                // composition and reruns this effect from scratch — if the user was on
                // Settings at the time, checking against MAIN alone would treat that as
                // "not navigated yet" and force the whole back stack (including Settings)
                // back to MAIN, silently kicking them out of the screen they were on.
                if (navController.currentDestination?.route !in NavDestinations.AUTHENTICATED_ROUTES) {
                    navController.navigate(NavDestinations.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            NavigationState.Initializing -> {
                // Do nothing - show splash/loading
            }
        }
    }

    val incomingInviteToken by pendingJoinCoordinator.incomingInviteToken.collectAsState()

    // Deep-linked invite: wait for auth to resolve (the effect above resets the back stack on
    // every auth transition, so navigating sooner would just get undone), then open Join with
    // the token pre-filled. Works signed in or out — JoinScreen handles both.
    LaunchedEffect(incomingInviteToken, navState) {
        val token = incomingInviteToken ?: return@LaunchedEffect
        if (navState is NavigationState.Initializing) return@LaunchedEffect

        navController.navigate(NavDestinations.joinRoute(token)) {
            launchSingleTop = true
        }
        pendingJoinCoordinator.onConsumeIncomingInviteToken()
    }

    LaunchedEffect(Unit) {
        pendingJoinCoordinator.autoJoinResult.collect { result ->
            when (result) {
                is AutoJoinResult.Success -> autoJoinedClubId = result.clubId
                is AutoJoinResult.Failure -> {
                    snackbarHostState.showSnackbar(
                        KluvsSnackbarVisuals(
                            message = result.message ?: "Failed to join club",
                            variant = SnackbarVariant.DANGER,
                        )
                    )
                }
            }
        }
    }

    Box(modifier = modifier) {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = NavDestinations.LOGIN
        ) {
            composable(NavDestinations.LOGIN) {
                LoginScreen(
                    onNavigateToSignUp = {
                        navController.navigate(NavDestinations.SIGNUP)
                    },
                )
            }
            composable(NavDestinations.SIGNUP) {
                SignupScreen(
                    onNavigateToLogIn = {
                        navController.navigate(NavDestinations.LOGIN)
                    },
                )
            }
            composable(NavDestinations.MAIN) {
                val userId = (navState as? NavigationState.Authenticated)?.userId
                if (userId != null) {
                    MainScreen(
                        userId = userId,
                        initialClubId = autoJoinedClubId,
                        isOffline = isOffline,
                        onNavigateToSettings = {
                            navController.navigate("${NavDestinations.SETTINGS}/$userId")
                        },
                    )
                }
            }
            composable(NavDestinations.SETTINGS_ROUTE) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                SettingsScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            // Reached only from an invite App Link (see handleDeepLinkIntent). "Join with a
            // code" from inside the Clubs tab opens JoinBottomSheet in-place instead — a deep
            // link needs a full screen it can land on before auth resolves, plus the
            // needsSignIn→Login handoff below, neither of which fits a bottom sheet nested
            // inside the (already authenticated) Clubs tab.
            composable(
                route = NavDestinations.JOIN_ROUTE,
                arguments = listOf(
                    navArgument(NavDestinations.JOIN_TOKEN_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                JoinScreen(
                    initialToken = backStackEntry.arguments
                        ?.getString(NavDestinations.JOIN_TOKEN_ARG),
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToClub = { clubId ->
                        autoJoinedClubId = clubId
                        navController.navigate(NavDestinations.MAIN) {
                            popUpTo(NavDestinations.JOIN) { inclusive = true }
                        }
                    },
                    onNeedsSignIn = { token ->
                        pendingJoinCoordinator.setPendingToken(token)
                        navController.navigate(NavDestinations.LOGIN)
                    }
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            snackbar = { KluvsSnackbar(it) }
        )
    }
}

object NavDestinations {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val SETTINGS_ROUTE = "$SETTINGS/{userId}"
    const val JOIN = "join"
    const val JOIN_TOKEN_ARG = "token"
    const val JOIN_ROUTE = "$JOIN?$JOIN_TOKEN_ARG={$JOIN_TOKEN_ARG}"

    /** Route to the Join screen with [token] pre-filled from an invite deep link. */
    fun joinRoute(token: String): String = "$JOIN?$JOIN_TOKEN_ARG=$token"

    /** Routes reachable while [NavigationState.Authenticated] — not just [MAIN] itself. */
    val AUTHENTICATED_ROUTES = setOf(MAIN, SETTINGS_ROUTE, JOIN_ROUTE)

    /**
     * Routes reachable while [NavigationState.Unauthenticated]. [JOIN_ROUTE] is here because an
     * invite link can be opened by someone who has never signed in — they preview the club
     * first, then get routed to auth by their own action.
     *
     * [SIGNUP] is deliberately absent, preserving existing behavior: it gets reset to [LOGIN]
     * on an auth-state emission just as it did before this set existed.
     */
    val UNAUTHENTICATED_ROUTES = setOf(LOGIN, JOIN_ROUTE)
}