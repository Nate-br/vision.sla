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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import et.vbl.core.model.PaymentProvider
import et.vbl.core.model.PaymentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentTimelineScreen(
    status: PaymentStatus = PaymentStatus.VERIFIED_CREDITED,
    provider: PaymentProvider = PaymentProvider.TELEBIRR,
    reference: String = "RA47KL92X",
    amountSantim: String = "50000",
    rejectionReason: String? = null,
    onViewHome: () -> Unit = {},
    onRetry: () -> Unit = {},
    onContactSupport: () -> Unit = {}
) {
    val amountEtb = (amountSantim.toDoubleOrNull() ?: 0.0) / 100.0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Payment Status",
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
                // Main Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (status) {
                            PaymentStatus.VERIFIED_CREDITED, PaymentStatus.APPROVED -> VblColors.SuccessLight
                            PaymentStatus.MANUAL_REVIEW -> VblColors.WarningLight
                            PaymentStatus.PENDING_RETRY, PaymentStatus.VERIFYING -> VblColors.PrimaryBlueLight.copy(alpha = 0.5f)
                            PaymentStatus.REJECTED -> VblColors.ErrorLight
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Status Icon Circle
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    when (status) {
                                        PaymentStatus.VERIFIED_CREDITED, PaymentStatus.APPROVED -> VblColors.Success
                                        PaymentStatus.MANUAL_REVIEW -> VblColors.Warning
                                        PaymentStatus.PENDING_RETRY, PaymentStatus.VERIFYING -> VblColors.PrimaryBlue
                                        PaymentStatus.REJECTED -> VblColors.Error
                                        else -> VblColors.PrimaryBlue
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (status == PaymentStatus.VERIFYING) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    text = when (status) {
                                        PaymentStatus.VERIFIED_CREDITED, PaymentStatus.APPROVED -> "✓"
                                        PaymentStatus.MANUAL_REVIEW -> "!"
                                        PaymentStatus.REJECTED -> "✕"
                                        else -> "•"
                                    },
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when (status) {
                                PaymentStatus.VERIFIED_CREDITED -> "Payment Credited!"
                                PaymentStatus.APPROVED -> "Payment Approved"
                                PaymentStatus.VERIFYING -> "Verifying Payment..."
                                PaymentStatus.PENDING_RETRY -> "Provider Pending..."
                                PaymentStatus.MANUAL_REVIEW -> "Under Manual Review"
                                PaymentStatus.REJECTED -> "Payment Rejected"
                                else -> "Payment Submitted"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = when (status) {
                                PaymentStatus.VERIFIED_CREDITED, PaymentStatus.APPROVED -> VblColors.Success
                                PaymentStatus.MANUAL_REVIEW -> VblColors.Warning
                                PaymentStatus.REJECTED -> VblColors.Error
                                else -> VblColors.PrimaryBlue
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ETB %.2f".format(amountEtb),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = when (status) {
                                PaymentStatus.VERIFIED_CREDITED ->
                                    "Your deposit has been verified and settled to your savings account via our double-entry ledger."
                                PaymentStatus.MANUAL_REVIEW ->
                                    "Your money is safe! This receipt requires verification by an SLA officer. It will be credited within 24 hours."
                                PaymentStatus.PENDING_RETRY ->
                                    "We are waiting for confirmation from ${provider.displayName}. We will keep polling automatically."
                                PaymentStatus.REJECTED ->
                                    rejectionReason ?: "This receipt could not be verified or was already credited."
                                else -> "Receipt received and awaiting verification."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Transaction Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "TRANSACTION DETAILS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow(label = "Payment Method", value = provider.displayName)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        DetailRow(label = "Reference Code", value = reference)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        DetailRow(label = "Minor Units", value = "$amountSantim santim")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        DetailRow(
                            label = "Status",
                            value = status.name,
                            valueColor = when (status) {
                                PaymentStatus.VERIFIED_CREDITED, PaymentStatus.APPROVED -> VblColors.Success
                                PaymentStatus.MANUAL_REVIEW -> VblColors.Warning
                                PaymentStatus.REJECTED -> VblColors.Error
                                else -> VblColors.PrimaryBlue
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                when (status) {
                    PaymentStatus.VERIFIED_CREDITED, PaymentStatus.APPROVED -> {
                        Button(
                            onClick = onViewHome,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VblColors.PrimaryBlue
                            )
                        ) {
                            Text(
                                text = "View Updated Balance",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    PaymentStatus.REJECTED -> {
                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VblColors.PrimaryBlue
                            )
                        ) {
                            Text(
                                text = "Try Again with Correct Reference",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onContactSupport,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Contact SLA Support",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = VblColors.PrimaryBlue
                            )
                        }
                    }
                    else -> {
                        Button(
                            onClick = onViewHome,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VblColors.PrimaryBlue
                            )
                        ) {
                            Text(
                                text = "Return to Dashboard",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
