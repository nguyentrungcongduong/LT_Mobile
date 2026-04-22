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
import com.gymapp.android.ui.screens.checkin.AdminCheckinLogScreen
import com.gymapp.android.ui.screens.checkin.QrDisplayScreen
import com.gymapp.android.ui.screens.checkin.QrScanScreen
import com.gymapp.android.ui.screens.pt.PaymentWebViewScreen
import kotlinx.coroutines.flow.collectLatest

sealed class Route(val route: String) {
    object Login : Route("login")
    object Register : Route("register")
    object Main : Route("main")
    object Goal : Route("goal/{isPt}")
    object WorkoutList : Route("workout")
    object WorkoutDetail : Route("workout/{planId}")
    object CreateSchedule : Route("create_schedule")
    object NotificationInbox : Route("notification_inbox")
    object WorkoutScheduleSettings : Route("workout_schedule_settings")
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
                onRegisterSuccess = { isPt ->
                    navController.navigate("goal/$isPt") {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "goal/{isPt}",
            arguments = listOf(navArgument("isPt") { type = NavType.BoolType; defaultValue = false })
        ) { backStackEntry ->
            val isPt = backStackEntry.arguments?.getBoolean("isPt") ?: false
            val onDone: () -> Unit = {
                navController.navigate(Route.Main.route) {
                    popUpTo("goal/{isPt}") { inclusive = true }
                }
            }
            if (isPt) {
                com.gymapp.android.ui.screens.home.PtProfileSetupScreen(onDone = onDone)
            } else {
                com.gymapp.android.ui.screens.home.GoalScreen(onDone = onDone)
            }
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
                    navController.navigate("goal/false")
                },
                onNavigateToWorkout = { navController.navigate(Route.WorkoutList.route) },
                onNavigateToQrDisplay = { navController.navigate("qr_display") },
                onNavigateToQrScan = { navController.navigate("qr_scan") },
                onNavigateToPtApproval = { navController.navigate("admin_pt_approval") },
                onNavigateToCheckinLog = { navController.navigate("admin_checkin_log") },
                onNavigateToNotifications = { navController.navigate(Route.NotificationInbox.route) },
                onNavigateToWorkoutScheduleSettings = { navController.navigate(Route.WorkoutScheduleSettings.route) }
            )
        }
        composable("admin_pt_approval") {
            com.gymapp.android.ui.screens.admin.pt.AdminPtApprovalScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("qr_display") {
            QrDisplayScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("qr_scan") {
            QrScanScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("admin_checkin_log") {
            AdminCheckinLogScreen(onNavigateBack = { navController.popBackStack() })
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
            WorkoutDetailScreenWrapper(planId = planId, navController = navController)
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPayment = { encodedUrl ->
                    navController.navigate("payment_webview/$encodedUrl")
                }
            )
        }

        composable("active_membership") {
            com.gymapp.android.ui.screens.membership.ActiveMembershipScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPackages = { 
                    navController.navigate("membership_packages") {
                        popUpTo("active_membership") { inclusive = true }
                    }
                },
                onNavigateToQrDisplay = {
                    navController.navigate("qr_display")
                }
            )
        }
        composable(
            route = "payment_webview/{encodedUrl}",
            arguments = listOf(navArgument("encodedUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
            PaymentWebViewScreen(
                encodedUrl = encodedUrl,
                onNavigateBack = { navController.popBackStack() },
                onPaymentResultReceived = { _ ->
                    navController.navigate("active_membership") {
                        popUpTo("membership_packages") { inclusive = false }
                    }
                }
            )
        }
        composable(Route.NotificationInbox.route) {
            com.gymapp.android.ui.screens.notification.NotificationInboxScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Route.WorkoutScheduleSettings.route) {
            com.gymapp.android.ui.screens.settings.WorkoutScheduleSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
