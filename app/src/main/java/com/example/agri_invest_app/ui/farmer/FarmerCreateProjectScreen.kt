package com.example.agri_invest_app.ui.farmer

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.agri_invest_app.data.model.ProjectCreateRequest
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerCreateProjectScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit,
    onProjectCreated: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val projectCreated by viewModel.projectCreated.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) }

    LaunchedEffect(projectCreated) {
        if (projectCreated) {
            onProjectCreated()
            viewModel.resetProjectCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Project - Step $currentStep of 3") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { currentStep / 3f },
                modifier = Modifier.fillMaxWidth()
            )

            Crossfade(targetState = currentStep, label = "FormStep") { step ->
                when (step) {
                    1 -> StepBasicInfo(formState, viewModel)
                    2 -> StepFinancials(formState, viewModel)
                    3 -> StepDetails(formState, viewModel)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (currentStep < 3) {
                        currentStep++
                    } else {
                        viewModel.createProject()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isStepValid(currentStep, formState)
            ) {
                Text(if (currentStep < 3) "Next" else "Submit for Review")
            }
        }
    }
}

@Composable
fun StepBasicInfo(state: ProjectCreateRequest, viewModel: FarmerViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Basic Information", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(
            value = state.title,
            onValueChange = { newValue -> viewModel.updateForm { it.copy(title = newValue) } },
            label = { Text("Project Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.cropType,
            onValueChange = { newValue -> viewModel.updateForm { it.copy(cropType = newValue) } },
            label = { Text("Crop Type (e.g. Rice, Wheat)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.location,
            onValueChange = { newValue -> viewModel.updateForm { it.copy(location = newValue) } },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StepFinancials(state: ProjectCreateRequest, viewModel: FarmerViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Financial Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = if (state.targetAmount.compareTo(BigDecimal.ZERO) == 0) "" else state.targetAmount.toString(),
            onValueChange = { newValue -> 
                val v = newValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
                viewModel.updateForm { it.copy(targetAmount = v) } 
            },
            label = { Text("Target Funding Amount (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = if (state.minInvestmentAmount.compareTo(BigDecimal.ZERO) == 0) "" else state.minInvestmentAmount.toString(),
            onValueChange = { newValue -> 
                val v = newValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
                viewModel.updateForm { it.copy(minInvestmentAmount = v) } 
            },
            label = { Text("Minimum Investment (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = if (state.equityOffered == 0.0) "" else state.equityOffered.toString(),
            onValueChange = { newValue -> 
                val v = newValue.toDoubleOrNull() ?: 0.0
                viewModel.updateForm { it.copy(equityOffered = v) } 
            },
            label = { Text("Equity Offered (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StepDetails(state: ProjectCreateRequest, viewModel: FarmerViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Project Story & Media", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = state.description,
            onValueChange = { newValue -> viewModel.updateForm { it.copy(description = newValue) } },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            minLines = 5
        )

        OutlinedTextField(
            value = state.landImageUrl ?: "",
            onValueChange = { newValue -> viewModel.updateForm { it.copy(landImageUrl = newValue) } },
            label = { Text("Land Image URL (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun isStepValid(step: Int, state: ProjectCreateRequest): Boolean {
    return when (step) {
        1 -> state.title.isNotBlank() && state.cropType.isNotBlank() && state.location.isNotBlank()
        2 -> state.targetAmount.compareTo(BigDecimal.ZERO) > 0 && 
             state.minInvestmentAmount.compareTo(BigDecimal.ZERO) > 0 && 
             state.minInvestmentAmount <= state.targetAmount && 
             state.equityOffered > 0
        3 -> state.description.length > 20
        else -> false
    }
}
