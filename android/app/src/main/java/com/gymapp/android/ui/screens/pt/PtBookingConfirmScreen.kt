package com.gymapp.android.ui.screens.pt

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gymapp.android.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PtBookingConfirmScreen(
    onNavigateBack: () -> Unit,
    onBookingSuccess: (String, String) -> Unit, // URL and BookingId
    viewModel: PtBookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ptDetail by viewModel.ptDetail.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val selectedSlotIds by viewModel.selectedSlotIds.collectAsState()
    
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("XÃ¡c nháº­n Ä‘áº·t lá»‹ch", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trá»Ÿ vá»", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { viewModel.confirmBatchBooking() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = uiState !is PtBookingUiState.Loading
                    ) {
                        if (uiState is PtBookingUiState.Loading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text("XÃ¡c nháº­n & Thanh toÃ¡n", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Lá»‹ch tá»± há»§y sau 15 phÃºt náº¿u chÆ°a thanh toÃ¡n",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PT Info Row
            ptDetail?.let { pt ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pt.avatarUrl.isNullOrBlank()) {
                        AvatarBubble(name = pt.fullName, modifier = Modifier.size(48.dp))
                    } else {
                        AsyncImage(
                            model = pt.avatarUrl,
                            contentDescription = pt.fullName,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(pt.fullName, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
                        Text(pt.specializations?.joinToString(" Â· ") ?: "Personal Trainer", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Booking Confirm Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConfirmRow(label = "NgÃ y", value = SimpleDateFormat("EE, dd/MM/yyyy", Locale("vi", "VN")).format(selectedDate))
                ConfirmRow(label = "Giá»", value = "Khung giá» Ä‘Ã£ chá»n") // Ideally fetch accurate slot string
                ConfirmRow(label = "Thá»i lÆ°á»£ng", value = "60 phÃºt")
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tá»•ng", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val slotCount = selectedSlotIds.size.coerceAtLeast(1)
                    val totalPrice = (ptDetail?.price ?: 0.0) * slotCount
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            currencyFormat.format(totalPrice),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (slotCount > 1) {
                            Text(
                                "${slotCount} buá»•i Ã— ${currencyFormat.format(ptDetail?.price ?: 0.0)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                ConfirmRow(label = "Qua", value = if (selectedProvider == "VNPAY") "VNPay" else "VÃ­ MoMo")
            }

            // Refund Policy Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("ChÃ­nh sÃ¡ch hoÃ n tiá»n", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Text("â€¢ Há»§y > 24h â†’ hoÃ n 100%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("â€¢ Há»§y < 24h â†’ hoÃ n 50%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("â€¢ Há»§y < 2h â†’ khÃ´ng hoÃ n", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            if (uiState is PtBookingUiState.Error) {
                val msg = (uiState as PtBookingUiState.Error).message
                if (msg.contains("NO_ACTIVE_MEMBERSHIP")) {
                    Toast.makeText(context, "Báº¡n chÆ°a cÃ³ gÃ³i há»™i viÃªn", Toast.LENGTH_LONG).show()
                } else if (msg.contains("SLOT_ALREADY_BOOKED")) {
                    Toast.makeText(context, "Slot vá»«a bá»‹ Ä‘áº·t bá»Ÿi ngÆ°á»i khÃ¡c", Toast.LENGTH_LONG).show()
                    LaunchedEffect(Unit) { onNavigateBack() }
                } else {
                    Text(
                        msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Single booking success
    if (uiState is PtBookingUiState.BookingSuccess) {
        val response = (uiState as PtBookingUiState.BookingSuccess).response
        LaunchedEffect(response) {
            onBookingSuccess(response.paymentUrl ?: "", response.bookingId)
            viewModel.resetUiState()
        }
    }

    // Batch booking success
    if (uiState is PtBookingUiState.BatchBookingSuccess) {
        val response = (uiState as PtBookingUiState.BatchBookingSuccess).response
        LaunchedEffect(response) {
            onBookingSuccess(response.paymentUrl ?: "", response.bookingIds.firstOrNull() ?: "")
            viewModel.resetUiState()
        }
    }
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}
