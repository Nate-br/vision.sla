package et.vbl.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.designsystem.theme.VblTheme
import et.vbl.core.ui.components.BalanceCard
import et.vbl.core.ui.components.KycBanner
import et.vbl.core.ui.components.LoanSummaryCard
import et.vbl.core.ui.components.TransactionRowItem

data class TransactionItemUiModel(
    val id: String,
    val transactionType: String,
    val amountSantim: String,
    val paymentProvider: String?,
    val status: String,
    val description: String?,
    val dateFormatted: String
)

@Composable
fun HomeScreen(
    memberName: String = "Abebe",
    kycStatus: String = "NOT_SUBMITTED",
    accountNumber: String = "VBL-SAV-102938",
    totalSavingsSantim: String = "1500000",
    availableSantim: String = "1450000",
    frozenSantim: String = "50000",
    hasActiveLoan: Boolean = true,
    loanDueSantim: String = "4500000",
    loanMonthlySantim: String = "375000",
    loanNextDueDate: String? = "2026-11-01",
    loanPaidInstallments: Int = 5,
    loanTotalInstallments: Int = 12,
    recentTransactions: List<TransactionItemUiModel> = listOf(
        TransactionItemUiModel("1", "DEPOSIT", "50000", "TELEBIRR", "COMPLETED", "Monthly Savings", "Today, 14:30"),
        TransactionItemUiModel("2", "LOAN_REPAYMENT", "375000", "CBE", "COMPLETED", "Business Loan Installment", "Yesterday"),
        TransactionItemUiModel("3", "DEPOSIT", "100000", "CBEBIRR", "COMPLETED", "Voluntary Deposit", "05 Oct")
    ),
    onDepositClick: () -> Unit = {},
    onApplyLoanClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onVerifyKycClick: () -> Unit = {},
    onSeeAllTransactionsClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Top Header: Greeting + Notifications Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good day,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = memberName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(role = Role.Button, onClick = onNotificationsClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔔", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // KYC Prompt Banner if unverified
                if (kycStatus != "APPROVED") {
                    KycBanner(
                        kycStatus = kycStatus,
                        onVerifyClick = onVerifyKycClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Balance Card
                BalanceCard(
                    accountNumber = accountNumber,
                    totalSavingsSantim = totalSavingsSantim,
                    availableSantim = availableSantim,
                    frozenSantim = frozenSantim
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton(
                        icon = "💳",
                        label = "Deposit / Pay",
                        onClick = onDepositClick,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    QuickActionButton(
                        icon = "📈",
                        label = "Loans",
                        onClick = onApplyLoanClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Loan Summary Card
                LoanSummaryCard(
                    hasActiveLoan = hasActiveLoan,
                    totalDueSantim = loanDueSantim,
                    monthlyInstallmentSantim = loanMonthlySantim,
                    nextDueDate = loanNextDueDate,
                    paidInstallments = loanPaidInstallments,
                    totalInstallments = loanTotalInstallments
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Recent Transactions Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onSeeAllTransactionsClick)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recentTransactions, key = { it.id }) { tx ->
                TransactionRowItem(
                    transactionType = tx.transactionType,
                    amountSantim = tx.amountSantim,
                    paymentProvider = tx.paymentProvider,
                    status = tx.status,
                    description = tx.description,
                    dateFormatted = tx.dateFormatted
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "HomeScreen - Light", showBackground = true)
@Composable
private fun HomeScreenLightPreview() {
    VblTheme(darkTheme = false) {
        HomeScreen()
    }
}

@Preview(name = "HomeScreen - Dark", showBackground = true)
@Composable
private fun HomeScreenDarkPreview() {
    VblTheme(darkTheme = true) {
        HomeScreen()
    }
}
