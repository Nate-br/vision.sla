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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.designsystem.theme.VblColors

enum class StatementPreset(val displayName: String, val days: Int) {
    LAST_30_DAYS("Last 30 Days", 30),
    LAST_90_DAYS("Last 90 Days", 90),
    THIS_YEAR("This Year", 365)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementExportScreen(
    currentSavingsEtb: Double = 15000.0,
    totalDepositsEtb: Double = 18500.0,
    activeLoanBalanceEtb: Double = 35000.0,
    isExporting: Boolean = false,
    onDownloadPdf: (fromDaysAgo: Int) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var selectedPreset by remember { mutableStateOf(StatementPreset.LAST_90_DAYS) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Account Statements",
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
                // Statement info banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VblColors.PrimaryBlue
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Official Certified Statement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generate and download a certified PDF statement for visa, bank verification, or personal record keeping.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Period selection
                Text(
                    text = "SELECT STATEMENT PERIOD",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatementPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = { selectedPreset = preset },
                            label = { Text(preset.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Financial Overview Preview
                Text(
                    text = "PERIOD SUMMARY PREVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SummaryRow("Current Savings Balance", "ETB %,.2f".format(currentSavingsEtb))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        SummaryRow("Total Period Deposits", "ETB %,.2f".format(totalDepositsEtb))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        SummaryRow("Active Loan Outstanding", "ETB %,.2f".format(activeLoanBalanceEtb))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onDownloadPdf(selectedPreset.days) },
                    enabled = !isExporting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VblColors.PrimaryBlue
                    )
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Download Official PDF Statement",
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

@Composable
private fun SummaryRow(label: String, value: String) {
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
            fontWeight = FontWeight.Bold
        )
    }
}
