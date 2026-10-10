package et.vbl.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.ShoppingCart
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ExpenseBubbleData(
    val title: String,
    val percentage: String,
    val icon: ImageVector,
    val sizeDp: Int,
    val xPercent: Float,
    val yPercent: Float,
    val isPrimaryHighlighted: Boolean = false
)

@Composable
fun ExpensesBubblesScreen(
    totalExpenses: String = "$1,842.56",
    onBackClick: () -> Unit = {},
    onSeeAllClick: () -> Unit = {}
) {
    val bubbles = remember {
        listOf(
            ExpenseBubbleData(
                title = "Clothes and Shoes",
                percentage = "16,1%",
                icon = Icons.Outlined.Checkroom,
                sizeDp = 126,
                xPercent = 0.05f,
                yPercent = 0.06f
            ),
            ExpenseBubbleData(
                title = "Health",
                percentage = "8,4%",
                icon = Icons.Outlined.MonitorHeart,
                sizeDp = 98,
                xPercent = 0.65f,
                yPercent = 0.05f
            ),
            ExpenseBubbleData(
                title = "Sport",
                percentage = "11,3%",
                icon = Icons.Outlined.FitnessCenter,
                sizeDp = 106,
                xPercent = 0.08f,
                yPercent = 0.35f
            ),
            ExpenseBubbleData(
                title = "Groceries",
                percentage = "32,8%",
                icon = Icons.Outlined.ShoppingCart,
                sizeDp = 156,
                xPercent = 0.50f,
                yPercent = 0.24f,
                isPrimaryHighlighted = true
            ),
            ExpenseBubbleData(
                title = "Cafes and Restaurants",
                percentage = "14,8%",
                icon = Icons.Outlined.LocalCafe,
                sizeDp = 122,
                xPercent = 0.08f,
                yPercent = 0.62f
            ),
            ExpenseBubbleData(
                title = "Travel",
                percentage = "6,7%",
                icon = Icons.Outlined.DirectionsBus,
                sizeDp = 96,
                xPercent = 0.68f,
                yPercent = 0.65f
            )
        )
    }

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF02173B),
            Color(0xFF062A68),
            Color(0xFF0E4394),
            Color(0xFF165DBF)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ─── TOP APP BAR ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
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

            // Calendar Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Calendar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ─── BUBBLES CANVAS AREA ───────────────────────────────────────
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            val canvasWidth = maxWidth
            val canvasHeight = maxHeight

            bubbles.forEach { bubble ->
                val bubbleAnim = remember { Animatable(0.6f) }
                LaunchedEffect(Unit) {
                    bubbleAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(650, easing = FastOutSlowInEasing)
                    )
                }

                val xOffset = (canvasWidth.value * bubble.xPercent).dp
                val yOffset = (canvasHeight.value * bubble.yPercent).dp

                GlassSphereBubble(
                    bubble = bubble,
                    modifier = Modifier
                        .offset(x = xOffset, y = yOffset)
                        .size((bubble.sizeDp * bubbleAnim.value).dp)
                )
            }
        }

        // ─── BOTTOM WHITE SHEET: EXPENSES SUMMARY ──────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 120.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expenses",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF101828)
                    )
                    Text(
                        text = "View All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF98A2B3),
                        modifier = Modifier.clickable(onClick = onSeeAllClick)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = totalExpenses,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828),
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Breakdown pills: Groceries & Clothes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FC))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Groceries",
                                    fontSize = 12.sp,
                                    color = Color(0xFF667085),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color(0xFF667085),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$604.36",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF101828)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FC))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Clothes and Shoes",
                                    fontSize = 12.sp,
                                    color = Color(0xFF667085),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Checkroom,
                                    contentDescription = null,
                                    tint = Color(0xFF667085),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$296.65",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF101828)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassSphereBubble(
    bubble: ExpenseBubbleData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 3D Glass Sphere Renderer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // 1. Specular highlight & spherical gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (bubble.isPrimaryHighlighted) {
                        listOf(
                            Color.White.copy(alpha = 0.50f),
                            Color.White.copy(alpha = 0.24f),
                            Color(0xFF286ED4).copy(alpha = 0.18f),
                            Color(0xFF0F3E8C).copy(alpha = 0.10f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.34f),
                            Color.White.copy(alpha = 0.16f),
                            Color(0xFF1F58AA).copy(alpha = 0.12f),
                            Color(0xFF0B2E6A).copy(alpha = 0.05f)
                        )
                    },
                    center = Offset(size.width * 0.35f, size.height * 0.30f),
                    radius = radius * 1.1f
                ),
                radius = radius,
                center = center
            )

            // 2. Ambient Glass Rim
            drawCircle(
                color = if (bubble.isPrimaryHighlighted) Color.White.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.30f),
                radius = radius - 1.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 3. Highlighted outer arc (specifically for Groceries as seen in screenshot)
            if (bubble.isPrimaryHighlighted) {
                drawArc(
                    color = Color.White.copy(alpha = 0.95f),
                    startAngle = 140f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Inner Text & Icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = bubble.icon,
                contentDescription = bubble.title,
                tint = Color.White,
                modifier = Modifier.size(if (bubble.sizeDp > 120) 22.dp else 17.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = bubble.title,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = if (bubble.sizeDp > 130) 11.sp else 9.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                maxLines = 2
            )
            Text(
                text = bubble.percentage,
                color = Color.White,
                fontSize = if (bubble.sizeDp > 130) 13.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
