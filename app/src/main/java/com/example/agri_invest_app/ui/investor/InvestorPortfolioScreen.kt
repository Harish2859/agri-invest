package com.example.agri_invest_app.ui.investor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agri_invest_app.data.model.InvestmentDetail
import com.example.agri_invest_app.ui.common.ShimmerProjectItem
import com.example.agri_invest_app.ui.common.shimmerEffect
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestorPortfolioScreen(viewModel: InvestorViewModel) {
    val state by viewModel.state.collectAsState()
    val accentColor = Color(0xFF1A237E)

    LaunchedEffect(Unit) {
        viewModel.getInvestorPortfolio()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Portfolio", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.getInvestorPortfolio() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading && state.portfolio == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { PortfolioSummaryShimmer() }
                    items(3) { ShimmerProjectItem() }
                }
            } else if (state.error != null && state.portfolio == null) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.getInvestorPortfolio() }) { Text("Retry") }
                }
            } else {
                state.portfolio?.let { portfolio ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val summary = portfolio.summary
                        if (summary != null) {
                            item {
                                PortfolioValueCard(
                                    balance = summary.walletBalance,
                                    totalValue = summary.totalPortfolioValue,
                                    accentColor = accentColor
                                )
                            }

                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        PortfolioMetricCard(
                                            label = "Farmers Helped",
                                            value = summary.impactFarmersHelped.toString(),
                                            icon = Icons.Default.TrendingUp,
                                            color = accentColor
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        PortfolioMetricCard(
                                            label = "Active Projects",
                                            value = summary.activeInvestmentsCount.toString(),
                                            icon = Icons.Default.AccountBalanceWallet,
                                            color = accentColor
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your Investments",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        val investments = portfolio.investments
                        if (!investments.isNullOrEmpty()) {
                            items(investments) { investment ->
                                PortfolioInvestmentItem(investment, accentColor)
                            }
                        } else {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Wallet, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("No investments yet.", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioSummaryShimmer() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Shimmer for PortfolioValueCard
        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().shimmerEffect())
        }
        
        // Shimmer for Metric Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f).height(100.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().shimmerEffect())
            }
            Card(
                modifier = Modifier.weight(1f).height(100.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().shimmerEffect())
            }
        }
    }
}

@Composable
fun PortfolioValueCard(balance: Double, totalValue: Double, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.8f))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(text = "Net Portfolio Value", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.2f", totalValue)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Available Cash", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        Text(text = "₹${String.format(Locale.getDefault(), "%,.2f", balance)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = { /* Future: Deposit */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Top Up", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioMetricCard(label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun PortfolioInvestmentItem(investment: InvestmentDetail, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = investment.projectTitle ?: "Unknown Project",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = investment.crop ?: "Agriculture",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                StatusChip(status = if (investment.settled) "SETTLED" else (investment.currentStatus ?: "PENDING"))
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Invested", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", investment.amountInvested)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    val isProfit = investment.settled && investment.finalReturn != null && investment.finalReturn > investment.amountInvested
                    val returnLabel = if (investment.settled) "Final Return" else "Exp. Return"
                    val returnVal = if (investment.settled) investment.finalReturn else investment.expectedReturn
                    
                    Text(text = returnLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", returnVal ?: 0.0)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) Color(0xFF2E7D32) else Color.Unspecified
                    )
                }
            }
            
            if (investment.settled && investment.finalReturn != null) {
                val roi = ((investment.finalReturn - investment.amountInvested) / investment.amountInvested) * 100
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", roi)}% Overall ROI",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "SETTLED" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "ACTIVE" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "FUNDED" -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        else -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
