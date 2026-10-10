package et.vbl.feature.home

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TransactionItem(
    val id: String,
    val title: String,
    val dateSubtitle: String,
    val amountFormatted: String,
    val isPositive: Boolean,
    val icon: ImageVector,
    val iconBg: Color = Color(0xFFF2F4F7)
)

@Composable
fun HomeScreen(
    memberName: String = "Nate",
    balanceDisplay: String = "$12,680.42",
    growthText: String = "2.46% this month",
    cardNumberMasked: String = "•••• 9286",
    cardExpiry: String = "08/32",
    showBottomDock: Boolean = true,
    currentTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onDepositClick: () -> Unit = {},
    onTransferClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onSeeAllTransactionsClick: () -> Unit = {}
) {
    val transactions = remember {
        listOf(
            TransactionItem(
                id = "1",
                title = "Adobe Creative Cloud",
                dateSubtitle = "Today, 09:42",
                amountFormatted = "-$59.99",
                isPositive = false,
                icon = Icons.Outlined.Palette,
                iconBg = Color(0xFFF2F4F8)
            ),
            TransactionItem(
                id = "2",
                title = "Stripe Payout",
                dateSubtitle = "Today, 08:15",
                amountFormatted = "$187.50",
                isPositive = true,
                icon = Icons.Outlined.AccountBalance,
                iconBg = Color(0xFFEBF3FF)
            ),
            TransactionItem(
                id = "3",
                title = "Office Supplies",
                dateSubtitle = "Jun 28, 10:22",
                amountFormatted = "$24.00",
                isPositive = true,
                icon = Icons.Outlined.WorkOutline,
                iconBg = Color(0xFFF2F4F7)
            ),
            TransactionItem(
                id = "4",
                title = "AWS Cloud Hosting",
                dateSubtitle = "Jun 24, 18:30",
                amountFormatted = "-$97.50",
                isPositive = false,
                icon = Icons.Outlined.CloudQueue,
                iconBg = Color(0xFFF2F4F8)
            ),
            TransactionItem(
                id = "5",
                title = "SLA Monthly Contribution",
                dateSubtitle = "Jun 20, 12:00",
                amountFormatted = "$250.00",
                isPositive = true,
                icon = Icons.Outlined.ShoppingBag,
                iconBg = Color(0xFFEBFDF2)
            )
        )
    }

    val heroGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF031634),
            Color(0xFF072C67),
            Color(0xFF0E4698),
            Color(0xFF1B63CD)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF031634))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ─── TOP HERO BLUE HEADER ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroGradient)
                    .statusBarsPadding()
                    .padding(top = 10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top App Bar: Profile Avatar & Notifications Bell
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar (styled like portrait in screenshot)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF7A889B), Color(0xFF384A63), Color(0xFF1B2A40))
                                    )
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                                .clickable(role = Role.Button, onClick = onProfileClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Notification Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .clickable(role = Role.Button, onClick = onNotificationsClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            // Mint notification dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-10).dp, y = 10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Balance Display
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Balance",
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = balanceDisplay,
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Growth pill badge: "↗ 2.46% this month"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.16f))
                                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NorthEast,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = growthText,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── STACKED CARDS AT BOTTOM OF BLUE SECTION ───────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Deepest Card Edge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .height(20.dp)
                                .offset(y = (-14).dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                        )

                        // Middle Card Edge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.91f)
                                .height(32.dp)
                                .offset(y = (-7).dp)
                                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                        )

                        // Front Card: •••• 9286 08/32 VISA
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF194A8A).copy(alpha = 0.92f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 22.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cardNumberMasked,
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(18.dp))
                                    Text(
                                        text = cardExpiry,
                                        color = Color.White.copy(alpha = 0.70f),
                                        fontSize = 13.sp
                                    )
                                }

                                Text(
                                    text = "VISA",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            // ─── WHITE CURVED CONTAINER ────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Quick Action Buttons Row: [↗ Deposit] [⇅] [↘ Withdraw]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Deposit Button
                            QuickActionPill(
                                icon = Icons.Default.NorthEast,
                                label = "Deposit",
                                onClick = onDepositClick,
                                modifier = Modifier.weight(1.3f)
                            )

                            // Transfer Button (Circular Icon)
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F3F7))
                                    .clickable(role = Role.Button, onClick = onTransferClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Transfer",
                                    tint = Color(0xFF101828),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Withdraw Button
                            QuickActionPill(
                                icon = Icons.Default.SouthEast,
                                label = "Withdraw",
                                onClick = onWithdrawClick,
                                modifier = Modifier.weight(1.3f)
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Recent Transactions Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Transactions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF101828)
                            )
                            Text(
                                text = "View All",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF98A2B3),
                                modifier = Modifier.clickable(onClick = onSeeAllTransactionsClick)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Transactions List
                    items(transactions, key = { it.id }) { tx ->
                        TransactionRow(
                            transaction = tx,
                            onClick = onSeeAllTransactionsClick
                        )
                    }
                }
            }
        }

        // ─── FLOATING BOTTOM DOCK ──────────────────────────────────────────
        if (showBottomDock) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                FloatingBottomDock(
                    currentTab = currentTab,
                    onTabSelected = onTabSelected
                )
            }
        }
    }
}

@Composable
fun FloatingBottomDock(
    currentTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F141E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Card / Wallet
            DockTabButton(
                isSelected = currentTab == 0,
                icon = Icons.Default.CreditCard,
                contentDescription = "Wallet",
                onClick = { onTabSelected(0) }
            )

            // Tab 1: Transfer ⇅
            DockTabButton(
                isSelected = currentTab == 1,
                icon = Icons.Default.SwapVert,
                contentDescription = "Transfer",
                onClick = { onTabSelected(1) }
            )

            // Tab 2: Analytics 🕒
            DockTabButton(
                isSelected = currentTab == 2,
                icon = Icons.Outlined.Schedule,
                contentDescription = "Analytics",
                onClick = { onTabSelected(2) }
            )

            // Tab 3: Loans [ ]
            DockTabButton(
                isSelected = currentTab == 3,
                icon = Icons.Outlined.CropFree,
                contentDescription = "Loans",
                onClick = { onTabSelected(3) }
            )

            // Tab 4: Profile & Settings ⊞
            DockTabButton(
                isSelected = currentTab == 4,
                icon = Icons.Outlined.Apps,
                contentDescription = "Settings",
                onClick = { onTabSelected(4) }
            )
        }
    }
}

@Composable
private fun DockTabButton(
    isSelected: Boolean,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFF222938) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFFF1F3F7))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF101828),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101828)
            )
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(transaction.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = null,
                    tint = Color(0xFF344054),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = transaction.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101828)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.dateSubtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF98A2B3)
                )
            }
        }

        Text(
            text = transaction.amountFormatted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101828)
        )
    }
}

