package et.vbl.feature.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.model.PaymentProvider

@Composable
fun ProviderSelectScreen(
    onProviderSelected: (PaymentProvider) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    val filterTabs = listOf("All Methods", "Mobile Wallets", "Commercial Banks")

    val allProviders = remember {
        listOf(
            PaymentProvider.TELEBIRR,
            PaymentProvider.CBE,
            PaymentProvider.BANK_OF_ABYSSINIA,
            PaymentProvider.CBE_BIRR,
            PaymentProvider.AWASH,
            PaymentProvider.DASHEN,
            PaymentProvider.MPESA,
            PaymentProvider.COOPAY_EBIRR,
            PaymentProvider.SIINQEE
        )
    }

    val wallets = listOf(
        PaymentProvider.TELEBIRR,
        PaymentProvider.CBE_BIRR,
        PaymentProvider.MPESA,
        PaymentProvider.COOPAY_EBIRR
    )

    val displayedProviders = remember(searchQuery, selectedFilterIndex) {
        allProviders.filter { provider ->
            val matchesFilter = when (selectedFilterIndex) {
                1 -> provider in wallets
                2 -> provider !in wallets
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                provider.displayName.contains(searchQuery, ignoreCase = true) ||
                provider.name.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }

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
            .background(Color(0xFFF7F9FC))
    ) {
        // ─── TOP HERO BLUE HEADER (ROUNDED BOTTOM CORNERS AS IN REFERENCE) ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                .background(heroGradient)
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
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
                            text = "Save & Deposit",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Direct transfer to Vision SLA account",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Savings Highlight Banner
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Vision SLA Member Vault",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ETB 24,500.00",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "0% Transfer Fee • Instant Clearing",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Search Input inside Blue Hero
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search Telebirr, CBE, Abyssinia...",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                            focusedBorderColor = Color.White.copy(alpha = 0.45f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filter Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterTabs.forEachIndexed { index, tab ->
                        val isSelected = selectedFilterIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.16f))
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White else Color.White.copy(alpha = 0.25f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilterIndex = index }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = tab,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF031634) else Color.White
                            )
                        }
                    }
                }
            }
        }

        // ─── LIST CONTAINER ───────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(displayedProviders, key = { it.name }) { provider ->
                ProviderCard(
                    provider = provider,
                    onClick = { onProviderSelected(provider) }
                )
            }
        }
    }
}

@Composable
private fun ProviderCard(
    provider: PaymentProvider,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Authentic Vector Bank Logo
                BankLogoBadge(
                    provider = provider,
                    size = 46.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = provider.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF101828)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Instant 0% Fee Clearing",
                            fontSize = 12.sp,
                            color = Color(0xFF667085)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFD0D5DD),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
