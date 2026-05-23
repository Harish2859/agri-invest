package com.example.agri_invest_app.ui.investor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.agri_invest_app.data.model.KycStatus
import com.example.agri_invest_app.data.model.User
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    viewModel: InvestorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val accentColor = Color(0xFF1A237E)
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(projectId) {
        viewModel.getProjectDetails(projectId)
        viewModel.fetchUserProfile() // Ensure we have latest balance/KYC
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            state.selectedProject?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Invest in this Field", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = accentColor)
            } else if (state.error != null) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.getProjectDetails(projectId) }) { Text("Retry") }
                }
            } else {
                state.selectedProject?.let { project ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Image Header
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                            AsyncImage(
                                model = project.landImageUrl,
                                contentDescription = "Land Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
                            ) {
                                Surface(
                                    color = accentColor,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = project.cropType ?: "CROP",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = project.title ?: "Untitled Project",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(20.dp)) {
                            // Farmer Info Row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = accentColor.copy(alpha = 0.1f)
                                ) {
                                    Icon(
                                        Icons.Default.Agriculture,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = project.farmerName ?: "Lead Farmer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                    }
                                    Text(text = project.location ?: "Region", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Funding Progress
                            Text(text = "Funding Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val progress = if (project.targetAmount.compareTo(BigDecimal.ZERO) > 0) {
                                project.amountAlreadyRaised.divide(project.targetAmount, 4, RoundingMode.HALF_UP).toFloat()
                            } else 0f
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(accentColor)
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%,.0f", project.amountAlreadyRaised)} raised",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                                Text(
                                    text = "Goal: ₹${String.format(Locale.getDefault(), "%,.0f", project.targetAmount)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Key Highlights Grid
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                HighlightCard(
                                    label = "Min Invest",
                                    value = "₹${project.minInvestmentAmount?.toInt() ?: 0}",
                                    icon = Icons.Default.Info,
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f)
                                )
                                HighlightCard(
                                    label = "Equity",
                                    value = "${project.equityOffered}%",
                                    icon = Icons.Default.Info,
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f)
                                )
                                HighlightCard(
                                    label = "Status",
                                    value = project.status ?: "Active",
                                    icon = Icons.Default.Info,
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // About Section
                            Text(text = "About this Farm", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = project.description ?: "Detailed information about this project is currently being compiled by the regional lead.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.DarkGray,
                                lineHeight = 24.sp
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    if (showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showBottomSheet = false },
                            sheetState = sheetState,
                            containerColor = Color.White,
                            dragHandle = { BottomSheetDefaults.DragHandle() }
                        ) {
                            InvestmentBottomSheetContent(
                                user = state.user,
                                minAmount = project.minInvestmentAmount ?: BigDecimal.ZERO,
                                targetAmount = project.targetAmount,
                                accentColor = accentColor,
                                onConfirm = { amount ->
                                    viewModel.processInvestment(projectId, amount)
                                    showBottomSheet = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightCard(label: String, value: String, icon: ImageVector, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@Composable
fun InvestmentBottomSheetContent(
    user: User?,
    minAmount: BigDecimal,
    targetAmount: BigDecimal,
    accentColor: Color,
    onConfirm: (BigDecimal) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
    
    val isMinAmountMet = amount >= minAmount
    val hasSufficientFunds = user != null && (user.walletBalance ?: BigDecimal.ZERO) >= amount
    // FIX: Use effectiveKycStatus to unlock investment for legacy Investors
    val isKycApproved = user?.effectiveKycStatus == KycStatus.APPROVED
    
    val isEligibleToInvest = isMinAmountMet && hasSufficientFunds && isKycApproved
    
    val ownership = if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
        amount.multiply(BigDecimal("100")).divide(targetAmount, 4, RoundingMode.HALF_UP).toDouble()
    } else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(text = "Confirm Investment", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Enter the amount you wish to invest in this field.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { if (it.length <= 10) amountText = it },
            label = { Text("Investment Amount") },
            prefix = { Text("₹") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = amountText.isNotEmpty() && (!isMinAmountMet || !hasSufficientFunds),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                focusedLabelColor = accentColor
            )
        )

        if (amountText.isNotEmpty()) {
            if (!isMinAmountMet) {
                Text(
                    text = "Minimum investment required is ₹${minAmount.toInt()}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            } else if (!hasSufficientFunds) {
                Text(
                    text = "Insufficient wallet balance (Available: ₹${user?.walletBalance ?: 0})",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isEligibleToInvest && amount > BigDecimal.ZERO) {
            Surface(
                color = accentColor.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Projected Stake", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = "${String.format("%.4f", ownership)}% ownership of the crop yield",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onConfirm(amount) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = isEligibleToInvest && amount > BigDecimal.ZERO,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEligibleToInvest) accentColor else Color.Gray
            )
        ) {
            Text(
                text = when {
                    !isKycApproved -> "KYC Approval Required"
                    amountText.isEmpty() -> "Enter Amount"
                    !isMinAmountMet -> "Below Minimum"
                    !hasSufficientFunds -> "Insufficient Balance"
                    else -> "Deploy Capital"
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}
