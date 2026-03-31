package com.gymapp.android.ui.screens.membership

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.gymapp.android.ui.components.cards.MembershipPackageCard
import com.gymapp.android.ui.screens.membership.event.MembershipListEvent
import com.gymapp.android.ui.screens.membership.viewmodel.MembershipListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipPackagesScreen(
    viewModel: MembershipListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPlanDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterOptions = listOf("Tất cả", "1 Chi nhánh", "Toàn chuỗi")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gói hội viên", color = Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Trở lại", tint = Color(0xFF1A1A1A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Filters
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
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFCCBC),
                            selectedLabelColor = Color(0xFFE65100),
                            containerColor = Color(0xFFF0F0F0),
                            labelColor = Color(0xFF666666)
                        ),
                        border = null
                    )
                }
            }

            // Results
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF5722))
                }
            } else if (!uiState.error.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.error!!, color = Color(0xFFE53935))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.onEvent(MembershipListEvent.Refresh) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(uiState.plans) { index, plan ->
                        // Simulate "Featured" for the second card (just for mockup purpose based on wireframe)
                        val isFeatured = index == 1
                        MembershipPackageCard(
                            plan = plan,
                            isFeatured = isFeatured,
                            onPlanClick = { planId ->
                                onNavigateToPlanDetail(planId)
                                viewModel.onEvent(MembershipListEvent.OnPackageClicked(planId))
                            }
                        )
                    }
                }
            }
        }
    }
}
