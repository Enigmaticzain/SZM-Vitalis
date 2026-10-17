package com.example.humandigitaltwin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.humandigitaltwin.ui.components.ContinuousWaveChart
import com.example.humandigitaltwin.ui.components.PillTabSelector
import com.example.humandigitaltwin.viewmodel.DigitalTwinViewModel

data class InsightCardItem(
    val title: String,
    val status: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val miniWave: List<Float>,
    val details: String
)

@Composable
fun TimelineScreen(
    viewModel: DigitalTwinViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Insights, 1: Timeline
    var selectedInsight by remember { mutableStateOf<InsightCardItem?>(null) }

    val insights = listOf(
        InsightCardItem(
            title = "Sleep Quality",
            status = "Improved",
            subtitle = "+12% vs last week",
            icon = Icons.Rounded.Bedtime,
            iconTint = Color(0xFF34D399),
            miniWave = listOf(40f, 45f, 50f, 55f, 70f, 85f, 92f),
            details = "Deep and REM sleep cycles accounted for 42% of total sleep duration, reflecting improved autonomic parasympathetic restoration."
        ),
        InsightCardItem(
            title = "Glycemic Stability",
            status = "Stable",
            subtitle = "Within target range",
            icon = Icons.AutoMirrored.Rounded.TrendingFlat,
            iconTint = Color(0xFF00E5BE),
            miniWave = listOf(70f, 72f, 68f, 74f, 70f, 71f, 69f),
            details = "Postprandial glucose excursions stayed below 135 mg/dL with minimal glycemic variability (MAGE <25 mg/dL)."
        ),
        InsightCardItem(
            title = "Activity Trend",
            status = "On Track",
            subtitle = "8,432 steps today",
            icon = Icons.Rounded.BarChart,
            iconTint = Color(0xFF38BDF8),
            miniWave = listOf(20f, 35f, 40f, 60f, 75f, 80f, 85f),
            details = "Daily movement goals reached 105% of baseline. Active energy expenditure estimated at 540 kcal."
        ),
        InsightCardItem(
            title = "Recovery",
            status = "Good",
            subtitle = "HRV is consistent",
            icon = Icons.Rounded.Favorite,
            iconTint = Color(0xFF34D399),
            miniWave = listOf(60f, 65f, 70f, 72f, 75f, 74f, 75f),
            details = "Resting HRV remained at 75 ms RMSSD with steady vagal nerve modulation."
        )
    )

    val timelineEvents = listOf(
        Triple("10:30 PM", "Sleep Cycle Initiated", "Deep sleep phase started. Projected duration: 7h 45m."),
        Triple("07:45 PM", "Post-Workout Recovery", "HRV rebounded to 78ms following evening resistance training."),
        Triple("01:15 PM", "Postprandial Glycemic Peak", "Glucose peaked at 128 mg/dL after Quinoa Salmon Bowl."),
        Triple("08:30 AM", "Morning Baseline Recorded", "Fasting glucose 92 mg/dL, HRV 75ms, Health Score 92."),
        Triple("06:00 AM", "Wearable Sync", "Android Health Connect & Garmin telemetry synchronized.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header: Back button, centered "Insights & Timeline"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Insights & Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // 2. Segmented Pill Tabs: [ Insights | Timeline ] (matching Screen 8)
        item {
            PillTabSelector(
                options = listOf("Insights", "Timeline"),
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
        }

        // 3. Tab Content
        if (selectedTab == 0) {
            // Insights Tab: Cards matching Screen 8
            items(insights.size) { index ->
                val insight = insights[index]
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedInsight = insight }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = insight.icon,
                                        contentDescription = insight.title,
                                        tint = insight.iconTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = insight.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${insight.status} (${insight.subtitle})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = insight.iconTint,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Mini wave preview graph
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(32.dp)
                                .padding(end = 8.dp)
                        ) {
                            ContinuousWaveChart(
                                dataPoints = insight.miniWave,
                                lineColor = insight.iconTint,
                                strokeWidth = 1.8.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
            // Timeline Tab: Chronological event stream
            items(timelineEvents.size) { index ->
                val (time, title, desc) = timelineEvents[index]
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedInsight != null) {
        val ins = selectedInsight!!
        AlertDialog(
            onDismissRequest = { selectedInsight = null },
            icon = { Icon(imageVector = ins.icon, contentDescription = null, tint = ins.iconTint) },
            title = { Text(ins.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${ins.status}: ${ins.subtitle}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ins.iconTint
                    )
                    Text(
                        text = ins.details,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedInsight = null }) { Text("Got it") }
            }
        )
    }
}
