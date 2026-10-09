package et.vbl.feature.payments

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.designsystem.theme.VblColors
import et.vbl.core.model.PaymentProvider

enum class PaymentPurposeOption(val displayName: String) {
    SAVINGS("Monthly / Voluntary Savings"),
    LOAN_REPAYMENT("Active Loan Repayment"),
    MEMBERSHIP_FEE("Membership Fee")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceEntryScreen(
    provider: PaymentProvider = PaymentProvider.TELEBIRR,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSubmitPayment: (reference: String, amountSantim: String, purpose: PaymentPurposeOption) -> Unit = { _, _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var rawReference by remember { mutableStateOf("") }
    var rawAmountEtb by remember { mutableStateOf("") }
    var selectedPurpose by remember { mutableStateOf(PaymentPurposeOption.SAVINGS) }

    // Normalize reference: uppercase and remove spaces/dashes
    val normalizedReference = remember(rawReference) {
        rawReference.uppercase().replace(Regex("[\\s-]+"), "")
    }

    val amountSantim = remember(rawAmountEtb) {
        val parsed = rawAmountEtb.toDoubleOrNull() ?: 0.0
        (Math.round(parsed * 100)).toString()
    }

    val isValid = normalizedReference.length >= 6 && (rawAmountEtb.toDoubleOrNull() ?: 0.0) > 0.0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Enter Receipt Reference",
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
                // Provider Pill
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VblColors.PrimaryBlueLight.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected Provider:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = provider.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = VblColors.PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Payment Purpose
                Text(
                    text = "PAYMENT PURPOSE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentPurposeOption.entries.forEach { option ->
                        FilterChip(
                            selected = selectedPurpose == option,
                            onClick = { selectedPurpose = option },
                            label = { Text(option.name.replace("_", " ")) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reference Number Input
                Text(
                    text = "RECEIPT OR TRANSACTION NUMBER",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = rawReference,
                    onValueChange = { rawReference = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. RA47KL92X or CBE98721A") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    supportingText = {
                        if (normalizedReference.isNotEmpty() && normalizedReference != rawReference) {
                            Text(
                                text = "Normalized: $normalizedReference",
                                color = VblColors.PrimaryBlue,
                                style = MaterialTheme.typography.labelSmall
                            )
                        } else {
                            Text(
                                text = "Enter exact transaction code from your SMS receipt",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input
                Text(
                    text = "AMOUNT (ETB)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = rawAmountEtb,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            rawAmountEtb = input
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    supportingText = {
                        if ((rawAmountEtb.toDoubleOrNull() ?: 0.0) > 0) {
                            Text(
                                text = "= $amountSantim santim minor units",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = VblColors.ErrorLight
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            color = VblColors.Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Security Note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "SECURE VERIFICATION GUARANTEE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VblColors.PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Receipts are verified directly with banking switches. Credits are posted immediately via atomic double-entry ledger. Duplicate submissions are strictly prevented.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isValid && !isLoading) {
                            onSubmitPayment(normalizedReference, amountSantim, selectedPurpose)
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
                            text = "Submit for Instant Verification",
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
