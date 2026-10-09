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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.designsystem.theme.VblColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCalculatorScreen(
    productName: String = "Business Micro Loan",
    annualInterestRatePercent: Double = 12.0,
    minAmountEtb: Double = 5000.0,
    maxAmountEtb: Double = 150000.0,
    minTenureMonths: Int = 3,
    maxTenureMonths: Int = 24,
    onProceedToApply: (amountEtb: Double, tenureMonths: Int) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var amountEtb by remember { mutableFloatStateOf(25000.0f) }
    var tenureMonths by remember { mutableStateOf(12) }

    // Financial formulas
    val principal = amountEtb.toDouble()
    val totalInterest = principal * (annualInterestRatePercent / 100.0) * (tenureMonths / 12.0)
    val totalRepayment = principal + totalInterest
    val monthlyPayment = totalRepayment / tenureMonths
    val processingFee = principal * 0.01 // 1% processing fee

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Loan Repayment Calculator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Product Header
                Text(
                    text = productName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VblColors.PrimaryBlue
                )
                Text(
                    text = "%.1f%% annual flat rate · 1%% administrative processing fee".format(annualInterestRatePercent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Amount Slider Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Principal Borrowed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "ETB %,.0f".format(amountEtb),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = VblColors.PrimaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = amountEtb,
                            onValueChange = { amountEtb = it },
                            valueRange = minAmountEtb.toFloat()..maxAmountEtb.toFloat(),
                            steps = 29,
                            colors = SliderDefaults.colors(
                                thumbColor = VblColors.PrimaryBlue,
                                activeTrackColor = VblColors.PrimaryBlue
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Min: ETB %,.0f".format(minAmountEtb),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Max: ETB %,.0f".format(maxAmountEtb),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tenure Slider Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Repayment Duration",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$tenureMonths Months",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VblColors.PrimaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = tenureMonths.toFloat(),
                            onValueChange = { tenureMonths = it.toInt() },
                            valueRange = minTenureMonths.toFloat()..maxTenureMonths.toFloat(),
                            steps = (maxTenureMonths - minTenureMonths - 1).coerceAtLeast(0),
                            colors = SliderDefaults.colors(
                                thumbColor = VblColors.PrimaryBlue,
                                activeTrackColor = VblColors.PrimaryBlue
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$minTenureMonths Months",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$maxTenureMonths Months",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Calculation Result Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VblColors.PrimaryBlueLight.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "ESTIMATED MONTHLY INSTALLMENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VblColors.PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ETB %,.2f".format(monthlyPayment),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = VblColors.PrimaryBlue
                        )
                        Text(
                            text = "Payable monthly across $tenureMonths installments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = VblColors.OutlineLight)
                        Spacer(modifier = Modifier.height(16.dp))

                        SummaryRow("Total Principal", "ETB %,.2f".format(principal))
                        Spacer(modifier = Modifier.height(6.dp))
                        SummaryRow("Total Interest", "ETB %,.2f".format(totalInterest))
                        Spacer(modifier = Modifier.height(6.dp))
                        SummaryRow("Processing Fee (1%)", "ETB %,.2f".format(processingFee))
                        Spacer(modifier = Modifier.height(6.dp))
                        SummaryRow("Total Repayment Amount", "ETB %,.2f".format(totalRepayment), isBold = true)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { onProceedToApply(principal, tenureMonths) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VblColors.PrimaryBlue
                    )
                ) {
                    Text(
                        text = "Apply for this Loan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isBold) VblColors.PrimaryBlue else Color.Unspecified
        )
    }
}
