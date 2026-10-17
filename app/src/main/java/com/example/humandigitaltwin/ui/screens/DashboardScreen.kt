package com.example.humandigitaltwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.humandigitaltwin.ui.components.*
import com.example.humandigitaltwin.viewmodel.DigitalTwinViewModel

@Composable
fun DashboardScreen(
    viewModel: DigitalTwinViewModel,
    onNavigateToVitals: () -> Unit = {},
    onNavigateToAnatomy: () -> Unit = {},
    onNavigateToNutrition: () -> Unit = {},
    onNavigateToDeviceHub: () -> Unit = {}
) {
    val twinState by viewModel.twinState.collectAsState()
    val isConnected by viewModel.isWebSocketConnected.collectAsState()
    val isOfflineDemo by viewModel.isOfflineDemo.collectAsState()
    val lastUpdated by viewModel.lastUpdatedTime.collectAsState()
    val summary by viewModel.adaptiveTwinSummary.collectAsState()

    val metrics = twinState?.latest_metrics
    val healthScore = twinState?.health_score ?: 92.4

    var selectedInfoTitle by remember { mutableStateOf<String?>(null) }
    var selectedInfoText by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header: Greeting & Date
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = "Morning",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Good Morning, Alex",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        selectedInfoTitle = "Date & Sync Status"
                        selectedInfoText = "Last synchronized: $lastUpdated. Continuous biometric telemetry stream is active."
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) Color(0xFF00E5BE) else Color(0xFFFB923C))
                        )
                        Text(
                            text = "Oct 12, 2026",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Overall Health Score Card (matching Screen 2 from reference)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAnatomy() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Overall Health Score",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${healthScore.toInt()}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (healthScore >= 90) "Excellent" else if (healthScore >= 80) "Optimal" else "Good",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Circular Progress Ring
                        MetricProgressRing(
                            progress = (healthScore.toFloat() / 100f).coerceIn(0f, 1f),
                            scoreText = "",
                            size = 86.dp,
                            strokeWidth = 9.dp,
                            ringColor = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.clickable {
                            selectedInfoTitle = "Health Score Computation"
                            selectedInfoText = "The Overall Health Score synthesizes nocturnal HRV (RMSSD), resting heart rate, sleep quality, and postprandial glycemic excursions into a weighted composite metric (0-100)."
                        }
                    ) {
                        Text(
                            text = "You're doing great! Your vitals are stable and showing positive trends.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 3. Quick Stat Tiles Row (4 stats matching Screen 2 in reference)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Heart Rate
                QuickStatTile(
                    icon = Icons.Rounded.Favorite,
                    iconTint = Color(0xFFFF5252),
                    value = "${metrics?.heart_rate?.toInt() ?: 68}",
                    label = "Heart Rate",
                    onClick = onNavigateToVitals,
                    modifier = Modifier.weight(1f)
                )

                // HRV
                QuickStatTile(
                    icon = Icons.AutoMirrored.Rounded.ShowChart,
                    iconTint = Color(0xFF38BDF8),
                    value = "${metrics?.hrv?.toInt() ?: 75} ms",
                    label = "HRV",
                    onClick = onNavigateToVitals,
                    modifier = Modifier.weight(1f)
                )

                // Sleep
                val sleepHours = metrics?.sleep_hours ?: 7.5
                val wholeHours = sleepHours.toInt()
                val minutes = ((sleepHours - wholeHours) * 60).toInt()
                QuickStatTile(
                    icon = Icons.Rounded.Bedtime,
                    iconTint = Color(0xFFA78BFA),
                    value = "${wholeHours}h ${minutes}m",
                    label = "Sleep",
                    onClick = onNavigateToVitals,
                    modifier = Modifier.weight(1f)
                )

                // Steps
                QuickStatTile(
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconTint = Color(0xFFFB923C),
                    value = "8,432",
                    label = "Steps",
                    onClick = onNavigateToVitals,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Today's Focus Card (matching Screen 2 in reference)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Today's Focus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.SmartToy,
                                    contentDescription = "Focus Assistant",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Keep it balanced",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "A light walk after meals can help glycemic stability.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Hardware & Connected Sensors Strip
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDeviceHub() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Devices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = summary.operatingMode.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Confidence: ${summary.overallConfidenceScore}% • ${summary.activeSensors.size} active streams",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                    TextButton(onClick = onNavigateToDeviceHub) {
                        Text("Manage →", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // 6. Quick Simulation Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.simulateWearable(false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Baseline Tick")
                }
                Button(
                    onClick = { viewModel.simulateWearable(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Rounded.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Simulate Spike")
                }
            }
        }
    }

    // Informational Metric Modal
    if (selectedInfoTitle != null) {
        AlertDialog(
            onDismissRequest = { selectedInfoTitle = null },
            icon = { Icon(Icons.Rounded.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(selectedInfoTitle!!) },
            text = {
                Text(
                    text = selectedInfoText ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedInfoTitle = null }) {
                    Text("Got it")
                }
            }
        )
    }
}
