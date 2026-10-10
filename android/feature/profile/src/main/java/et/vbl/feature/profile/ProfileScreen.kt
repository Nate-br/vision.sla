package et.vbl.feature.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun ProfileScreen(
    fullName: String = "Abebe Kebede",
    phoneNumber: String = "+251 91 122 3344",
    kycStatus: String = "VERIFIED",
    isBiometricsEnabled: Boolean = true,
    onKycClick: () -> Unit = {},
    onChangePinClick: () -> Unit = {},
    onSessionsClick: () -> Unit = {},
    onBiometricsToggle: (Boolean) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var biometricsState by remember { mutableStateOf(isBiometricsEnabled) }
    var notificationsState by remember { mutableStateOf(true) }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
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
                            text = "Profile & Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Vision SLA Member Portal",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // MEMBER EXECUTIVE CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User Avatar
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A3F7A))
                                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = fullName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = phoneNumber,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Member Tier Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFB81C).copy(alpha = 0.25f))
                                        .border(1.dp, Color(0xFFFFB81C).copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SLA PLATINUM MEMBER",
                                        color = Color(0xFFFFD466),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {

            // ─── SECURITY & ACCOUNT ────────────────────────────────────────
            SettingsGroupHeader(title = "SECURITY & ACCESS")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Security,
                        iconBg = Color(0xFFEFF8FF),
                        iconTint = Color(0xFF1B63CD),
                        title = "KYC Verification",
                        subtitle = "National ID & selfie approved",
                        trailing = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Verified",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        },
                        onClick = onKycClick
                    )

                    HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsRow(
                        icon = Icons.Default.Fingerprint,
                        iconBg = Color(0xFFF4F3FF),
                        iconTint = Color(0xFF5925DC),
                        title = "Biometric Authentication",
                        subtitle = "Instant fingerprint / face unlock",
                        trailing = {
                            Switch(
                                checked = biometricsState,
                                onCheckedChange = {
                                    biometricsState = it
                                    onBiometricsToggle(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF031634)
                                )
                            )
                        }
                    )

                    HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsRow(
                        icon = Icons.Default.Lock,
                        iconBg = Color(0xFFFEF3F2),
                        iconTint = Color(0xFFD92D20),
                        title = "Change Security PIN",
                        subtitle = "Update your 6-digit transaction PIN",
                        onClick = onChangePinClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── PREFERENCES ───────────────────────────────────────────────
            SettingsGroupHeader(title = "PREFERENCES")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        iconBg = Color(0xFFECFDF3),
                        iconTint = Color(0xFF027A48),
                        title = "Language",
                        subtitle = "English (US)",
                        trailing = {
                            Text(
                                text = "English",
                                fontSize = 13.sp,
                                color = Color(0xFF667085)
                            )
                        }
                    )

                    HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        iconBg = Color(0xFFFFF6ED),
                        iconTint = Color(0xFFC4320A),
                        title = "Push Notifications",
                        subtitle = "Instant SMS & receipt alerts",
                        trailing = {
                            Switch(
                                checked = notificationsState,
                                onCheckedChange = { notificationsState = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF031634)
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── COOPERATIVE & SUPPORT ─────────────────────────────────────
            SettingsGroupHeader(title = "COOPERATIVE LEGAL & HELP")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        iconBg = Color(0xFFF8F9FC),
                        iconTint = Color(0xFF344054),
                        title = "Cooperative Agreement & Bylaws",
                        subtitle = "SLA Member rights and deposit guarantee"
                    )

                    HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsRow(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        iconBg = Color(0xFFF8F9FC),
                        iconTint = Color(0xFF344054),
                        title = "24/7 Member Support",
                        subtitle = "Hotline 8200 • support@savethegeneration.com.et"
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── LOGOUT BUTTON ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(Color(0xFFFEE4E2))
                    .clickable(role = Role.Button, onClick = onLogoutClick),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color(0xFFD92D20),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out of Vision SLA",
                        color = Color(0xFFD92D20),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF98A2B3),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101828)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF667085)
                )
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFD0D5DD),
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
