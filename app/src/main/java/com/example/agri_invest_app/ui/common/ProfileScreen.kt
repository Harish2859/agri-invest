package com.example.agri_invest_app.ui.common

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
import com.example.agri_invest_app.data.model.User
import com.example.agri_invest_app.data.network.RetrofitClient
import java.util.Locale

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToTransactions: () -> Unit = {}
) {
    var user by remember { mutableStateOf<User?>(null) }
    var farmerProjects by remember { mutableStateOf<List<FarmProject>>(emptyList()) }
    var investorPortfolio by remember { mutableStateOf<InvestorPortfolio?>(null) }
    var leadMetrics by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        try {
            val response = RetrofitClient.authService.getProfile()
            if (response.isSuccessful) {
                val u = response.body()
                user = u
                u?.let {
                    when (it.role.uppercase()) {
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
                }
            }
        } catch (e: Exception) {
            user = null
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        ProfileShimmer()
    } else if (user != null) {
        val roleColor = when (user!!.role.uppercase()) {
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
                ProfileHeader(user!!, roleColor)
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                when (user!!.role.uppercase()) {
                    "FARMER" -> FarmerContent(user!!, farmerProjects, roleColor)
                    "INVESTOR" -> InvestorContent(user!!, investorPortfolio, roleColor)
                    "VILLAGE_LEAD", "LEAD" -> LeadContent(user!!, leadMetrics, roleColor)
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
                        if (user!!.role.uppercase() == "VILLAGE_LEAD" || user!!.role.uppercase() == "LEAD") {
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
            if (user.verified) {
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

        Text(user.fullName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(user.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

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
                    text = user.role.uppercase(),
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
        if (user.verified) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verified Vendor • KYC Approved", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val activeCount = projects.count { it.status == "ACTIVE" || it.status == "FUNDED" || it.status == "FUNDING" }
            val lifetimeEarnings = projects.sumOf { it.releasedToFarmer.toDouble() }
            
            Box(modifier = Modifier.weight(1f)) {
                MetricCard("Active Projects", activeCount.toString(), Icons.Default.Agriculture, accentColor)
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricCard("Earnings", "₹${String.format(Locale.getDefault(), "%.0f", lifetimeEarnings)}", Icons.Default.Payments, accentColor)
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
            Text("Portfolio Worth", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Wallet Cash", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("₹${portfolio?.summary?.walletBalance ?: 0.0}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Invested Capital", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("₹${portfolio?.summary?.totalPortfolioValue ?: 0.0}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total ROI", style = MaterialTheme.typography.labelSmall)
                Text("+12.5%", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
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
                     Text("Impact", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                     Text("${portfolio?.summary?.impactFarmersHelped ?: 0} Farmers", fontWeight = FontWeight.Bold, color = accentColor)
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
                    Text("Regional Supervisor - Zone 4", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
            DetailRow("Total Commission", "₹${user.walletBalance}", Icons.Default.AccountBalanceWallet)
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
