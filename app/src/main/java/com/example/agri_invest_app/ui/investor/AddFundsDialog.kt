package com.example.agri_invest_app.ui.investor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddFundsDialog(
    onDismiss: () -> Unit,
    onConfirmDeposit: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fund Apps Wallet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Enter the amount you wish to transfer via linked external banking source (UPI/NetBanking).",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { 
                        amountText = it
                        isError = false 
                    },
                    label = { Text("Amount (₹)") },
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        "Please enter a valid amount greater than zero.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull()
                    if (parsedAmount != null && parsedAmount > 0.0) {
                        onConfirmDeposit(parsedAmount)
                        onDismiss()
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Proceed to Pay")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
