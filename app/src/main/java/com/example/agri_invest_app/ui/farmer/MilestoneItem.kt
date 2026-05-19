package com.example.agri_invest_app.ui.farmer

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agri_invest_app.data.model.Milestone
import java.math.BigDecimal
import java.util.Locale

@Composable
fun MilestoneItem(
    milestone: Milestone,
    enabled: Boolean = true, // NEW: Control if this is the active milestone
    onUploadClick: () -> Unit
) {
    val isPending = milestone.status.contains("PENDING", ignoreCase = true)
    val isSubmitted = milestone.status.contains("SUBMITTED", ignoreCase = true)
    val isCompleted = milestone.status.contains("COMPLETED", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled && isPending) { 
                onUploadClick() 
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> Color(0xFFE8F5E9)
                isSubmitted -> Color(0xFFFFF3E0)
                !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (enabled && isPending) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title ?: "Untitled Milestone",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (!enabled && isPending) Color.Gray else Color.Unspecified
                )
                
                val displayAmount = milestone.amountToRelease ?: BigDecimal.ZERO
                Text(
                    "Release: ${milestone.releasePercentage}% (₹${String.format(Locale.getDefault(), "%.2f", displayAmount)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (!enabled && isPending) Color.Gray.copy(alpha = 0.7f) else Color.Unspecified
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when {
                            isCompleted -> Icons.Default.CheckCircle
                            isSubmitted -> Icons.Default.HourglassEmpty
                            !enabled -> Icons.Default.Lock
                            else -> Icons.Default.AddAPhoto
                        },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = when {
                            isCompleted -> Color(0xFF2E7D32)
                            isSubmitted -> Color(0xFFF57C00)
                            !enabled -> Color.Gray
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (!enabled && isPending) "LOCKED" else milestone.status.uppercase(),
                        color = when {
                            isCompleted -> Color(0xFF2E7D32)
                            isSubmitted -> Color(0xFFF57C00)
                            else -> Color.Gray
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (isPending && enabled) {
                Button(
                    onClick = onUploadClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Proof", style = MaterialTheme.typography.labelMedium)
                }
            } else if (!enabled && isPending) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray.copy(alpha = 0.5f)
                )
            }
        }
    }
}
