package et.vbl.feature.notifications

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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditScore
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NotificationItemUiModel(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val timeAgo: String
)

@Composable
fun NotificationsScreen(
    notifications: List<NotificationItemUiModel> = listOf(
        NotificationItemUiModel("1", "Deposit Credited", "Your deposit of 500 ETB via Telebirr has been credited to your savings account.", "PAYMENT", false, "10m ago"),
        NotificationItemUiModel("2", "Loan Repayment Due Soon", "Your monthly installment of 3,750 ETB is due on 01 Nov 2026.", "LOAN", false, "2h ago"),
        NotificationItemUiModel("3", "KYC Verification Received", "We received your national ID document and our team is reviewing it.", "KYC", true, "Yesterday")
    ),
    onNotificationClick: (String) -> Unit = {},
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
                        text = "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Activity & Cooperative Alerts",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
            }
        }

        // ─── LIST CONTAINER ───────────────────────────────────────────
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notifications yet",
                    fontSize = 15.sp,
                    color = Color(0xFF98A2B3)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationCard(
                        notification = notif,
                        onClick = { onNotificationClick(notif.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItemUiModel,
    onClick: () -> Unit
) {
    val (iconVector: ImageVector, iconTint: Color, iconBg: Color) = when (notification.type) {
        "PAYMENT" -> Triple(Icons.Outlined.AccountBalanceWallet, Color(0xFF16A34A), Color(0xFFDCFCE7))
        "LOAN" -> Triple(Icons.Outlined.CreditScore, Color(0xFF1B63CD), Color(0xFFEFF4FF))
        "KYC" -> Triple(Icons.Outlined.VerifiedUser, Color(0xFF7C3AED), Color(0xFFF3E8FF))
        else -> Triple(Icons.Outlined.Notifications, Color(0xFFD97706), Color(0xFFFEF3C7))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFF0F4FE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Professional Vector Icon (No emojis)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        color = Color(0xFF101828)
                    )
                    Text(
                        text = notification.timeAgo,
                        fontSize = 12.sp,
                        color = Color(0xFF98A2B3)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Color(0xFF475467),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
