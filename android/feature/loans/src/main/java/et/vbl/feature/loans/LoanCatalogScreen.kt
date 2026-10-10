package et.vbl.feature.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.designsystem.theme.VblColors

data class LoanProductUiModel(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val interestRatePercent: Double,
    val minAmountEtb: Double,
    val maxAmountEtb: Double,
    val minMonths: Int,
    val maxMonths: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCatalogScreen(
    products: List<LoanProductUiModel> = listOf(
        LoanProductUiModel(
            id = "1",
            code = "BIZ_MICRO",
            name = "Business Micro Loan",
            description = "Working capital and inventory finance for cooperative members in trade and retail.",
            interestRatePercent = 12.0,
            minAmountEtb = 5000.0,
            maxAmountEtb = 150000.0,
            minMonths = 3,
            maxMonths = 24
        ),
        LoanProductUiModel(
            id = "2",
            code = "SALARY_ADV",
            name = "Salary Advance",
            description = "Quick short-term liquidity advance for salaried members with direct payroll deposit.",
            interestRatePercent = 9.5,
            minAmountEtb = 2000.0,
            maxAmountEtb = 40000.0,
            minMonths = 1,
            maxMonths = 6
        ),
        LoanProductUiModel(
            id = "3",
            code = "EMERGENCY",
            name = "Emergency Medical & Relief Loan",
            description = "Fast-disbursing emergency relief for health, education, or unforeseen urgent family needs.",
            interestRatePercent = 8.0,
            minAmountEtb = 1000.0,
            maxAmountEtb = 25000.0,
            minMonths = 1,
            maxMonths = 12
        )
    ),
    onCalculateClick: (LoanProductUiModel) -> Unit = {},
    onApplyClick: (LoanProductUiModel) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val heroGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF031634),
            Color(0xFF072C67),
            Color(0xFF0E4698),
            Color(0xFF1B63CD)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF031634))
    ) {
        // ─── TOP HERO BLUE HEADER ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroGradient)
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                // Top App Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "SLA Member Loans",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Zero-Collateral Cooperative Financing",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Information banner pill
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Transparent terms, competitive interest rates, and no hidden penalties. All loans are disbursed instantly to Telebirr or CBE.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.90f),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // ─── WHITE CURVED BODY CONTAINER ──────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(products) { product ->
                    ProductCardItem(
                        product = product,
                        onCalculateClick = { onCalculateClick(product) },
                        onApplyClick = { onApplyClick(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCardItem(
    product: LoanProductUiModel,
    onCalculateClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(VblColors.PrimaryBlueLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "%.1f%% p.a.".format(product.interestRatePercent),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VblColors.PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Limits grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Loan Range",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "ETB %,.0f - %,.0f".format(product.minAmountEtb, product.maxAmountEtb),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tenure Range",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${product.minMonths} - ${product.maxMonths} Months",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCalculateClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Calculator",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = VblColors.PrimaryBlue
                    )
                }

                Button(
                    onClick = onApplyClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VblColors.PrimaryBlue
                    )
                ) {
                    Text(
                        text = "Apply Now",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
