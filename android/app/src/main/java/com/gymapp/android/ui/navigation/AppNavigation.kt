package com.gymapp.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymapp.android.data.remote.AuthEventBus
import com.gymapp.android.data.remote.AuthEvent
import com.gymapp.android.ui.screens.auth.AuthViewModel
import com.gymapp.android.ui.screens.auth.LoginScreen
import com.gymapp.android.ui.screens.auth.RegisterScreen
import com.gymapp.android.ui.screens.home.MainScreen
import com.gymapp.android.ui.screens.training.CreateScheduleScreen
import com.gymapp.android.ui.screens.training.WorkoutDetailScreenWrapper
import com.gymapp.android.ui.screens.training.WorkoutHistoryScreen
import com.gymapp.android.ui.screens.training.WorkoutMenuScreen
import com.gymapp.android.ui.screens.training.WorkoutScreen
import kotlinx.coroutines.flow.collectLatest

sealed class Route(val route: String) {
    object Login : Route("login")
    object Register : Route("register")
    object Main : Route("main")
    object Goal : Route("goal")
    object WorkoutList : Route("workout")
    object WorkoutDetail : Route("workout/{planId}")
    object CreateSchedule : Route("create_schedule")

}

@Composable
fun AppNavigation(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    // Bắt đầu từ màn Login hoặc Main tùy theo trạng thái có token hay không
    val startDestination = if (authViewModel.isLoggedIn()) Route.Main.route else Route.Login.route

    // Lắng nghe sự kiện token hết hạn để tự động logout
    LaunchedEffect(Unit) {
        authViewModel.authEventBus.authEvents.collectLatest { event ->
            when (event) {
                is AuthEvent.TokenExpired -> {
                    authViewModel.logout()
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

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
        composable(Route.CreateSchedule.route) {
            CreateScheduleScreen(navController)
        }
        composable("workout_history") {
            WorkoutHistoryScreen(navController)
        }

        composable(Route.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Route.Goal.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Goal.route) {
            com.gymapp.android.ui.screens.home.GoalScreen(
                onDone = {
                    navController.navigate(Route.Main.route) {
                        popUpTo(Route.Goal.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Main.route) {
            MainScreen(
                onNavigateToPackages = { navController.navigate("membership_packages") },
                onNavigateToActiveMembership = { navController.navigate("active_membership") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                        onNavigateToGoal = {
                    navController.navigate(Route.Goal.route)
                },
                onNavigateToWorkout = { navController.navigate(Route.WorkoutList.route) }

            )
        }
        composable(Route.WorkoutList.route) {
            WorkoutScreen(navController)
        }

        composable(
            route = "workout/{planId}",
            arguments = listOf(navArgument("planId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->

            val planId = backStackEntry.arguments?.getString("planId")!!

            WorkoutDetailScreenWrapper(planId)
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
