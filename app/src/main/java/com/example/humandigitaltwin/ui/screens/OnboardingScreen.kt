package com.example.humandigitaltwin.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.humandigitaltwin.ui.components.LotusLogoBadge

@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (unitsMetric: Boolean, allowLocalPersistence: Boolean) -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCompleteOnboarding(true, true) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Headings & Brand
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LotusLogoBadge(size = 46.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Welcome to",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "SZM Vitalis",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "by Team Beyond Horizon • szm",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Your Personal Health Companion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center Visual: Holographic glowing human avatar silhouette with concentric ripple rings
            Box(
                modifier = Modifier
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                OnboardingHologramAvatar()
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Feature Checklist (matching Screen 1 from reference)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OnboardingFeatureItem("Connect your devices")
                OnboardingFeatureItem("Get personalized insights")
                OnboardingFeatureItem("Improve your daily habits")
                OnboardingFeatureItem("Your data stays private")
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Navigation: Indicator Dots & Get Started Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Indicator Dots (matching reference)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                // Teal CTA Pill Button
                Button(
                    onClick = { onCompleteOnboarding(true, true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun OnboardingHologramAvatar() {
    val infiniteTransition = rememberInfiniteTransition(label = "hologram")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        // Ripple orbital rings
        for (i in 0..2) {
            val ringFraction = ((waveOffset + i * 0.33f) % 1.0f)
            val ringRadius = (w * 0.2f) + ringFraction * (w * 0.28f)
            val ringAlpha = (1f - ringFraction) * 0.35f
            drawCircle(
                color = Color(0xFF00E5BE).copy(alpha = ringAlpha),
                radius = ringRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00E5BE).copy(alpha = 0.22f),
                    Color(0xFF38BDF8).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = w * 0.45f
            ),
            center = Offset(cx, cy),
            radius = w * 0.45f
        )

        // Holographic Human Avatar Silhouette
        val avatarColor = Color(0xFF5FFFE0)
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

        // Head
        drawCircle(avatarColor, radius = h * 0.08f, center = Offset(cx, cy - h * 0.24f), style = stroke)

        // Torso & Upper Body
        val torso = Path().apply {
            moveTo(cx - w * 0.18f, cy - h * 0.12f)
            lineTo(cx + w * 0.18f, cy - h * 0.12f)
            lineTo(cx + w * 0.12f, cy + h * 0.16f)
            lineTo(cx - w * 0.12f, cy + h * 0.16f)
            close()
        }
        drawPath(torso, Color(0xFF0A262D))
        drawPath(torso, avatarColor, style = stroke)

        // Chest Core Light Node
        drawCircle(Color(0xFF00E5BE), radius = 5.dp.toPx(), center = Offset(cx, cy - h * 0.02f))
        drawCircle(Color(0xFF00E5BE).copy(alpha = 0.4f), radius = 12.dp.toPx(), center = Offset(cx, cy - h * 0.02f))

        // Arms
        drawLine(avatarColor, Offset(cx - w * 0.18f, cy - h * 0.12f), Offset(cx - w * 0.24f, cy + h * 0.06f), strokeWidth = 2.dp.toPx())
        drawLine(avatarColor, Offset(cx - w * 0.24f, cy + h * 0.06f), Offset(cx - w * 0.20f, cy + h * 0.22f), strokeWidth = 2.dp.toPx())

        drawLine(avatarColor, Offset(cx + w * 0.18f, cy - h * 0.12f), Offset(cx + w * 0.24f, cy + h * 0.06f), strokeWidth = 2.dp.toPx())
        drawLine(avatarColor, Offset(cx + w * 0.24f, cy + h * 0.06f), Offset(cx + w * 0.20f, cy + h * 0.22f), strokeWidth = 2.dp.toPx())

        // Lower body / pedestal fade
        drawLine(avatarColor, Offset(cx - w * 0.08f, cy + h * 0.16f), Offset(cx - w * 0.10f, cy + h * 0.36f), strokeWidth = 2.dp.toPx())
        drawLine(avatarColor, Offset(cx + w * 0.08f, cy + h * 0.16f), Offset(cx + w * 0.10f, cy + h * 0.36f), strokeWidth = 2.dp.toPx())
    }
}
