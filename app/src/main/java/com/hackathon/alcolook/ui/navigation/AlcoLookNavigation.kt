package com.hackathon.alcolook.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hackathon.alcolook.ui.home.HomeScreen
import com.hackathon.alcolook.ui.calendar.CalendarScreen
import com.hackathon.alcolook.ui.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlcoLookNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                AlcoLookDestinations.entries.forEach { destination ->
                    NavigationBarItem(
                        icon = {
                            Text(
                                text = when (destination) {
                                    AlcoLookDestinations.HOME -> "📷"
                                    AlcoLookDestinations.CALENDAR -> "📅"
                                    AlcoLookDestinations.SETTINGS -> "⚙️"
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        label = { Text(destination.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AlcoLookDestinations.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AlcoLookDestinations.HOME.route) {
                HomeScreen()
            }
            composable(AlcoLookDestinations.CALENDAR.route) {
                CalendarScreen()
            }
            composable(AlcoLookDestinations.SETTINGS.route) {
                SettingsScreen()
            }
        }
    }
}