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
            MainScreen(
                onNavigateToPackages = { navController.navigate("membership_packages") },
                onNavigateToActiveMembership = { navController.navigate("active_membership") }
            )
        }

        composable("membership_packages") {
            com.gymapp.android.ui.screens.membership.MembershipPackagesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlanDetail = { planId ->
                    navController.navigate("package_detail/$planId")
                }
            )
        }

        composable(
            route = "package_detail/{planId}",
            arguments = listOf(androidx.navigation.navArgument("planId") { type = androidx.navigation.NavType.StringType })
        ) {
            com.gymapp.android.ui.screens.membership.PackageDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("active_membership") {
            com.gymapp.android.ui.screens.membership.ActiveMembershipScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPackages = { 
                    navController.navigate("membership_packages") {
                        popUpTo("active_membership") { inclusive = true }
                    }
                }
            )
        }
    }
}
