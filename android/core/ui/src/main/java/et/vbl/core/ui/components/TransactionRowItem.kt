package et.vbl.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.designsystem.theme.VblTheme
import java.text.DecimalFormat

@Composable
fun TransactionRowItem(
    transactionType: String = "DEPOSIT",
    amountSantim: String = "50000",
    paymentProvider: String? = "TELEBIRR",
    status: String = "COMPLETED",
    description: String? = "Monthly Savings",
    dateFormatted: String = "Today, 14:32",
    modifier: Modifier = Modifier
) {
    val isCredit = transactionType == "DEPOSIT" || transactionType == "INTEREST_CREDIT"
    val santim = amountSantim.toLongOrNull() ?: 0L
    val birr = santim / 100.0
    val formattedAmount = DecimalFormat("#,##0.00").format(birr)

    val iconSymbol = when (transactionType) {
        "DEPOSIT" -> "↓"
        "WITHDRAWAL" -> "↑"
        "INTEREST_CREDIT" -> "★"
        "LOAN_REPAYMENT" -> "↩"
        else -> "•"
    }

    val iconBgColor = if (isCredit) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val iconTextColor = if (isCredit) Color(0xFF2E7D32) else Color(0xFFC62828)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconSymbol,
                    color = iconTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = description ?: transactionType.replace('_', ' '),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${paymentProvider ?: "System"} • $dateFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isCredit) "+" else "-"}$formattedAmount ETB",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCredit) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "TransactionRowItem - Light", showBackground = true)
@Composable
private fun TransactionRowItemPreview() {
    VblTheme(darkTheme = false) {
        TransactionRowItem()
    }
}
