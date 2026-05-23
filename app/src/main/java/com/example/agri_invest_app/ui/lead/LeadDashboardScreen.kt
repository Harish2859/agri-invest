package com.example.agri_invest_app.ui.lead

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.data.model.Milestone
import com.example.agri_invest_app.data.model.User
import com.example.agri_invest_app.ui.common.*
import java.math.BigDecimal
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDashboardScreen(viewModel: LeadViewModel, onLogout: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val message by viewModel.message.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("KYC Verification", "Project Approval", "Milestone Proofs")

    val snackbarHostState = remember { SnackbarHostState() }
    
    var userToReject by remember { mutableStateOf<User?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    // Refresh data when Lead enters screen
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Show snackbar for messages from ViewModel
    LaunchedEffect(message) {
        message?.let { 
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    var showWithdrawDialog by remember { mutableStateOf(false) }

    if (userToReject != null) {
        AlertDialog(
            onDismissRequest = { userToReject = null },
            title = { Text("Reject Verification") },
            text = {
                Column {
                    Text("Provide a reason for rejecting ${userToReject?.fullName}'s KYC.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Blurred document, Invalid ID") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyUser(userToReject!!.id!!, false, rejectionReason)
                        userToReject = null
                        rejectionReason = ""
                    },
                    enabled = rejectionReason.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToReject = null; rejectionReason = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lead Governance Dashboard") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Option A: Lead Earnings Banner
            LeadEarningsBanner(
                balance = state.walletBalance,
                onRefresh = { viewModel.loadData() },
                onWithdrawClick = { showWithdrawDialog = true }
            )

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(5) {
                            when (selectedTab) {
                                0 -> ShimmerUserItem()
                                1 -> ShimmerProjectItem()
                                2 -> ShimmerMilestoneItem()
                            }
                        }
                    }
                } else if (state.error != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${state.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.loadData() }) {
                            Text("Retry")
                        }
                    }
                } else {
                    when (selectedTab) {
                        0 -> PendingUsersList(
                            users = state.pendingUsers, 
                            onRefresh = { viewModel.loadData() },
                            onVerify = { viewModel.verifyUser(it, true) },
                            onReject = { userToReject = it }
                        )
                        1 -> PendingProjectsList(state.pendingProjects, { viewModel.loadData() }) { projectId ->
                            viewModel.approveProject(projectId)
                        }
                        2 -> PendingMilestonesList(state.pendingMilestones, { viewModel.loadData() }) { milestoneId ->
                            viewModel.approveMilestone(milestoneId)
                        }
                    }
                }
            }
        }
    }

    if (showWithdrawDialog) {
        WithdrawalDialog(
            currentBalance = state.walletBalance,
            onDismiss = { showWithdrawDialog = false },
            onConfirm = { amount, details ->
                viewModel.processWithdrawal(amount, details)
                showWithdrawDialog = false
            }
        )
    }
}

@Composable
fun LeadEarningsBanner(balance: BigDecimal, onRefresh: () -> Unit, onWithdrawClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Lead Commission Balance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%.2f", balance)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Button(
                onClick = onWithdrawClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Withdraw")
            }
        }
    }
}

@Composable
fun WithdrawalDialog(
    currentBalance: BigDecimal,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var bankDetailsText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw Commission Earnings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Available Balance: ₹$currentBalance", color = Color.Gray)
                
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = bankDetailsText,
                    onValueChange = { bankDetailsText = it },
                    label = { Text("UPI ID or Bank Account Details") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toBigDecimalOrNull()
                    if (amt == null || amt <= BigDecimal.ZERO || amt > currentBalance) {
                        errorMessage = "Invalid amount or insufficient balance"
                    } else if (bankDetailsText.isBlank()) {
                        errorMessage = "Bank details cannot be empty"
                    } else {
                        onConfirm(amt, bankDetailsText)
                    }
                }
            ) {
                Text("Confirm Payout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PendingUsersList(
    users: List<User>, 
    onRefresh: () -> Unit, 
    onVerify: (Long) -> Unit,
    onReject: (User) -> Unit
) {
    if (users.isEmpty()) {
        EmptyStateView("No KYC verifications require your review right now.", onRefresh)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { user ->
                UserVerificationCard(user, onVerify, onReject)
            }
        }
    }
}

@Composable
fun PendingProjectsList(projects: List<FarmProject>, onRefresh: () -> Unit, onApprove: (Long) -> Unit) {
    if (projects.isEmpty()) {
        EmptyStateView("No new projects are currently awaiting approval.", onRefresh)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projects) { project ->
                ProjectApprovalCard(project, onApprove)
            }
        }
    }
}

@Composable
fun PendingMilestonesList(milestones: List<Milestone>, onRefresh: () -> Unit, onApprove: (Long) -> Unit) {
    if (milestones.isEmpty()) {
        EmptyStateView("No pending milestone proofs require your verification right now.", onRefresh)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(milestones) { milestone ->
                MilestoneReviewCard(milestone, onApprove)
            }
        }
    }
}

@Composable
fun UserVerificationCard(user: User, onVerify: (Long) -> Unit, onReject: (User) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = user.fullName ?: "Unknown User", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Email: ${user.email ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Aadhaar: ${user.aadhaarNo ?: "Not Provided"}", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onReject(user) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reject")
                }
                Button(
                    onClick = { user.id?.let { onVerify(it) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve KYC")
                }
            }
        }
    }
}

@Composable
fun ProjectApprovalCard(project: FarmProject, onApprove: (Long) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = project.title ?: "Untitled Project", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Badge { Text(project.status ?: "PENDING") }
            }
            Text(text = "Location: ${project.location ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Target: ₹${project.targetAmount}", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = project.description ?: "No description.", maxLines = 3, style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { onApprove(project.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Approve & Go Live")
            }
        }
    }
}

@Composable
fun MilestoneReviewCard(milestone: Milestone, onApprove: (Long) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = milestone.title ?: "Untitled Milestone", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            val displayAmount = milestone.amountToRelease ?: BigDecimal.ZERO
            Text(
                text = "Release Amount: ₹${String.format(Locale.getDefault(), "%.2f", displayAmount)}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!milestone.proofImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = milestone.proofImageUrl,
                    contentDescription = "Milestone Proof",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No proof image provided.", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { onApprove(milestone.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
            ) {
                Text("Approve Milestone")
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String, onRefresh: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "All Caught Up!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Refresh")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeadEarningsBannerPreview() {
    LeadEarningsBanner(balance = BigDecimal("500.0"), onRefresh = {}, onWithdrawClick = {})
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    EmptyStateView(message = "No pending milestones.", onRefresh = {})
}
