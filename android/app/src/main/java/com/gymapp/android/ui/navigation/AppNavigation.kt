package com.gymapp.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gymapp.android.ui.screens.auth.AuthViewModel
import com.gymapp.android.ui.screens.auth.LoginScreen
import com.gymapp.android.ui.screens.auth.RegisterScreen
import com.gymapp.android.ui.screens.home.MainScreen

sealed class Route(val route: String) {
    object Login : Route("login")
    object Register : Route("register")
    object Main : Route("main")
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    // Bắt đầu từ màn Login hoặc Main tùy theo trạng thái có token hay không
    val startDestination = if (authViewModel.isLoggedIn()) Route.Main.route else Route.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
        
        composable(Route.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Route.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Route.Main.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Route.Main.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Main.route) {
            MainScreen()
        }
    }
}
