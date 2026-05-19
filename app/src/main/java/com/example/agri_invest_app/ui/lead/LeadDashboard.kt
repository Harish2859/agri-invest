package com.example.agri_invest_app.ui.lead

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.data.model.Milestone
import com.example.agri_invest_app.data.model.User
import com.example.agri_invest_app.ui.common.ShimmerProjectItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDashboard(viewModel: LeadViewModel) {
    val state by viewModel.state.collectAsState()
    val leadAccent = Color(0xFF006064)
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Governance Console", fontWeight = FontWeight.Bold)
                        Text("Regional supervisor - Zone 4", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LeadMetricCard("KYC", state.pendingUsers.size.toString(), Icons.Default.People, leadAccent)
                }
                Box(modifier = Modifier.weight(1f)) {
                    LeadMetricCard("Projects", state.pendingProjects.size.toString(), Icons.Default.Agriculture, leadAccent)
                }
                Box(modifier = Modifier.weight(1f)) {
                    LeadMetricCard("Milestones", state.pendingMilestones.size.toString(), Icons.AutoMirrored.Filled.FactCheck, leadAccent)
                }
            }

            // Commission Card
            LeadCommissionCard(balance = state.walletBalance.toDouble(), accentColor = leadAccent)

            // Tabs for Pending Items
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = leadAccent,
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Users (${state.pendingUsers.size})", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Projects (${state.pendingProjects.size})", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Milestones (${state.pendingMilestones.size})", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                    Text("Earnings", modifier = Modifier.padding(16.dp))
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && state.pendingUsers.isEmpty() && state.pendingProjects.isEmpty() && state.pendingMilestones.isEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(5) { ShimmerProjectItem() }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (selectedTab) {
                            0 -> items(state.pendingUsers) { user ->
                                PendingUserItem(user, onVerify = { viewModel.verifyUser(user.id!!) })
                            }
                            1 -> items(state.pendingProjects) { project ->
                                PendingProjectItem(project, onApprove = { viewModel.approveProject(project.id) })
                            }
                            2 -> items(state.pendingMilestones) { milestone ->
                                PendingMilestoneItem(milestone, onVerify = { viewModel.approveMilestone(milestone.id) })
                            }
                            3 -> items(state.transactions) { tx ->
                                CommissionTransactionItem(tx, leadAccent)
                            }
                        }
                        
                        if (isEmpty(selectedTab, state)) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                        Text("No pending items in this category", color = Color.Gray)
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

private fun isEmpty(tab: Int, state: LeadDashboardState): Boolean {
    return when(tab) {
        0 -> state.pendingUsers.isEmpty()
        1 -> state.pendingProjects.isEmpty()
        2 -> state.pendingMilestones.isEmpty()
        3 -> state.transactions.isEmpty()
        else -> true
    }
}

@Composable
fun LeadMetricCard(label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun LeadCommissionCard(balance: Double, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Total Commissions Earned", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                }
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.2f", balance)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Future: Withdraw */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Withdraw to Bank", color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PendingUserItem(user: User, onVerify: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color(0xFFE0F2F1)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(12.dp), tint = Color(0xFF006064))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.fullName, fontWeight = FontWeight.Bold)
                    Text(user.role, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Verification Documents", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Aadhaar: ${user.aadhaarNo ?: "Not Provided"}", color = Color.Gray)
                
                user.kycDocumentUrl?.let { url ->
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = url,
                        contentDescription = "KYC Doc",
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { /* Future: Reject */ }, modifier = Modifier.weight(1f)) {
                        Text("Reject", color = Color.Red)
                    }
                    Button(onClick = onVerify, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064))) {
                        Text("Approve KYC")
                    }
                }
            }
        }
    }
}

@Composable
fun PendingProjectItem(project: FarmProject, onApprove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(project.title ?: "Untitled Project", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("By: ${project.farmerName}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Target", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("₹${project.targetAmount}", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Location", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(project.location ?: "N/A", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onApprove,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064))
            ) {
                Text("Approve Field for Investment")
            }
        }
    }
}

@Composable
fun PendingMilestoneItem(milestone: Milestone, onVerify: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(milestone.title ?: "Milestone Proof", fontWeight = FontWeight.Bold)
                Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        "Release: ${milestone.releasePercentage}%",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE65100)
                    )
                }
            }
            Text("Project ID: ${milestone.projectId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            
            milestone.proofImageUrl?.let { url ->
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Milestone Proof",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onVerify,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064))
            ) {
                Text("Verify & Release Funds")
            }
        }
    }
}

@Composable
fun CommissionTransactionItem(tx: com.example.agri_invest_app.data.model.Transaction, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = accentColor.copy(alpha = 0.1f)) {
            Icon(Icons.Default.Add, contentDescription = null, tint = accentColor, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.description, fontWeight = FontWeight.Medium)
            Text(tx.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Text("+ ₹${tx.amount}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
    }
}
