package com.example.agri_invest_app.ui.farmer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneListScreen(
    projectId: Long,
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val project = state.myProjects.find { it.id == projectId }
    val milestones = state.projectMilestones[projectId] ?: emptyList()
    val snackbarHostState = remember { SnackbarHostState() }

    val onRefresh = {
        viewModel.fetchMilestones(projectId)
        viewModel.loadDashboardData()
    }

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

    LaunchedEffect(projectId) {
        onRefresh()
    }

    var selectedMilestoneId by remember { mutableLongStateOf(-1L) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && selectedMilestoneId != -1L) {
            viewModel.submitMilestoneProof(context, selectedMilestoneId, uri, projectId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Milestones & Releases") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            project?.let { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(p.title ?: "Project", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Funding Raised: ₹${p.amountAlreadyRaised} / ₹${p.targetAmount}", style = MaterialTheme.typography.bodySmall)
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Released", style = MaterialTheme.typography.labelSmall)
                                Text("₹${p.releasedToFarmer}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                // Logic: Use project withdrawable if present, else use global aggregated wallet
                                val displayWithdrawable = if (p.withdrawableBalance > BigDecimal.ZERO) p.withdrawableBalance else state.withdrawableBalance
                                Text("Available to Withdraw", style = MaterialTheme.typography.labelSmall)
                                Text("₹$displayWithdrawable", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Status: ${p.status}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading && milestones.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (milestones.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No milestones found.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRefresh) {
                            Text("Refresh Now")
                        }
                    }
                } else {
                    // Find the first milestone that isn't completed to enable it
                    val firstIncompleteIndex = milestones.indexOfFirst { 
                        !it.status.contains("COMPLETED", ignoreCase = true) 
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(milestones) { index, milestone ->
                            // A milestone is enabled only if it is the first incomplete one
                            val isEnabled = index == firstIncompleteIndex

                            MilestoneItem(
                                milestone = milestone,
                                enabled = isEnabled,
                                onUploadClick = { 
                                    selectedMilestoneId = milestone.id
                                    galleryLauncher.launch("image/*") 
                                }
                            )
                        }
                    }
                }
                
                if (state.isLoading && milestones.isNotEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                }
            }
        }
    }
}
