package com.gymapp.android.ui.screens.membership

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.ui.components.cards.MembershipPackageCard
import com.gymapp.android.ui.screens.membership.event.MembershipListEvent
import com.gymapp.android.ui.screens.membership.viewmodel.MembershipListViewModel

// ── Dark tokens ────────────────────────────────────────────────────────────────
private val MBgPrimary    = Color(0xFF121212)
private val MBgSecondary  = Color(0xFF1C1C1E)
private val MBorderDark   = Color(0xFF2A2A2E)
private val MTprimary     = Color(0xFFF2F2F2)
private val MTsecondary   = Color(0xFF9A9A9E)
private val MOrange       = Color(0xFFFF6B2B)
private val MOrangeGlow   = Color(0xFFFF8C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipPackagesScreen(
    viewModel: MembershipListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPlanDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterOptions = listOf("Tất cả", "1 Chi nhánh", "Toàn chuỗi")
    var selectedPlanId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CardMembership,
                            contentDescription = null,
                            tint = MOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Gói hội viên",
                            color = MTprimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MBgSecondary, CircleShape)
                                .border(1.dp, MBorderDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Trở lại",
                                tint = MTprimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MBgSecondary,
                    titleContentColor = MTprimary
                )
            )
        },
        containerColor = MBgPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ── Filter chips ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = uiState.selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onEvent(MembershipListEvent.OnFilterChanged(filter)) },
                        label = {
                            Text(
                                filter,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MOrange.copy(alpha = 0.2f),
                            selectedLabelColor     = MOrange,
                            containerColor         = MBgSecondary,
                            labelColor             = MTsecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = MOrange.copy(alpha = 0.5f),
                            borderColor = MBorderDark
                        )
                    )
                }
            }

            // ── Content ─────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MOrange)
                    }
                }
                !uiState.error.isNullOrEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(uiState.error!!, color = Color(0xFFEF5350))
                            Button(
                                onClick = { viewModel.onEvent(MembershipListEvent.Refresh) },
                                colors = ButtonDefaults.buttonColors(containerColor = MOrange)
                            ) { Text("Thử lại", color = Color.White) }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(uiState.plans) { index, plan ->
                            val isFeatured = index == 1
                            val isSelected = if (selectedPlanId == null) isFeatured else plan.id == selectedPlanId

                            MembershipPackageCard(
                                plan = plan,
                                isFeatured = isFeatured,
                                isSelected = isSelected,
                                onCardClick = { selectedPlanId = plan.id },
                                onButtonClick = {
                                    onNavigateToPlanDetail(plan.id)
                                    viewModel.onEvent(MembershipListEvent.OnPackageClicked(plan.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
