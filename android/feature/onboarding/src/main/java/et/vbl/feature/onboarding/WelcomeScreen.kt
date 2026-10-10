package et.vbl.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onLearnMoreClick: () -> Unit = {},
    onSkipToDashboard: () -> Unit = {}
) {
    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF031634),
            Color(0xFF062B67),
            Color(0xFF0C4697),
            Color(0xFF165EC8)
        )
    )

    // Gentle floating card float animation
    val cardFloatY = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        cardFloatY.animateTo(
            targetValue = -12f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ─── TOP SECTION: HEADLINE ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                // Skip button in top corner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Dashboard",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable(onClick = onSkipToDashboard)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Modern Tools\nfor Modern\nFinance",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 46.sp,
                    letterSpacing = (-0.5).sp
                )
            }

            // ─── CENTER SECTION: MATTE OBSIDIAN FINTECH CARD ────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Ambient backlight aura
                Canvas(modifier = Modifier.size(260.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF388BFD).copy(alpha = 0.45f),
                                Color(0xFF1E5BB5).copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 1.5f,
                        center = center
                    )
                }

                // Sleek Matte Black Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(210.dp)
                        .shadow(elevation = 24.dp, shape = RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF121419)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1D2128),
                                        Color(0xFF111317),
                                        Color(0xFF0D0E12)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(700f, 700f)
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.25f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .padding(22.dp)
                    ) {
                        // Geometric stylized pattern matching the photo
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val path = Path().apply {
                                moveTo(w * 0.65f, 0f)
                                lineTo(w * 0.95f, 0f)
                                lineTo(w * 0.65f, h)
                                lineTo(w * 0.35f, h)
                                close()
                            }
                            drawPath(
                                path = path,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.08f),
                                        Color.White.copy(alpha = 0.02f)
                                    )
                                )
                            )

                            val path2 = Path().apply {
                                moveTo(w * 0.82f, 0f)
                                lineTo(w, 0f)
                                lineTo(w * 0.85f, h)
                                lineTo(w * 0.67f, h)
                                close()
                            }
                            drawPath(
                                path = path2,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.White.copy(alpha = 0.03f)
                                    )
                                )
                            )
                        }

                        // Card Content
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Chip and NFC icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Realistic EMV chip
                                Box(
                                    modifier = Modifier
                                        .size(width = 44.dp, height = 34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    Color(0xFFE5C158),
                                                    Color(0xFFF9E8A2),
                                                    Color(0xFFD4A738)
                                                )
                                            )
                                        )
                                        .border(0.5.dp, Color(0xFF8A6B1A), RoundedCornerShape(6.dp))
                                ) {
                                    // Internal chip contact lines
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawLine(
                                            color = Color(0xFF9E7C20),
                                            start = Offset(0f, size.height / 2),
                                            end = Offset(size.width, size.height / 2),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                        drawLine(
                                            color = Color(0xFF9E7C20),
                                            start = Offset(size.width * 0.45f, 0f),
                                            end = Offset(size.width * 0.45f, size.height),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = "Contactless",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Bottom Row: Card Details & Logo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "•••• 9286",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 2.sp
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "VISION SLA PLATINUM",
                                        color = Color.White.copy(alpha = 0.55f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.5.sp
                                    )
                                }

                                Text(
                                    text = "VISA",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            // ─── BOTTOM SECTION: CTA BUTTONS ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Learn More" Pill Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .clip(RoundedCornerShape(29.dp))
                        .background(Color.White)
                        .clickable(role = Role.Button, onClick = onLearnMoreClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Learn More",
                        color = Color(0xFF101828),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Arrow Circle Button
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(role = Role.Button, onClick = onLearnMoreClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Continue",
                        tint = Color(0xFF101828),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
