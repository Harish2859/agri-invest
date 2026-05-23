package com.example.agri_invest_app.ui.farmer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.ui.common.ShimmerProjectItem
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboardScreen(
    viewModel: FarmerViewModel,
    onLogout: () -> Unit,
    onCreateProject: () -> Unit,
    onProjectClick: (Long) -> Unit,
    onNavigateToTransactions: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var selectedProjectForWithdraw by remember { mutableStateOf<FarmProject?>(null) }
    var projectToSettle by remember { mutableStateOf<FarmProject?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // KYC File Picker
    val kycLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.submitKyc(it.toString()) }
    }

    // Feedback Handler
    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Auto-refresh on entry
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    selectedProjectForWithdraw?.let { project ->
        WithdrawDialog(
            projectTitle = project.title ?: "Project",
            balance = project.withdrawableBalance,
            onDismiss = { selectedProjectForWithdraw = null },
            onConfirm = { amount, bankDetails ->
                viewModel.withdrawFunds(amount, project.id, bankDetails)
                selectedProjectForWithdraw = null
            }
        )
    }

    projectToSettle?.let { project ->
        SettlementDialog(
            project = project,
            onDismiss = { projectToSettle = null },
            onConfirm = { revenue ->
                viewModel.settleProject(project.id, revenue)
                projectToSettle = null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Farmer Dashboard") },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboardData() }) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.isVerified) {
                FloatingActionButton(onClick = onCreateProject) {
                    Icon(Icons.Default.Add, contentDescription = "Create Project")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Verification Status
            if (!state.isVerified && !state.isLoading) {
                VerificationRequiredCard(onVerifyClick = { kycLauncher.launch("*/*") })
            }

            // 2. The Wallet (Aggregated Balance)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Withdrawable Balance", style = MaterialTheme.typography.labelMedium)
                            val balanceGreaterThanZero = state.withdrawableBalance.compareTo(BigDecimal.ZERO) > 0
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%,.2f", state.withdrawableBalance)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (balanceGreaterThanZero) Color(0xFF2E7D32) else Color.Unspecified
                            )
                        }
                        Button(
                            onClick = { 
                                selectedProjectForWithdraw = state.myProjects.firstOrNull { it.withdrawableBalance.compareTo(BigDecimal.ZERO) > 0 }
                            },
                            enabled = state.withdrawableBalance.compareTo(BigDecimal.ZERO) > 0 && !state.isLoading && state.myProjects.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Withdraw")
                        }
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Lifetime Released:", style = MaterialTheme.typography.bodySmall)
                            Text("₹${String.format(Locale.getDefault(), "%,.2f", state.releasedToFarmer)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = onNavigateToTransactions) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("View History", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // 3. Project List
            Text("My Projects", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            if (state.isLoading && state.myProjects.isEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(3) { ShimmerProjectItem() }
                }
            } else if (state.myProjects.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active projects.", color = Color.Gray)
                        if (state.isVerified) {
                            TextButton(onClick = onCreateProject) {
                                Text("Create your first project")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.myProjects) { project ->
                        FarmerProjectItem(
                            project = project, 
                            onClick = { onProjectClick(project.id) },
                            onWithdraw = { selectedProjectForWithdraw = project },
                            onComplete = { projectToSettle = project }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettlementDialog(
    project: FarmProject,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal) -> Unit
) {
    var revenueText by remember { mutableStateOf("") }
    val revenue = revenueText.toBigDecimalOrNull() ?: BigDecimal.ZERO
    
    // Live Calculations using BigDecimal
    val systemFee = revenue.multiply(BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP)
    val remainingAfterLead = revenue.subtract(systemFee)
    val equityFactor = BigDecimal.valueOf(project.equityOffered).divide(BigDecimal("100"), 4, RoundingMode.HALF_UP)
    val investorShare = remainingAfterLead.multiply(equityFactor).setScale(2, RoundingMode.HALF_UP)
    val farmerTakeHome = remainingAfterLead.subtract(investorShare)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Final Settlement: ${project.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter the total revenue from harvest sales. This will trigger the automated payout distribution.",
                    style = MaterialTheme.typography.bodySmall
                )
                
                OutlinedTextField(
                    value = revenueText,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) revenueText = it },
                    label = { Text("Total Sale Revenue (₹)") },
                    placeholder = { Text("e.g. 50000") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )

                if (revenue.compareTo(BigDecimal.ZERO) > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Payout Preview", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Lead Commission (5%):", style = MaterialTheme.typography.bodySmall)
                                Text("-₹${String.format(Locale.getDefault(), "%,.2f", systemFee)}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Investor Pool (${project.equityOffered}%):", style = MaterialTheme.typography.bodySmall)
                                Text("-₹${String.format(Locale.getDefault(), "%,.2f", investorShare)}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Your Net Profit:", fontWeight = FontWeight.Bold)
                                Text("₹${String.format(Locale.getDefault(), "%,.2f", farmerTakeHome)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(revenue) },
                enabled = revenue.compareTo(BigDecimal.ZERO) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Confirm & Disburse")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WithdrawDialog(
    projectTitle: String,
    balance: BigDecimal, 
    onDismiss: () -> Unit, 
    onConfirm: (BigDecimal, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var bankDetailsText by remember { mutableStateOf("") }
    
    val enteredAmount = amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val isAmountValid = enteredAmount.compareTo(BigDecimal.ZERO) > 0 && enteredAmount <= balance
    val isBankDetailsValid = bankDetailsText.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw: $projectTitle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Available Balance: ₹${String.format(Locale.getDefault(), "%,.2f", balance)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() || it == '.' }) amountText = input 
                    },
                    label = { Text("Amount to Payout") },
                    placeholder = { Text("Max ₹${String.format(Locale.getDefault(), "%.0f", balance)}") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = amountText.isNotEmpty() && !isAmountValid
                )
                
                OutlinedTextField(
                    value = bankDetailsText,
                    onValueChange = { bankDetailsText = it },
                    label = { Text("Bank Details / UPI ID") },
                    placeholder = { Text("e.g. SBI - XXXXXXXXX123") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = bankDetailsText.isNotEmpty() && !isBankDetailsValid
                )
                
                if (amountText.isNotEmpty() && !isAmountValid) {
                    Text(
                        text = if (enteredAmount > balance) "Insufficient funds" else "Invalid amount",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(enteredAmount, bankDetailsText) },
                enabled = isAmountValid && isBankDetailsValid
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
fun FarmerProjectItem(project: FarmProject, onClick: () -> Unit, onWithdraw: () -> Unit, onComplete: () -> Unit) {
    val isClosed = project.status == "COMPLETED" || project.status == "CLOSED"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(project.title ?: "Untitled Project", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                StatusBadge(project.status ?: "PENDING")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isClosed && project.finalFarmerProfit != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Final Net Profit", style = MaterialTheme.typography.labelSmall)
                        Text("₹${String.format(Locale.getDefault(), "%,.2f", project.finalFarmerProfit)}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), style = MaterialTheme.typography.titleMedium)
                    }
                    Icon(Icons.Default.CheckCircle, contentDescription = "Settled", tint = Color(0xFF2E7D32))
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("Project Balance", style = MaterialTheme.typography.labelSmall)
                        Text("₹${String.format(Locale.getDefault(), "%,.2f", project.withdrawableBalance)}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (project.withdrawableBalance.compareTo(BigDecimal.ZERO) > 0) {
                            Button(
                                onClick = onWithdraw,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Withdraw", fontSize = 12.sp)
                            }
                        }
                        
                        if (!isClosed && (project.status == "FULLY_FUNDED" || project.status == "FUNDING_IN_PROGRESS")) {
                            OutlinedButton(
                                onClick = onComplete,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Harvested", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isClosed) "Settlement Date:" else "Total Released:", style = MaterialTheme.typography.labelSmall)
                Text(if (isClosed) "Completed" else "₹${String.format(Locale.getDefault(), "%,.2f", project.releasedToFarmer)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "FULLY_FUNDED", "COMPLETED" -> Color(0xFF2E7D32)
        "FUNDING_IN_PROGRESS" -> Color(0xFF1976D2)
        "APPROVED" -> Color(0xFFFB8C00)
        else -> Color.Gray
    }
    Surface(color = color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun VerificationRequiredCard(onVerifyClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verification Required", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            }
            Text("Complete KYC to unlock fund withdrawal features.", modifier = Modifier.padding(vertical = 8.dp))
            Button(onClick = onVerifyClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Verify Now")
            }
        }
    }
}
