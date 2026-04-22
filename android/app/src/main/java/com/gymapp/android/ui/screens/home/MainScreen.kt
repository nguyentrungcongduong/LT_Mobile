package com.gymapp.android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.gymapp.android.ui.theme.GlowBackground
import com.gymapp.android.ui.theme.OrangePrimary
import kotlinx.coroutines.delay

sealed class BottomNavRoute(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : BottomNavRoute("dashboard", "Trang Chủ", Icons.Default.Home)
    object PTBooking : BottomNavRoute("booking", "Thuê PT", Icons.Default.Star)
    object Profile : BottomNavRoute("profile", "Cá nhân", Icons.Default.Person)
    object UserBookings : BottomNavRoute("user_bookings", "Lịch", Icons.Default.DateRange)
    object PtSchedule : BottomNavRoute("pt_schedule", "Lịch dạy", Icons.Default.DateRange)
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
    onNavigateToCheckinLog: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToWorkoutScheduleSettings: () -> Unit = {},
    mainViewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    bannerViewModel: com.gymapp.android.ui.screens.banner.BannerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val navController = rememberNavController()
    val userRole by mainViewModel.userRole.collectAsState()
    val userAvatar by mainViewModel.userAvatar.collectAsState()
    val adminStats by mainViewModel.adminStats.collectAsState()
    val ptStats by mainViewModel.ptStats.collectAsState()
    val banners by bannerViewModel.banners.collectAsState()
    val unreadCount by mainViewModel.unreadNotifCount.collectAsState()

    val userItems = listOf(
        BottomNavRoute.Dashboard,
        BottomNavRoute.PTBooking,
        BottomNavRoute.UserBookings,
        BottomNavRoute.Profile
    )

    val ptItems = listOf(
        BottomNavRoute.Dashboard,
        BottomNavRoute.PtSchedule,
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
    // Chỉ hiện BottomBar khi ở các tab chính, ẩn khi vào sub-screen (payment_history, v.v.)
    val showBottomBar = items.any { it.route == currentRoute }

    LaunchedEffect(currentRoute) {
        if (currentRoute == BottomNavRoute.Dashboard.route) {
            mainViewModel.fetchProfile()
            mainViewModel.fetchUnreadNotifCount()
        }
        if (currentRoute == BottomNavRoute.Dashboard.route && userRole == "PT") {
            mainViewModel.fetchPtStats()
        }
    }

    GlowBackground {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
            NavigationBar(
                containerColor = Color(0xFF1A1A1E),
                contentColor = Color(0xFFB0B0B5),
                tonalElevation = 0.dp
            ) {
                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontSize = 11.sp) },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrangePrimary,
                            selectedTextColor = OrangePrimary,
                            indicatorColor = Color(0xFF2A1A0D),
                            unselectedIconColor = Color(0xFF606068),
                            unselectedTextColor = Color(0xFF606068)
                        ),
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
            } // end if showBottomBar
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavRoute.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(BottomNavRoute.Dashboard.route) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Notification bell với badge số chưa đọc
                            androidx.compose.material3.BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        androidx.compose.material3.Badge(
                                            containerColor = Color(0xFFFF3B30)
                                        ) {
                                            Text(
                                                if (unreadCount > 99) "99+" else unreadCount.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                androidx.compose.material3.IconButton(onClick = {
                                    mainViewModel.clearUnreadNotifCount()
                                    onNavigateToNotifications()
                                }) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.NotificationsNone,
                                        contentDescription = "Thông báo",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
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
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // ── USER DASHBOARD ──────────────────────────────────────
                    if (userRole == "USER") {

                        // ── Premium Membership Card ──────────────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460)),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    )
                                )
                                .clickable(onClick = onNavigateToActiveMembership)
                                .padding(20.dp)
                        ) {
                            // Decorative circles
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x0DFFFFFF))
                                    .align(Alignment.TopEnd)
                                    .offset(x = 20.dp, y = (-20).dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x0AFFFFFF))
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-10).dp, y = 30.dp)
                            )
                            // Content
                            Column(
                                modifier = Modifier.align(Alignment.CenterStart),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF8C00))
                                    )
                                    Text("HỘI VIÊN", color = Color(0xFFFF8C00), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                }
                                Text("GYM FITNESS", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text("Đã tham gia", color = Color(0x88FFFFFF), fontSize = 10.sp)
                                        Text("2024", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Column {
                                        Text("Gói", color = Color(0x88FFFFFF), fontSize = 10.sp)
                                        Text("Standard", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            // QR icon in top-right
                            Column(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFFF8C00), Color(0xFFFF4500))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Text("QR", color = Color(0xAAFFFFFF), fontSize = 9.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Banner Carousel (from admin) ──────────────────
                        com.gymapp.android.ui.screens.banner.BannerCarousel(
                            banners = banners,
                            modifier = Modifier.fillMaxWidth()
                        )


                        // ── Quick Actions ───────────────────────────────────
                        Text("Thao tác nhanh", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Color(0xFF1A1A1A))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            @Composable
                            fun UserActionCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isPrimary: Boolean = false, onClick: () -> Unit) {
                                Card(
                                    onClick = onClick,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isPrimary) Color.Transparent else Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isPrimary) 0.dp else 2.dp)
                                ) {
                                    Box(
                                        modifier = if (isPrimary)
                                            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                                .background(Brush.linearGradient(listOf(Color(0xFFFF8C00), Color(0xFFFF4500))))
                                        else Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(7.dp)
                                        ) {
                                            val iconBoxMod = if (isPrimary)
                                                Modifier.size(44.dp).clip(CircleShape).background(Color(0x33FFFFFF))
                                            else
                                                Modifier.size(44.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFFFF8C00), Color(0xFFFF4500))))
                                            Box(modifier = iconBoxMod, contentAlignment = Alignment.Center) {
                                                Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            }
                                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                                color = if (isPrimary) Color.White else Color(0xFF333333),
                                                textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                            UserActionCard("Mua gói", Icons.Default.AddCircle, onClick = onNavigateToPackages)
                            UserActionCard("Thuê PT", Icons.Default.Star) {
                                navController.navigate(BottomNavRoute.PTBooking.route) {
                                    navController.graph.startDestinationRoute?.let { r -> popUpTo(r) { saveState = true } }
                                    launchSingleTop = true; restoreState = true
                                }
                            }
                            UserActionCard("Bắt đầu tập", Icons.Default.FitnessCenter, isPrimary = true, onClick = onNavigateToWorkout)
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // ── Lịch tập sắp tới ─────────────────────────────────
                        val userUpcomingBookings by mainViewModel.userUpcomingBookings.collectAsState()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lịch tập sắp tới", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Color(0xFF1A1A1A))
                            if (userUpcomingBookings.isNotEmpty()) {
                                Text(
                                    "Xem tất cả →",
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF6B2B),
                                    modifier = Modifier.clickable {
                                        navController.navigate(BottomNavRoute.UserBookings.route) {
                                            navController.graph.startDestinationRoute?.let { r -> popUpTo(r) { saveState = true } }
                                            launchSingleTop = true; restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (userUpcomingBookings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.DateRange, null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(36.dp))
                                    Text("Chưa có lịch tập nào", color = Color(0xFF999999), fontSize = 14.sp)
                                    Text(
                                        "Thuê PT để đặt lịch tập ngay!",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFF6B2B),
                                        modifier = Modifier.clickable {
                                            navController.navigate(BottomNavRoute.PTBooking.route) {
                                                navController.graph.startDestinationRoute?.let { r -> popUpTo(r) { saveState = true } }
                                                launchSingleTop = true; restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                userUpcomingBookings.forEach { booking ->
                                    UpcomingBookingCard(booking = booking)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    } else if (userRole == "PT") {
                        // ── Banner Carousel (from admin) - PT ─────────────
                        com.gymapp.android.ui.screens.banner.BannerCarousel(
                            banners = banners,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Stat Cards ─────────────────────────────────────
                        val orangeGrad = Brush.linearGradient(
                            listOf(Color(0xFFFF8C00), Color(0xFFFF4500)),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                        val greenGrad = Brush.linearGradient(
                            listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Card 1: Ca hôm nay
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(orangeGrad)
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                                Icon(
                                    Icons.Default.Timer, null,
                                    tint = Color(0x1AFFFFFF),
                                    modifier = Modifier.size(70.dp).align(Alignment.CenterEnd)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Ca hôm nay", color = Color(0xBBFFFFFF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        ptStats.todaySessions.toString(),
                                        fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                                        lineHeight = 44.sp
                                    )
                                    Text("buổi", color = Color(0xAAFFFFFF), fontSize = 11.sp)
                                }
                            }
                            // Card 2: Khách Active
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(greenGrad)
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                                Icon(
                                    Icons.Default.FitnessCenter, null,
                                    tint = Color(0x1AFFFFFF),
                                    modifier = Modifier.size(70.dp).align(Alignment.CenterEnd)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Khách Active", color = Color(0xBBFFFFFF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        ptStats.activeClients.toString(),
                                        fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                                        lineHeight = 44.sp
                                    )
                                    Text("người", color = Color(0xAAFFFFFF), fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // ── Quick Actions 2×2 ───────────────────────────────
                        Text("Thao tác nhanh", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Color(0xFFF2F2F2))
                        Spacer(modifier = Modifier.height(12.dp))

                        @Composable
                        fun PtActionCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
                            Card(
                                onClick = onClick,
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2E))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2A1508)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, null, tint = Color(0xFFFF6B2B), modifier = Modifier.size(22.dp))
                                    }
                                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF2F2F2), textAlign = TextAlign.Center)
                                }
                            }
                        }

                        // Row 1
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                PtActionCard("Lịch hẹn", Icons.Default.Timer) {
                                    navController.navigate(BottomNavRoute.PtQueue.route) {
                                        navController.graph.startDestinationRoute?.let { r -> popUpTo(r) { saveState = true } }
                                        launchSingleTop = true; restoreState = true
                                    }
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                PtActionCard("Clients", Icons.Default.FitnessCenter) {
                                    navController.navigate(BottomNavRoute.PtClients.route) {
                                        navController.graph.startDestinationRoute?.let { r -> popUpTo(r) { saveState = true } }
                                        launchSingleTop = true; restoreState = true
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        // Row 2
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                PtActionCard("Mua gói HV", Icons.Default.AddCircle, onNavigateToPackages)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                PtActionCard("Mã QR vào", Icons.Default.DateRange, onNavigateToQrDisplay)
                            }
                        }


                        Spacer(modifier = Modifier.height(22.dp))

                        // ── Weekly Bar Chart ────────────────────────────────
                        Text("Ca dạy trong tuần", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Color(0xFF1A1A1A))
                        Spacer(modifier = Modifier.height(12.dp))
                        val weekDays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                        val weekData = listOf(3f, 5f, 2f, 6f, 4f, 7f, 1f)
                        val maxVal = weekData.max()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                weekDays.forEachIndexed { idx, day ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom
                                    ) {
                                        val fraction = weekData[idx] / maxVal
                                        val isToday = idx == 3
                                        val barHeight = (fraction * 80).dp
                                        val minHeight = 10.dp
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height(if (barHeight < minHeight) minHeight else barHeight)
                                                .clip(CircleShape) // full rounded
                                                .background(
                                                    if (isToday)
                                                        Brush.verticalGradient(listOf(Color(0xFFFF8C00), Color(0xFFFF4500)))
                                                    else
                                                        Brush.verticalGradient(listOf(Color(0xFFFFD0A0), Color(0xFFFFBB80)))
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            day, fontSize = 10.sp,
                                            color = if (isToday) Color(0xFFFF5722) else Color(0xFF999999),
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ── Upcoming Bookings ───────────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lịch sắp tới", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            if (ptStats.upcomingBookings.isNotEmpty()) {
                                Text(
                                    "${ptStats.upcomingBookings.size} buổi",
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF5722),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (ptStats.upcomingBookings.isEmpty()) {
                            // ── Empty State with CTA ─────────────────────────
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF8)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp, horizontal = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Stacked circles illustration
                                    Box(contentAlignment = Alignment.Center) {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFF3E0))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFFFF8C00), Color(0xFFFF4500)),
                                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Timer, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                    Text(
                                        "Hôm nay thảnh thơi! ☀️",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = Color(0xFF1A1A1A)
                                    )
                                    Text(
                                        "Chưa có lịch nào sắp tới.\nHãy liên hệ học viên để đặt buổi mới nhé!",
                                        fontSize = 13.sp,
                                        color = Color(0xFF888888),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                    // CTA Button
                                    Button(
                                        onClick = {
                                            navController.navigate(BottomNavRoute.PtClients.route) {
                                                navController.graph.startDestinationRoute?.let { r -> popUpTo(r) { saveState = true } }
                                                launchSingleTop = true; restoreState = true
                                            }
                                        },
                                        modifier = Modifier.height(44.dp),
                                        shape = RoundedCornerShape(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(50.dp))
                                                .background(Brush.horizontalGradient(listOf(Color(0xFFFF8C00), Color(0xFFFF4500))))
                                                .padding(horizontal = 28.dp, vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "Xem danh sách clients →",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            ptStats.upcomingBookings.forEach { booking ->
                                val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                    .format(booking.scheduledAt)
                                val dateStr = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                                    .format(booking.scheduledAt)
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Time badge
                                        androidx.compose.foundation.layout.Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFFF3E0), androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(timeStr, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF5722))
                                                Text(dateStr, fontSize = 11.sp, color = Color(0xFFFF7043))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        // Divider line
                                        androidx.compose.foundation.layout.Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(40.dp)
                                                .background(Color(0xFFFF5722), androidx.compose.foundation.shape.RoundedCornerShape(1.dp))
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                booking.userName ?: "Học viên",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF1A1A1A)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "Buổi tập cá nhân",
                                                color = Color.Gray,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = Color(0xFFE0E0E0),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (userRole == "ADMIN") {
                        Text("Tổng quan Hệ Thống", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                onClick = onNavigateToCheckinLog,
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Check-in", color = Color.Gray, fontSize = 13.sp)
                                    Text(adminStats?.totalCheckins?.toString() ?: "...", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                                    Text("Xem lịch sử ›", fontSize = 11.sp, color = Color(0xFF1565C0))
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Tổng Thành viên", color = Color.Gray, fontSize = 13.sp)
                                    Text(adminStats?.newRegistrations?.toString() ?: "...", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
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
            composable(BottomNavRoute.PtSchedule.route) {
                // Get the PT's user ID from MainViewModel
                val ptId by mainViewModel.currentUserId.collectAsState()
                com.gymapp.android.ui.screens.pt.PtScheduleManagementScreen(
                    currentPtId = ptId ?: ""
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
                    onNavigateToHistory = { navController.navigate("payment_history") },
                    onNavigateToWorkoutSchedule = onNavigateToWorkoutScheduleSettings
                )
            }
            composable(route = "payment_history") {
                com.gymapp.android.ui.screens.pt.PaymentHistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
    } // end GlowBackground
}

// ── Upcoming Booking Card ──────────────────────────────────────────────────────
@Composable
fun UpcomingBookingCard(booking: com.gymapp.android.data.remote.api.BookingDto) {
    val formattedDate = remember(booking.scheduledAt) {
        try {
            val cal = java.util.Calendar.getInstance().apply { time = booking.scheduledAt }
            val daysVI = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
            val dow = daysVI.getOrElse(cal.get(java.util.Calendar.DAY_OF_WEEK) - 1) { "" }
            val fmt = java.text.SimpleDateFormat("HH:mm · dd/MM/yyyy", java.util.Locale.getDefault())
            "$dow, ${fmt.format(booking.scheduledAt)}"
        } catch (e: Exception) { "" }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFFF8C00), Color(0xFFFF4500)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.FitnessCenter, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "PT ${booking.ptName ?: "Personal Trainer"}",
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A)
            )
            Text(formattedDate, fontSize = 13.sp, color = Color(0xFF888888))
            Text(
                "${booking.durationMinutes ?: 60} phút",
                fontSize = 12.sp, color = Color(0xFFFF6B2B)
            )
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFFF0FFF8))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text("Đã xác nhận", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF00A86B))
        }
    }
}
