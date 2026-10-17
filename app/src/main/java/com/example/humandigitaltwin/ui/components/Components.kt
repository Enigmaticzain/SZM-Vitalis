package com.example.humandigitaltwin.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ProvenanceType(val label: String, val description: String) {
    MEASURED("Measured", "Direct biometric telemetry from hardware sensor"),
    ESTIMATED("Estimated", "Derived from physiological baseline and activity heuristic"),
    PREDICTED("Predicted", "Machine learning forecast and scenario projection"),
    SIMULATED("Simulated", "Synthetic physiological event generator"),
    USER_REPORTED("User Reported", "Self-recorded manual entry by user")
}

@Composable
fun ProvenanceBadge(
    type: ProvenanceType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (type) {
        ProvenanceType.MEASURED -> Color(0xFF00382E) to Color(0xFF73F5DD)
        ProvenanceType.ESTIMATED -> Color(0xFF00363D) to Color(0xFF8CEEFF)
        ProvenanceType.PREDICTED -> Color(0xFF4A2800) to Color(0xFFFFDDB8)
        ProvenanceType.SIMULATED -> Color(0xFF281C4A) to Color(0xFFD6C2FF)
        ProvenanceType.USER_REPORTED -> Color(0xFF162A45) to Color(0xFFB8D7FF)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier.semantics {
            contentDescription = "Data provenance: ${type.description}"
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(textColor, RoundedCornerShape(3.dp))
            )
            Text(
                text = type.label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun MedicalDisclaimerCard(
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Medical disclaimer icon",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Investigational Health Prototype",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse disclaimer" else "Expand disclaimer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This application is an educational and lifestyle biohacking platform designed to model physiological trends. Calculations, biological age scores, and digital nurse coaching are heuristic approximations and DO NOT constitute clinical diagnoses, medical advice, or continuous hospital-grade patient monitoring. Always consult a licensed healthcare professional for medical concerns.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    isConnected: Boolean,
    isOfflineDemo: Boolean,
    lastUpdatedText: String,
    onReconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                    contentDescription = if (isConnected) "Connected" else "Offline",
                    tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
                Column {
                    Text(
                        text = when {
                            isConnected -> "Live Telemetry Bridge Active"
                            isOfflineDemo -> "Offline Resilience Demo Mode"
                            else -> "Disconnected from Telemetry Host"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isOfflineDemo) "Operating with deterministic baseline • Updated: $lastUpdatedText"
                               else "Last updated: $lastUpdatedText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            FilledTonalButton(
                onClick = onReconnectClick,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isConnected) "Refresh" else "Retry")
            }
        }
    }
}

