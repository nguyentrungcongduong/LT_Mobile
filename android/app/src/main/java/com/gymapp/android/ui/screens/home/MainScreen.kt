package com.gymapp.android.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToPackages: () -> Unit = {},
    onNavigateToActiveMembership: () -> Unit = {}
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavRoute.Dashboard,
        BottomNavRoute.PTBooking,
        BottomNavRoute.Profile
    )

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

                    // Quick Actions
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
                            onClick = { },
                            modifier = Modifier.weight(1f).height(60.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E))
                        ) {
                            Text("🏋️ Thuê PT", fontSize = 16.sp)
                        }
                    }
                }
            }
            composable(BottomNavRoute.PTBooking.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Booking/PT Screen") }
            }
            composable(BottomNavRoute.Profile.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("User Profile") }
            }
        }
    }
}

