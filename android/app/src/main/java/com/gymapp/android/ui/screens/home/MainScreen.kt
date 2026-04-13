    package com.gymapp.android.ui.screens.home

    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.ui.graphics.Color
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Home
    import androidx.compose.material.icons.filled.Person
    import androidx.compose.material.icons.filled.Star
    import androidx.compose.material.icons.filled.DateRange
    import androidx.compose.material.icons.filled.AddCircle
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.navigation.NavHostController
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.compose.rememberNavController

    sealed class BottomNavRoute(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
        object Dashboard : BottomNavRoute("dashboard", "Trang Chủ", Icons.Default.Home)
        object PTBooking : BottomNavRoute("booking", "Thuê PT", Icons.Default.Star)
        object Profile : BottomNavRoute("profile", "Cá nhân", Icons.Default.Person)
        object UserBookings : BottomNavRoute("user_bookings", "Lịch", Icons.Default.DateRange)
        object PtQueue : BottomNavRoute("pt_queue", "Lịch hẹn", Icons.Default.DateRange)
        object PtClients : BottomNavRoute("pt_clients", "Clients", Icons.Default.AddCircle) // Assuming Icons.Default.AddCircle as Group isn't imported
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(
        onNavigateToPackages: () -> Unit = {},
        onNavigateToActiveMembership: () -> Unit = {},
        onLogout: () -> Unit = {},
        onNavigateToGoal: () -> Unit = {},
        onNavigateToWorkout: () -> Unit = {},
        mainViewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    ) {
        val navController = rememberNavController()
        val userRole by mainViewModel.userRole.collectAsState()
        val needSetupGoal by mainViewModel.needSetupGoal.collectAsState()

        LaunchedEffect(needSetupGoal) {
            if (needSetupGoal) {
                onNavigateToGoal()
            }
        }

        val userItems = listOf(
            BottomNavRoute.Dashboard,
            BottomNavRoute.PTBooking,
            BottomNavRoute.UserBookings,
            BottomNavRoute.Profile
        )

        val ptItems = listOf(
            BottomNavRoute.Dashboard,
            BottomNavRoute.PtQueue,
            BottomNavRoute.PtClients,
            BottomNavRoute.Profile
        )

        val items = if (userRole == "PT") ptItems else userItems

        var currentRoute by remember { mutableStateOf(BottomNavRoute.Dashboard.route) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                currentRoute = screen.route
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
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
                startDestination = BottomNavRoute.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomNavRoute.Dashboard.route) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        Text("Xin chào, bạn 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Hôm nay là ngày tập luyện tuyệt vời!", color = Color.Gray)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Banner Hội Viên
                        if (userRole == "USER") {
                            Card(
                                onClick = onNavigateToActiveMembership,
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Hội viên của tôi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("Nhấn vào đây để xem chi tiết & QR", color = Color(0xFFC8E6C9), fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Quick Actions
                        if (userRole == "USER") {
                            Text("Thao tác nhanh", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = onNavigateToPackages,
                                    modifier = Modifier.weight(1f).height(60.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                                ) {
                                    Text("🎫 Mua gói HV", fontSize = 16.sp)
                                }

                                Button(
                                    onClick = { navController.navigate(BottomNavRoute.PTBooking.route) },
                                    modifier = Modifier.weight(1f).height(60.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E))
                                ) {
                                    Text("🏋️ Thuê PT", fontSize = 16.sp)
                                }
                                Button(
                                    onClick = onNavigateToWorkout,   // THÊM NÚT NÀY
                                    modifier = Modifier.weight(1f).height(60.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                                ) {
                                    Text("🏋️ Luyện tập", fontSize = 16.sp)
                                }
                            }
                        } else if (userRole == "PT") {
                            // PT Dashboard content
                            Text("Trạng thái quản lý", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Chào huấn luyện viên! Chúc bạn có một ngày làm việc hiệu quả.", color = Color.Gray, fontSize = 15.sp)
                        }
                    }
                }
                composable(BottomNavRoute.PTBooking.route) {
                    com.gymapp.android.ui.screens.pt.PtListScreen(
                        onNavigateToBooking = { ptId ->
                            navController.navigate("pt_booking/$ptId")
                        }
                    )
                }
                composable(
                    route = "pt_booking/{ptId}",
                    arguments = listOf(androidx.navigation.navArgument("ptId") { type = androidx.navigation.NavType.StringType })
                ) { backStackEntry ->
                    com.gymapp.android.ui.screens.pt.PtBookingScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNext = { ptId ->
                            navController.navigate("pt_booking_confirm/$ptId")
                        }
                    )
                }
                composable(
                    route = "pt_booking_confirm/{ptId}",
                    arguments = listOf(androidx.navigation.navArgument("ptId") { type = androidx.navigation.NavType.StringType })
                ) { backStackEntry ->
                    com.gymapp.android.ui.screens.pt.PtBookingConfirmScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onBookingSuccess = { paymentUrl ->
                            // Open payment URL or success screen
                            // For now we just go back to clean the flow
                            navController.popBackStack("booking", inclusive = false)
                        }
                    )
                }
                composable(BottomNavRoute.UserBookings.route) {
                    com.gymapp.android.ui.screens.pt.UserBookingsScreen(
                        onNavigateToCancel = { route ->
                            navController.navigate(route)
                        }
                    )
                }
                composable(BottomNavRoute.PtQueue.route) {
                    com.gymapp.android.ui.screens.pt.PtQueueScreen()
                }
                composable(BottomNavRoute.PtClients.route) {
                    com.gymapp.android.ui.screens.pt.PtClientsScreen(
                        onNavigateToClientProgress = { userId, clientName, totalSessions, lastSessionAt ->
                            val encodedName = java.net.URLEncoder.encode(clientName, "UTF-8")
                            val dateStr = lastSessionAt ?: "none"
                            navController.navigate("client_progress/$userId/$encodedName/$totalSessions/$dateStr")
                        }
                    )
                }
                composable(
                    route = "client_progress/{userId}/{clientName}/{totalSessions}/{lastSessionAt}",
                    arguments = listOf(
                        androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType },
                        androidx.navigation.navArgument("clientName") { type = androidx.navigation.NavType.StringType },
                        androidx.navigation.navArgument("totalSessions") { type = androidx.navigation.NavType.LongType },
                        androidx.navigation.navArgument("lastSessionAt") { type = androidx.navigation.NavType.StringType }
                    )
                ) {
                    com.gymapp.android.ui.screens.pt.ClientProgressScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "cancel_booking/{bookingId}/{ptName}/{scheduledAt}/{amount}",
                    arguments = listOf(
                        androidx.navigation.navArgument("bookingId") { type = androidx.navigation.NavType.StringType },
                        androidx.navigation.navArgument("ptName") { type = androidx.navigation.NavType.StringType },
                        androidx.navigation.navArgument("scheduledAt") { type = androidx.navigation.NavType.StringType },
                        androidx.navigation.navArgument("amount") { type = androidx.navigation.NavType.StringType }
                    )
                ) { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    val ptName = backStackEntry.arguments?.getString("ptName") ?: ""
                    val scheduledAt = backStackEntry.arguments?.getString("scheduledAt") ?: ""
                    val amount = backStackEntry.arguments?.getString("amount") ?: "0"

                    com.gymapp.android.ui.screens.pt.PtBookingCancelScreen(
                        bookingId = bookingId,
                        ptName = ptName,
                        scheduledAt = scheduledAt,
                        amount = amount,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(BottomNavRoute.Profile.route) {
                    com.gymapp.android.ui.screens.profile.ProfileScreen(
                        onLogout = onLogout,
                        onNavigateToGoal = {
                            onNavigateToGoal()
                        }

                    )
                }
                }
            }
        }


