package com.example.agri_invest_app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agri_invest_app.data.model.KycStatus

@Composable
fun KycStatusBanner(
    kycStatus: KycStatus, 
    rejectionReason: String?, 
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (kycStatus) {
                KycStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                KycStatus.SUBMITTED -> Color(0xFFFFF7E6)
                KycStatus.APPROVED -> Color(0xFFF6FFED)
                KycStatus.REJECTED -> Color(0xFFFFF1F0)
            }
        ),
        border = BorderStroke(
            1.dp,
            when (kycStatus) {
                KycStatus.PENDING -> MaterialTheme.colorScheme.outlineVariant
                KycStatus.SUBMITTED -> Color(0xFFFFD591)
                KycStatus.APPROVED -> Color(0xFFB7EB8F)
                KycStatus.REJECTED -> Color(0xFFFFA39E)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (kycStatus) {
                KycStatus.PENDING -> {
                    Text("Identity Verification Required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Please upload government ID to activate investment access.", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = onNavigateToUpload, modifier = Modifier.padding(top = 8.dp), shape = RoundedCornerShape(8.dp)) {
                        Text("Start KYC")
                    }
                }
                KycStatus.SUBMITTED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pending, contentDescription = null, tint = Color(0xFFE6A23C))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verification In Progress", color = Color(0xFFE6A23C), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("Our regional supervisor lead is vetting your documents. This usually takes under 24 hours.", style = MaterialTheme.typography.bodyMedium)
                }
                KycStatus.APPROVED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF67C23A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Account Fully Verified", color = Color(0xFF67C23A), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("Your account profile is active. Equity share order portals are enabled.", style = MaterialTheme.typography.bodyMedium)
                }
                KycStatus.REJECTED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFA30000))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verification Rejected", color = Color(0xFFA30000), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("Reason: ${rejectionReason ?: "Invalid or blurred document snapshot."}", style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = onNavigateToUpload, 
                        modifier = Modifier.padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA30000)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Re-submit Documents")
                    }
                }
            }
        }
    }
}
