package com.backlogorder.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.backlogorder.ui.theme.LocalHudColors

import androidx.compose.runtime.remember

sealed class BottomNavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    data object Home : BottomNavDestination(
        route = BacklogOrderRoute.Home.route,
        title = "Home",
        icon = Icons.Default.Home,
    )
    data object Lists : BottomNavDestination(
        route = BacklogOrderRoute.ListsOverview.route,
        title = "Lists",
        icon = Icons.Default.List,
    )
    data object Games : BottomNavDestination(
        route = BacklogOrderRoute.Games.route,
        title = "Games",
        icon = Icons.Default.PlayArrow,
    )
    data object Settings : BottomNavDestination(
        route = BacklogOrderRoute.Settings.route,
        title = "Settings",
        icon = Icons.Default.Settings,
    )
}

@Composable
fun BacklogBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    val colors = LocalHudColors.current
    val items = remember {
        listOf(
            BottomNavDestination.Home,
            BottomNavDestination.Lists,
            BottomNavDestination.Games,
            BottomNavDestination.Settings,
        )
    }

    NavigationBar(
        containerColor = colors.panel,
        contentColor = colors.text,
    ) {
        items.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onNavigate(destination.route)
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.title) },
                label = { Text(destination.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.text,
                    selectedTextColor = colors.text,
                    indicatorColor = colors.accent,
                    unselectedIconColor = colors.textMuted,
                    unselectedTextColor = colors.textMuted,
                ),
            )
        }
    }
}
