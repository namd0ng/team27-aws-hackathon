package com.hackathon.alcolook.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hackathon.alcolook.ui.home.HomeScreen
import com.hackathon.alcolook.ui.calendar.CalendarScreen
import com.hackathon.alcolook.ui.settings.SettingsScreen
import com.hackathon.alcolook.ui.auth.LoginScreen
import com.hackathon.alcolook.ui.auth.SignUpScreen
import com.hackathon.alcolook.ui.auth.ForgotPasswordScreen
import com.hackathon.alcolook.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlcoLookNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // 인증 화면에서는 하단 네비게이션 숨기기
    val showBottomBar = currentDestination?.route in listOf(
        AlcoLookDestinations.HOME.route,
        AlcoLookDestinations.CALENDAR.route,
        AlcoLookDestinations.SETTINGS.route
    )
    
    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = BottomNavBackground,
                    contentColor = BottomNavSelected,
                    tonalElevation = 8.dp
                ) {
                    AlcoLookDestinations.entries.forEach { destination ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        
                        NavigationBarItem(
                            icon = {
                                Text(
                                    text = when (destination) {
                                        AlcoLookDestinations.HOME -> "📷"
                                        AlcoLookDestinations.CALENDAR -> "📅"
                                        AlcoLookDestinations.SETTINGS -> "⚙️"
                                    },
                                    fontSize = 24.sp
                                )
                            },
                            label = { 
                                Text(
                                    text = destination.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BottomNavSelected,
                                selectedTextColor = BottomNavSelected,
                                unselectedIconColor = BottomNavUnselected,
                                unselectedTextColor = BottomNavUnselected,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AlcoLookDestinations.CALENDAR.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AlcoLookDestinations.HOME.route) {
                HomeScreen()
            }
            composable(AlcoLookDestinations.CALENDAR.route) {
                CalendarScreen()
            }
            composable(AlcoLookDestinations.SETTINGS.route) {
                SettingsScreen(
                    onLoginClick = {
                        navController.navigate("login")
                    }
                )
            }
            composable("login") {
                LoginScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLoginSuccess = {
                        // 로그인 성공 시 설정 화면으로 돌아가기
                        navController.popBackStack()
                    },
                    onSignUpClick = {
                        navController.navigate("signup")
                    },
                    onForgotPasswordClick = {
                        navController.navigate("forgotpassword")
                    }
                )
            }
            composable("signup") {
                SignUpScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSignUpSuccess = {
                        navController.popBackStack("login", inclusive = true)
                    }
                )
            }
            composable("forgotpassword") {
                ForgotPasswordScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