@Composable
fun VitalCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    description: String,
    provenance: ProvenanceType = ProvenanceType.SIMULATED,
    referenceRange: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ProvenanceBadge(type = provenance)
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            if (referenceRange != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "Ref: $referenceRange",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyNoticeCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Emergency,
                contentDescription = "Emergency alert",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = "Medical Emergency Guidance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "If experiencing acute chest pressure, breathing distress, sudden severe headache, or severe symptoms, call 911 / emergency services immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Screen header matching the reference designs: back arrow, centered/left title, action button
 */
@Composable
fun BeyondHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    actionDescription: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (actionIcon != null && onActionClick != null) {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionDescription ?: "Action",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

/**
 * Sleek Segmented Pill Selector (Day | Week | Month | Year etc.)
 */
@Composable
fun PillTabSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Circular progress ring with glowing gradient stroke (for Overall Health Score & Calories)
 */
@Composable
fun MetricProgressRing(
    progress: Float,
    scoreText: String,
    scoreSubtitle: String? = null,
    ringColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    size: Dp = 110.dp,
    strokeWidth: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to ringColor.copy(alpha = 0.7f),
                    progress.coerceIn(0.01f, 1.0f) to ringColor,
                    1.0f to ringColor
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = scoreText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (scoreSubtitle != null) {
                Text(
                    text = scoreSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Compact Quick Stat Tile for Dashboard row (Heart Rate, HRV, Sleep, Steps)
 */
@Composable
fun QuickStatTile(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * Continuous smooth Bezier wave line chart with glowing gradient fill underneath
 */
@Composable
fun ContinuousWaveChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF00E5BE),
    fillGradient: Brush = Brush.verticalGradient(
        listOf(
            Color(0xFF00E5BE).copy(alpha = 0.35f),
            Color(0xFF00E5BE).copy(alpha = 0.0f)
        )
    ),
    strokeWidth: Dp = 2.5.dp
) {
    Canvas(modifier = modifier) {
        if (dataPoints.size < 2) return@Canvas

        val width = size.width
        val height = size.height

        val minVal = dataPoints.minOrNull() ?: 0f
        val maxVal = dataPoints.maxOrNull() ?: 100f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val stepX = width / (dataPoints.size - 1)

        val path = Path()
        val fillPath = Path()

        val points = dataPoints.mapIndexed { i, value ->
            val normalizedY = 1f - ((value - minVal) / range)
            val y = normalizedY * (height * 0.75f) + (height * 0.12f)
            Offset(i * stepX, y)
        }

        path.moveTo(points[0].x, points[0].y)
        fillPath.moveTo(points[0].x, height)
        fillPath.lineTo(points[0].x, points[0].y)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val cx = (p0.x + p1.x) / 2
            path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
        }

        fillPath.lineTo(points.last().x, height)
        fillPath.close()

        drawPath(path = fillPath, brush = fillGradient)
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Multi-segment Donut Chart for Nutrition (Carbs, Protein, Fat) with kcal count in center
 */
@Composable
fun MacroDonutChart(
    carbsPercent: Float,
    proteinPercent: Float,
    fatPercent: Float,
    centerKcal: Int,
    carbsColor: Color = Color(0xFF38BDF8),
    proteinColor: Color = Color(0xFF00E5BE),
    fatColor: Color = Color(0xFFFBBF24),
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 14.dp
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val total = (carbsPercent + proteinPercent + fatPercent).coerceAtLeast(1f)
            val carbsSweep = (carbsPercent / total) * 360f
            val proteinSweep = (proteinPercent / total) * 360f
            val fatSweep = (fatPercent / total) * 360f

            var currentAngle = -90f

            // Carbs arc
            drawArc(
                color = carbsColor,
                startAngle = currentAngle,
                sweepAngle = carbsSweep - 2f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            currentAngle += carbsSweep

            // Protein arc
            drawArc(
                color = proteinColor,
                startAngle = currentAngle,
                sweepAngle = proteinSweep - 2f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            currentAngle += proteinSweep

            // Fat arc
            drawArc(
                color = fatColor,
                startAngle = currentAngle,
                sweepAngle = fatSweep - 2f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$centerKcal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "kcal today",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Visual human body anatomical silhouette with glowing interactive system nodes
 */
@Composable
fun AnatomicalSilhouette(
    modifier: Modifier = Modifier,
    highlightedNode: String? = null,
    onNodeClick: ((String) -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2

            // Glowing aura rings
            drawCircle(
                color = Color(0xFF00E5BE).copy(alpha = 0.08f * pulseAlpha),
                radius = h * 0.38f,
                center = Offset(cx, h * 0.45f)
            )
            drawCircle(
                color = Color(0xFF00E5BE).copy(alpha = 0.05f),
                radius = h * 0.48f,
                center = Offset(cx, h * 0.45f),
                style = Stroke(width = 1.dp.toPx())
            )

            // Body Outline (Stylized holographic silhouette)
            val bodyColor = Color(0xFF38BDF8).copy(alpha = 0.6f)
            val strokeStyle = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

            // Head
            drawCircle(
                color = bodyColor,
                radius = h * 0.06f,
                center = Offset(cx, h * 0.12f),
                style = strokeStyle
            )
            // Neck
            drawLine(bodyColor, Offset(cx, h * 0.18f), Offset(cx, h * 0.22f), strokeWidth = 2.dp.toPx())

            // Torso & Shoulders
            val torsoPath = Path().apply {
                moveTo(cx - w * 0.28f, h * 0.24f)
                lineTo(cx + w * 0.28f, h * 0.24f)
                lineTo(cx + w * 0.20f, h * 0.52f)
                lineTo(cx - w * 0.20f, h * 0.52f)
                close()
            }
            drawPath(torsoPath, Color(0xFF0D2530))
            drawPath(torsoPath, bodyColor, style = strokeStyle)

            // Spine / Core Telemetry line
            drawLine(
                color = Color(0xFF00E5BE).copy(alpha = 0.8f),
                start = Offset(cx, h * 0.22f),
                end = Offset(cx, h * 0.52f),
                strokeWidth = 2.dp.toPx()
            )

            // Arms
            drawLine(bodyColor, Offset(cx - w * 0.28f, h * 0.24f), Offset(cx - w * 0.36f, h * 0.44f), strokeWidth = 2.dp.toPx())
            drawLine(bodyColor, Offset(cx - w * 0.36f, h * 0.44f), Offset(cx - w * 0.32f, h * 0.62f), strokeWidth = 2.dp.toPx())

            drawLine(bodyColor, Offset(cx + w * 0.28f, h * 0.24f), Offset(cx + w * 0.36f, h * 0.44f), strokeWidth = 2.dp.toPx())
            drawLine(bodyColor, Offset(cx + w * 0.36f, h * 0.44f), Offset(cx + w * 0.32f, h * 0.62f), strokeWidth = 2.dp.toPx())

            // Legs
            drawLine(bodyColor, Offset(cx - w * 0.14f, h * 0.52f), Offset(cx - w * 0.16f, h * 0.76f), strokeWidth = 2.dp.toPx())
            drawLine(bodyColor, Offset(cx - w * 0.16f, h * 0.76f), Offset(cx - w * 0.18f, h * 0.94f), strokeWidth = 2.dp.toPx())

            drawLine(bodyColor, Offset(cx + w * 0.14f, h * 0.52f), Offset(cx + w * 0.16f, h * 0.76f), strokeWidth = 2.dp.toPx())
            drawLine(bodyColor, Offset(cx + w * 0.16f, h * 0.76f), Offset(cx + w * 0.18f, h * 0.94f), strokeWidth = 2.dp.toPx())

            // Glowing Organ Nodes
            // 1. Brain
            drawCircle(Color(0xFF818CF8), radius = 6.dp.toPx(), center = Offset(cx, h * 0.12f))
            drawCircle(Color(0xFF818CF8).copy(alpha = 0.4f * pulseAlpha), radius = 12.dp.toPx(), center = Offset(cx, h * 0.12f))

            // 2. Cardiovascular (Heart)
            val heartCenter = Offset(cx - w * 0.06f, h * 0.32f)
            drawCircle(Color(0xFFFF5252), radius = 7.dp.toPx(), center = heartCenter)
            drawCircle(Color(0xFFFF5252).copy(alpha = 0.5f * pulseAlpha), radius = 14.dp.toPx(), center = heartCenter)

            // 3. Respiratory (Lungs)
            drawCircle(Color(0xFF38BDF8), radius = 5.dp.toPx(), center = Offset(cx + w * 0.08f, h * 0.32f))

            // 4. Metabolic (Liver/Pancreas)
            val metabolicCenter = Offset(cx, h * 0.42f)
            drawCircle(Color(0xFF00E5BE), radius = 6.dp.toPx(), center = metabolicCenter)
            drawCircle(Color(0xFF00E5BE).copy(alpha = 0.4f * pulseAlpha), radius = 12.dp.toPx(), center = metabolicCenter)

            // 5. Musculoskeletal (Knees/Joints)
            drawCircle(Color(0xFFFBBF24), radius = 4.dp.toPx(), center = Offset(cx - w * 0.16f, h * 0.76f))
            drawCircle(Color(0xFFFBBF24), radius = 4.dp.toPx(), center = Offset(cx + w * 0.16f, h * 0.76f))
        }
    }
}

/**
 * Geometric Lotus / Leaf Emblem matching Beyond Horizon brand logo
 */
@Composable
fun LotusLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    tint: Color = Color(0xFF00E5BE)
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2

        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

        // Center petal
        val centerPetal = Path().apply {
            moveTo(cx, h * 0.1f)
            cubicTo(cx - w * 0.15f, h * 0.4f, cx - w * 0.15f, h * 0.75f, cx, h * 0.9f)
            cubicTo(cx + w * 0.15f, h * 0.75f, cx + w * 0.15f, h * 0.4f, cx, h * 0.1f)
        }
        drawPath(centerPetal, tint, style = stroke)

        // Left petal
        val leftPetal = Path().apply {
            moveTo(cx - w * 0.08f, h * 0.25f)
            cubicTo(cx - w * 0.45f, h * 0.45f, cx - w * 0.4f, h * 0.8f, cx - w * 0.05f, h * 0.9f)
            cubicTo(cx - w * 0.2f, h * 0.75f, cx - w * 0.2f, h * 0.45f, cx - w * 0.08f, h * 0.25f)
        }
        drawPath(leftPetal, tint.copy(alpha = 0.85f), style = stroke)

        // Right petal
        val rightPetal = Path().apply {
            moveTo(cx + w * 0.08f, h * 0.25f)
            cubicTo(cx + w * 0.45f, h * 0.45f, cx + w * 0.4f, h * 0.8f, cx + w * 0.05f, h * 0.9f)
            cubicTo(cx + w * 0.2f, h * 0.75f, cx + w * 0.2f, h * 0.45f, cx + w * 0.08f, h * 0.25f)
        }
        drawPath(rightPetal, tint.copy(alpha = 0.85f), style = stroke)
    }
}

