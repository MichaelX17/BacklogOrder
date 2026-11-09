package com.backlogorder.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.backlogorder.BacklogOrderApplication
import com.backlogorder.ui.gamedetail.GameDetailScreen
import com.backlogorder.ui.games.GamesScreen
import com.backlogorder.ui.home.HomeScreen
import com.backlogorder.ui.listdetail.ListDetailScreen
import com.backlogorder.ui.lists.ListsScreen
import com.backlogorder.ui.manualgame.ManualGameScreen
import com.backlogorder.ui.onboarding.OnboardingScreen
import com.backlogorder.ui.search.SearchScreen
import com.backlogorder.ui.settings.SettingsScreen

sealed class BacklogOrderRoute(val route: String) {
    data object Onboarding : BacklogOrderRoute("onboarding")
    data object Home : BacklogOrderRoute("home")
    data object ListsOverview : BacklogOrderRoute("lists_overview")
    data object Games : BacklogOrderRoute("games")
    data object Search : BacklogOrderRoute("search")
    data object Settings : BacklogOrderRoute("settings")
    data object ManualGame : BacklogOrderRoute("manual_game")

    data class Lists(val listId: String) : BacklogOrderRoute("lists/$listId")
    data class Game(val listId: String, val gameId: String) : BacklogOrderRoute("game/$listId/$gameId")

    companion object {
        fun searchRoute(listId: String? = null): String = if (listId == null) "search" else "search?listId=$listId"
        fun manualGameRoute(listId: String? = null): String = if (listId == null) "manual_game" else "manual_game?listId=$listId"
    }
}

@Composable
fun BacklogOrderNavHost(
    navController: NavHostController,
    startDestination: String? = null,
) {
    val context = LocalContext.current.applicationContext
    val container = (context as? BacklogOrderApplication)?.container
    val settingsRepository = container?.settingsRepository

    val onboardingComplete by (settingsRepository?.onboardingComplete?.collectAsState(initial = null)
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) })

    if (onboardingComplete == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Loading state while checking onboarding status
        }
        return
    }

    val resolvedStartDestination = startDestination ?: if (onboardingComplete == true) {
        BacklogOrderRoute.Home.route
    } else {
        BacklogOrderRoute.Onboarding.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutes = listOf(
        BacklogOrderRoute.Home.route,
        BacklogOrderRoute.ListsOverview.route,
        BacklogOrderRoute.Games.route,
        BacklogOrderRoute.Settings.route,
    )

    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            if (showBottomBar) {
                BacklogBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(BacklogOrderRoute.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = resolvedStartDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable(BacklogOrderRoute.Onboarding.route) {
                OnboardingScreen(
                    onNavigateToHome = {
                        navController.navigate(BacklogOrderRoute.Home.route) {
                            popUpTo(BacklogOrderRoute.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(BacklogOrderRoute.Home.route) {
                HomeScreen(
                    onListClick = { listId -> navController.navigate("lists/$listId") },
                    onGameClick = { listId, gameId -> navController.navigate("game/$listId/$gameId") },
                    onSearchClick = { navController.navigate(BacklogOrderRoute.Search.route) },
                )
            }
            composable(BacklogOrderRoute.ListsOverview.route) {
                ListsScreen(
                    onListClick = { listId -> navController.navigate("lists/$listId") },
                )
            }
            composable(BacklogOrderRoute.Games.route) {
                GamesScreen(
                    onGameClick = { listId, gameId -> navController.navigate("game/$listId/$gameId") },
                )
            }
            composable(BacklogOrderRoute.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = "search?listId={listId}",
                arguments = listOf(
                    navArgument("listId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString("listId")
                SearchScreen(
                    listId = listId,
                    onBack = { navController.popBackStack() },
                    onNavigateToOnboarding = {
                        navController.navigate(BacklogOrderRoute.Onboarding.route) {
                            popUpTo(BacklogOrderRoute.Search.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = "manual_game?listId={listId}",
                arguments = listOf(
                    navArgument("listId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString("listId")
                ManualGameScreen(
                    listId = listId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(
                route = "lists/{listId}",
                arguments = listOf(navArgument("listId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString("listId") ?: "unknown"
                ListDetailScreen(
                    listId = listId,
                    onBack = { navController.popBackStack() },
                    onGameClick = { _, gameId -> navController.navigate("game/$listId/$gameId") },
                    onAddRawgGameClick = { id -> navController.navigate(BacklogOrderRoute.searchRoute(id)) },
                    onAddManualGameClick = { id -> navController.navigate(BacklogOrderRoute.manualGameRoute(id)) },
                )
            }
            composable(
                route = "game/{listId}/{gameId}",
                arguments = listOf(
                    navArgument("listId") { type = NavType.StringType },
                    navArgument("gameId") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString("listId") ?: "unknown"
                val gameId = backStackEntry.arguments?.getString("gameId") ?: "unknown"
                GameDetailScreen(
                    listId = listId,
                    gameId = gameId,
                    onBack = { navController.popBackStack() },
                    onRemoved = { navController.popBackStack() },
                )
            }
        }
    }
}
