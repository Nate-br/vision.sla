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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.designsystem.theme.VblColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanApplicationScreen(
    productName: String = "Business Micro Loan",
    initialAmountEtb: Double = 25000.0,
    initialTenureMonths: Int = 12,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSubmitApplication: (amountEtb: Double, tenureMonths: Int, purpose: String) -> Unit = { _, _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var amountText by remember { mutableStateOf(initialAmountEtb.toInt().toString()) }
    var tenureText by remember { mutableStateOf(initialTenureMonths.toString()) }
    var purpose by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val tenure = tenureText.toIntOrNull() ?: 0
    val isValid = amount > 0 && tenure > 0 && purpose.trim().length >= 10 && agreeToTerms

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Loan Application",
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
                // Product Summary Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VblColors.PrimaryBlue
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Applying for: $productName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Applications are typically reviewed by credit officers within 24 to 48 hours.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Loan Amount
                Text(
                    text = "REQUESTED AMOUNT (ETB)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 25000") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tenure Months
                Text(
                    text = "DESIRED TENURE (MONTHS)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = tenureText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) tenureText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 12") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Purpose
                Text(
                    text = "LOAN PURPOSE & USE OF FUNDS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = { Text("Describe how the funds will be used (min 10 characters)...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = VblColors.ErrorLight)
                    ) {
                        Text(
                            text = errorMessage,
                            color = VblColors.Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Terms agreement checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreeToTerms,
                        onCheckedChange = { agreeToTerms = it }
                    )
                    Text(
                        text = "I confirm the accuracy of this information and agree to the cooperative lending bylaws and deduction terms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isValid && !isLoading) {
                            onSubmitApplication(amount, tenure, purpose)
                        }
                    },
                    enabled = isValid && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VblColors.PrimaryBlue
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Submit Application",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
