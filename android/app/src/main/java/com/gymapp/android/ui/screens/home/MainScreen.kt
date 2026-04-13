package com.gymapp.android.ui.screens.home

import androidx.compose.foundation.layout.*
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
import androidx.navigation.compose.currentBackStackEntryAsState
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
    onNavigateToQrDisplay: () -> Unit = {},
    onNavigateToQrScan: () -> Unit = {},
    onNavigateToPtApproval: () -> Unit = {},
    mainViewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val navController = rememberNavController()
    val userRole by mainViewModel.userRole.collectAsState()
    val userAvatar by mainViewModel.userAvatar.collectAsState()
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

    val adminItems = listOf(
        BottomNavRoute.Dashboard,
        BottomNavRoute.Profile
    )

    val items = when (userRole) {
        "PT" -> ptItems
        "ADMIN" -> adminItems
        else -> userItems
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavRoute.Dashboard.route

    LaunchedEffect(currentRoute) {
        if (currentRoute == BottomNavRoute.Dashboard.route) {
            mainViewModel.fetchProfile()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) {
                                        saveState = true
                                    }
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
            startDestination = BottomNavRoute.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavRoute.Dashboard.route) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (userRole == "PT") "Xin chào, HLV 👋" else "Xin chào, bạn 👋", 
                                fontSize = 24.sp, 
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (userRole == "PT") "Chúc một ngày làm việc hiệu quả!" else "Hôm nay là ngày tập luyện tuyệt vời!", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            if (!userAvatar.isNullOrEmpty()) {
                                coil.compose.AsyncImage(
                                    model = userAvatar,
                                    contentDescription = "Avatar",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person, 
                                    contentDescription = "Avatar", 
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                    
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
                                onClick = {
                                    navController.navigate(BottomNavRoute.PTBooking.route) {
                                        navController.graph.startDestinationRoute?.let { route ->
                                            popUpTo(route) { saveState = true }
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.weight(1f).height(60.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text("🏋️ Thuê PT", fontSize = 16.sp)
                            }
                            
                            Button(
                                onClick = onNavigateToWorkout,
                                modifier = Modifier.weight(1f).height(60.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                            ) {
                                Text("🔥 Tập luyện", fontSize = 16.sp)
                            }
                        }
                    } else if (userRole == "PT") {
                        Text("Thống kê nhanh", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Ca hôm nay", color = Color.Gray, fontSize = 13.sp)
                                    Text("2", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Khách Active", color = Color.Gray, fontSize = 13.sp)
                                    Text("5", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("Thao tác nhanh", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { 
                                    navController.navigate(BottomNavRoute.PtQueue.route) {
                                        navController.graph.startDestinationRoute?.let { route ->
                                            popUpTo(route) { saveState = true }
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                            ) {
                                Text("📅 Lịch hẹn", fontSize = 14.sp)
                            }
                            Button(
                                onClick = { 
                                    navController.navigate(BottomNavRoute.PtClients.route) {
                                        navController.graph.startDestinationRoute?.let { route ->
                                            popUpTo(route) { saveState = true }
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text("👥 Clients", fontSize = 14.sp)
                            }
                            Button(
                                onClick = onNavigateToQrScan,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                            ) {
                                Text("📷 Quét QR", fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("Lịch sắp tới", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = Color.White, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.padding(8.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("14:30 - Tăng cơ bắp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Học viên: Nguyễn Văn A", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                    } else if (userRole == "ADMIN") {
                        Text("Tổng quan Hệ Thống", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Check-in", color = Color.Gray, fontSize = 13.sp)
                                    Text("128", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Đăng ký mới", color = Color.Gray, fontSize = 13.sp)
                                    Text("15", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("Quản lý & Vận hành", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onNavigateToQrScan,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) {
                                Text("📷 Quét Cửa", fontSize = 14.sp)
                            }
                            
                            Button(
                                onClick = onNavigateToPtApproval,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text("✅ Duyệt PT", fontSize = 14.sp)
                            }
                        }
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
                val parentEntry = remember(backStackEntry) {
                    try {
                        navController.getBackStackEntry("pt_booking/{ptId}")
                    } catch (e: Exception) {
                        backStackEntry
                    }
                }
                val sharedViewModel: com.gymapp.android.ui.screens.pt.PtBookingViewModel = androidx.hilt.navigation.compose.hiltViewModel(parentEntry)

                com.gymapp.android.ui.screens.pt.PtBookingConfirmScreen(
                    viewModel = sharedViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onBookingSuccess = { paymentUrl, bookingId ->
                        val encodedUrl = android.util.Base64.encodeToString(paymentUrl.toByteArray(), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                        navController.navigate("pt_payment_webview/$encodedUrl")
                    }
                )
            }
            composable(
                route = "pt_payment_webview/{encodedUrl}",
                arguments = listOf(androidx.navigation.navArgument("encodedUrl") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
                com.gymapp.android.ui.screens.pt.PaymentWebViewScreen(
                    encodedUrl = encodedUrl,
                    onNavigateBack = { navController.popBackStack() },
                    onPaymentResultReceived = { bookingId ->
                        navController.navigate("pt_payment_result/$bookingId") {
                            popUpTo("pt_booking_confirm/{ptId}") { inclusive = true } // close payment view
                        }
                    }
                )
            }
            composable(
                route = "pt_payment_result/{bookingId}",
                arguments = listOf(androidx.navigation.navArgument("bookingId") { type = androidx.navigation.NavType.StringType }),
                deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "gymapp://payment/result?booking_id={bookingId}" })
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                com.gymapp.android.ui.screens.pt.PaymentResultScreen(
                    bookingId = bookingId,
                    onNavigateHome = {
                        navController.navigate(BottomNavRoute.Dashboard.route) {
                            popUpTo(BottomNavRoute.Dashboard.route) { inclusive = true }
                        }
                    },
                    onRetry = {
                        navController.popBackStack("pt_booking/{ptId}", inclusive = false)
                    },
                    onCancel = {
                        navController.navigate(BottomNavRoute.Dashboard.route) {
                            popUpTo(BottomNavRoute.Dashboard.route) { inclusive = true }
                        }
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
                    onNavigateToGoal = onNavigateToGoal,
                    onNavigateToHistory = { navController.navigate("payment_history") }
                )
            }
            composable(route = "payment_history") {
                com.gymapp.android.ui.screens.pt.PaymentHistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

