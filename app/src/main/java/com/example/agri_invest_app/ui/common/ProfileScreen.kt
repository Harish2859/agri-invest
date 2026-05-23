package com.example.agri_invest_app.ui.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.data.model.InvestorPortfolio
import com.example.agri_invest_app.data.model.KycStatus
import com.example.agri_invest_app.data.model.User
import com.example.agri_invest_app.data.network.RetrofitClient
import com.example.agri_invest_app.ui.investor.AddFundsDialog
import java.math.BigDecimal
import java.util.Locale

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToTransactions: () -> Unit = {},
    onAddFunds: (Double) -> Unit = {},
    onVerifyKyc: (String) -> Unit = {},
    externalUser: User? = null,
    externalIsLoading: Boolean = false,
    externalError: String? = null,
    externalSuccessMessage: String? = null,
    onClearError: () -> Unit = {},
    onClearSuccess: () -> Unit = {}
) {
    var internalUser by remember { mutableStateOf<User?>(null) }
    var farmerProjects by remember { mutableStateOf<List<FarmProject>>(emptyList()) }
    var investorPortfolio by remember { mutableStateOf<InvestorPortfolio?>(null) }
    var leadMetrics by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    
    var isFetchingInternalData by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    var showAddFundsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val user = externalUser ?: internalUser
    val isLoading = if (externalUser != null) externalIsLoading else isFetchingInternalData

    val kycLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onVerifyKyc(it.toString()) }
    }

    // Fetch initial profile if not provided externally
    LaunchedEffect(refreshTrigger, externalUser == null) {
        if (externalUser == null) {
            isFetchingInternalData = true
            try {
                val response = RetrofitClient.authService.getProfile()
                if (response.isSuccessful) {
                    internalUser = response.body()
                }
            } catch (e: Exception) {
                internalUser = null
            } finally {
                isFetchingInternalData = false
            }
        }
    }

    // Secondary data fetch based on user role
    LaunchedEffect(user?.id, user?.role) {
        user?.let { u ->
            try {
                val roleStr = u.role ?: ""
                when (roleStr.uppercase()) {
                    "FARMER" -> {
                        val pResp = RetrofitClient.farmerService.getMyProjects()
                        if (pResp.isSuccessful) farmerProjects = pResp.body() ?: emptyList()
                    }
                    "INVESTOR" -> {
                        val portResp = RetrofitClient.projectService.getInvestorPortfolio()
                        if (portResp.isSuccessful) investorPortfolio = portResp.body()
                    }
                    "VILLAGE_LEAD", "LEAD" -> {
                        val kyc = RetrofitClient.leadService.getPendingUsers()
                        val projs = RetrofitClient.leadService.getPendingProjects()
                        val miles = RetrofitClient.leadService.getPendingMilestones()
                        leadMetrics = mapOf(
                            "kyc" to (kyc.body()?.size ?: 0),
                            "projects" to (projs.body()?.size ?: 0),
                            "milestones" to (miles.body()?.size ?: 0)
                        )
                    }
                }
            } catch (e: Exception) {
                // Silently handle secondary fetch errors
            }
        }
    }

    // Show error snackbar
    LaunchedEffect(externalError) {
        externalError?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    // Show success snackbar
    LaunchedEffect(externalSuccessMessage) {
        externalSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearSuccess()
        }
    }

    if (showAddFundsDialog) {
        AddFundsDialog(
            onDismiss = { showAddFundsDialog = false },
            onConfirmDeposit = { amount ->
                onAddFunds(amount)
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && user == null) {
                ProfileShimmer()
            } else if (user != null) {
                val roleStr = user.role ?: "USER"
                val roleColor = when (roleStr.uppercase()) {
                    "FARMER" -> Color(0xFF2E7D32)
                    "INVESTOR" -> Color(0xFF1A237E)
                    "VILLAGE_LEAD", "LEAD" -> Color(0xFF006064)
                    else -> MaterialTheme.colorScheme.primary
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        ProfileHeader(user, roleColor)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (roleStr.uppercase() == "INVESTOR") {
                        item {
                            ProfileWalletCard(
                                balance = user.walletBalance ?: BigDecimal.ZERO,
                                accentColor = roleColor,
                                onAddFunds = { showAddFundsDialog = true }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    item {
                        // FIX: Use effectiveKycStatus to auto-approve Investors even with stale DB state
                        val status = user.effectiveKycStatus
                        KycStatusBanner(
                            kycStatus = status,
                            rejectionReason = user.kycRejectionReason,
                            onNavigateToUpload = { kycLauncher.launch("*/*") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        when (roleStr.uppercase()) {
                            "FARMER" -> FarmerContent(user, farmerProjects, roleColor)
                            "INVESTOR" -> InvestorContent(user, investorPortfolio, roleColor)
                            "VILLAGE_LEAD", "LEAD" -> LeadContent(user, leadMetrics, roleColor)
                            else -> {}
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Text(
                            "Account Actions",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column {
                                ProfileOptionItem(
                                    title = "Transaction History",
                                    subtitle = "View all your deposits and withdrawals",
                                    icon = Icons.Default.History,
                                    onClick = onNavigateToTransactions
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                                ProfileOptionItem(
                                    title = "App Settings",
                                    subtitle = "Notifications, security, and preferences",
                                    icon = Icons.Default.Settings,
                                    onClick = { /* Future: Settings */ }
                                )
                                if (roleStr.uppercase().contains("LEAD")) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                                    ProfileOptionItem(
                                        title = "Export Transaction Data",
                                        subtitle = "Download CSV for local records",
                                        icon = Icons.Default.FileDownload,
                                        onClick = { /* Future: CSV Export */ }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        Button(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logout", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Failed to load profile.", color = Color.Gray)
                        Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { refreshTrigger++ }) { Text("Retry") }
                            Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                Text("Logout")
                            }
                        }
                    }
                }
            }
            
            if (isLoading && user != null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
fun ProfileWalletCard(balance: BigDecimal, accentColor: Color, onAddFunds: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wallet, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apps Wallet Balance", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹${String.format(Locale.getDefault(), "%,.2f", balance.toDouble())}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddFunds,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = accentColor)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Funds to Wallet", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileHeader(user: User, roleColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = roleColor
            )
            // Use isEffectivelyVerified to show the checkmark for legacy investors
            if (user.isEffectivelyVerified) {
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp).border(2.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Verified",
                        modifier = Modifier.padding(4.dp),
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(user.fullName ?: "User", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(user.email ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = roleColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, roleColor.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(roleColor, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = (user.role ?: "USER").uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = roleColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, icon: ImageVector, color: Color) {
    Column(
        modifier = Modifier
            .background(color.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
        Text(label, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
    }
}

@Composable
fun FarmerContent(user: User, projects: List<FarmProject>, accentColor: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val activeCount = projects.count { it.status == "ACTIVE" || it.status == "FUNDED" || it.status == "FUNDING" }
            val lifetimeEarnings = projects.sumOf { (it.releasedToFarmer ?: BigDecimal.ZERO).toDouble() }
            
            Box(modifier = Modifier.weight(1f)) {
                MetricCard("Active Projects", activeCount.toString(), Icons.Default.Agriculture, accentColor)
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricCard("Earnings", "₹${String.format(Locale.getDefault(), "%,.0f", lifetimeEarnings)}", Icons.Default.Payments, accentColor)
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricCard("Projects", projects.size.toString(), Icons.AutoMirrored.Filled.FactCheck, accentColor)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        GlassCard(accentColor = accentColor) {
            Text("Operational Ledger Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow("Primary Location", projects.firstOrNull()?.location ?: "Not Specified", Icons.Default.LocationOn)
            DetailRow("Focus Crops", projects.mapNotNull { it.cropType }.distinct().joinToString(", ").ifEmpty { "None" }, Icons.Default.Grass)
        }
    }
}

@Composable
fun InvestorContent(user: User, portfolio: InvestorPortfolio?, accentColor: Color) {
    Column {
        GlassCard(accentColor = accentColor) {
            Text("Portfolio Snapshot", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Invested", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("₹${portfolio?.summary?.totalPortfolioValue ?: 0.0}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Impact", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${portfolio?.summary?.impactFarmersHelped ?: 0} Farmers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
             Box(modifier = Modifier.weight(1f)) {
                 GlassCard(accentColor = accentColor) {
                     Text("Risk Profile", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                     Text(portfolio?.riskProfile?.profileType ?: "Balanced", fontWeight = FontWeight.Bold, color = accentColor)
                 }
             }
             Box(modifier = Modifier.weight(1f)) {
                 GlassCard(accentColor = accentColor) {
                     Text("Total ROI", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                     Text("+12.5%", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                 }
             }
        }
    }
}

@Composable
fun LeadContent(user: User, metrics: Map<String, Int>, accentColor: Color) {
    Column {
        GlassCard(accentColor = accentColor) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = accentColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Governance Console", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Regional Supervisor", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Audit Metrics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) { MetricCard("Pending KYC", metrics["kyc"]?.toString() ?: "0", Icons.Default.People, accentColor) }
            Box(modifier = Modifier.weight(1f)) { MetricCard("Pending Proj", metrics["projects"]?.toString() ?: "0", Icons.Default.PendingActions, accentColor) }
            Box(modifier = Modifier.weight(1f)) { MetricCard("Milestones", metrics["milestones"]?.toString() ?: "0", Icons.AutoMirrored.Filled.FactCheck, accentColor) }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GlassCard(accentColor = accentColor) {
            DetailRow("Total Commission", "₹${user.walletBalance ?: BigDecimal.ZERO}", Icons.Default.AccountBalanceWallet)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileOptionItem(title: String, subtitle: String? = null, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { 
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp))
            }
        },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.2f),
                    accentColor.copy(alpha = 0.05f)
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
