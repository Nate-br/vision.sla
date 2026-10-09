package et.vbl.feature.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.designsystem.theme.VblColors

data class InstallmentUiModel(
    val installmentNumber: Int,
    val dueDate: String,
    val totalDueEtb: Double,
    val principalEtb: Double,
    val interestEtb: Double,
    val status: String, // PAID, PENDING, OVERDUE, PARTIALLY_PAID
    val paidDate: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanScheduleScreen(
    loanId: String = "loan-1",
    productName: String = "Business Micro Loan",
    balanceRemainingEtb: Double = 35000.0,
    nextDueDate: String? = "2026-11-01",
    paidCount: Int = 4,
    totalCount: Int = 12,
    installments: List<InstallmentUiModel> = listOf(
        InstallmentUiModel(1, "2026-07-01", 3125.0, 2500.0, 625.0, "PAID", "2026-06-28"),
        InstallmentUiModel(2, "2026-08-01", 3125.0, 2500.0, 625.0, "PAID", "2026-07-30"),
        InstallmentUiModel(3, "2026-09-01", 3125.0, 2500.0, 625.0, "PAID", "2026-08-31"),
        InstallmentUiModel(4, "2026-10-01", 3125.0, 2500.0, 625.0, "PAID", "2026-09-29"),
        InstallmentUiModel(5, "2026-11-01", 3125.0, 2500.0, 625.0, "PENDING"),
        InstallmentUiModel(6, "2026-12-01", 3125.0, 2500.0, 625.0, "PENDING"),
        InstallmentUiModel(7, "2027-01-01", 3125.0, 2500.0, 625.0, "PENDING")
    ),
    onPayInstallmentClick: (loanId: String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val progress = if (totalCount > 0) paidCount.toFloat() / totalCount.toFloat() else 0f

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Repayment Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active Loan Overview Card
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = VblColors.PrimaryBlue
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = productName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ETB %,.2f".format(balanceRemainingEtb),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Remaining Principal & Accrued Interest",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Repayment Progress",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "$paidCount of $totalCount Installments Paid",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = VblColors.GoldAccent,
                                trackColor = Color.White.copy(alpha = 0.25f)
                            )

                            if (nextDueDate != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Next Due Date: $nextDueDate",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VblColors.GoldLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onPayInstallmentClick(loanId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VblColors.PrimaryBlue
                        )
                    ) {
                        Text(
                            text = "Pay Next Installment",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "INSTALLMENT SCHEDULE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(installments) { inst ->
                    InstallmentCardItem(installment = inst)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun InstallmentCardItem(installment: InstallmentUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when (installment.status) {
                                    "PAID" -> VblColors.SuccessLight
                                    "OVERDUE" -> VblColors.ErrorLight
                                    else -> VblColors.PrimaryBlueLight
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (installment.status) {
                                "PAID" -> "✓"
                                "OVERDUE" -> "!"
                                else -> "#${installment.installmentNumber}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (installment.status) {
                                "PAID" -> VblColors.Success
                                "OVERDUE" -> VblColors.Error
                                else -> VblColors.PrimaryBlue
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Installment ${installment.installmentNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Due: ${installment.dueDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ETB %,.2f".format(installment.totalDueEtb),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = installment.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (installment.status) {
                            "PAID" -> VblColors.Success
                            "OVERDUE" -> VblColors.Error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = VblColors.OutlineLight.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Principal: ETB %,.2f".format(installment.principalEtb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Interest: ETB %,.2f".format(installment.interestEtb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
