package com.example.humandigitaltwin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.humandigitaltwin.ui.components.ContinuousWaveChart
import com.example.humandigitaltwin.ui.components.PillTabSelector
import com.example.humandigitaltwin.viewmodel.DigitalTwinViewModel

@Composable
fun SimulationScreen(
    viewModel: DigitalTwinViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val result by viewModel.simulationResult.collectAsState()
    val twinState by viewModel.twinState.collectAsState()

    var selectedScenarioIndex by remember { mutableStateOf(0) } // 0: Sleep, 1: Caffeine, 2: Carbs, 3: Exercise
    var intensity by remember { mutableStateOf(0.80f) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val scenarioNames = listOf("Sleep", "Caffeine", "Carbs", "Exercise")
    val scenarioKeys = listOf("sleep_deprivation", "high_caffeine", "carb_surge", "exercise_stress")

    val baselineScore = twinState?.health_score ?: 92.4
    val baselineRecovery = twinState?.recovery_index ?: 89.2
    val baselineHrv = twinState?.latest_metrics?.hrv ?: 75.0

    // Dynamic projection calculations based on scenario and intensity
    val projectedScore = (baselineScore - (14f * intensity)).coerceIn(50.0, 100.0)
    val projectedRecovery = (baselineRecovery - (27f * intensity)).coerceIn(40.0, 100.0)
    val projectedHrv = (baselineHrv - (27f * intensity)).coerceIn(30.0, 100.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header: Back button, centered "What-If Simulation", info icon
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
                    text = "What-If Simulation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Scenario Category Selector Chips: [ Sleep | Caffeine | Carbs | Exercise ]
        item {
            PillTabSelector(
                options = scenarioNames,
                selectedIndex = selectedScenarioIndex,
                onSelect = { selectedScenarioIndex = it }
            )
        }

        // 3. Scenario Slider Card (matching Screen 9 from reference)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (selectedScenarioIndex) {
                                    0 -> "Sleep Deprivation"
                                    1 -> "Caffeine Surge"
                                    2 -> "Carbohydrate Spike"
                                    else -> "Intense Exercise"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (selectedScenarioIndex) {
                                    0 -> "7h → 4h"
                                    1 -> "0mg → 320mg"
                                    2 -> "15g → 110g carbs"
                                    else -> "Moderate → High Strain"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Intensity: ${(intensity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Slider
                    Slider(
                        value = intensity,
                        onValueChange = { intensity = it },
                        valueRange = 0.2f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // 4. Projected Impact Indicators (matching Screen 9 in reference)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Health Score
                ProjectedMetricTile(
                    title = "Health Score",
                    fromValue = "${baselineScore.toInt()}",
                    toValue = "${projectedScore.toInt()}",
                    isProjectedNegative = projectedScore < baselineScore,
                    modifier = Modifier.weight(1f)
                )

                // Recovery
                ProjectedMetricTile(
                    title = "Recovery",
                    fromValue = "${baselineRecovery.toInt()}%",
                    toValue = "${projectedRecovery.toInt()}%",
                    isProjectedNegative = projectedRecovery < baselineRecovery,
                    modifier = Modifier.weight(1f)
                )

                // HRV
                ProjectedMetricTile(
                    title = "HRV",
                    fromValue = "${baselineHrv.toInt()}",
                    toValue = "${projectedHrv.toInt()}ms",
                    isProjectedNegative = projectedHrv < baselineHrv,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Action Button: Teal Pill "Run Simulation"
        item {
            Button(
                onClick = {
                    viewModel.runSimulation(
                        scenarioKeys[selectedScenarioIndex],
                        intensity.toDouble(),
                        8
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Run Simulation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 6. Detailed Forecast Trajectory (when simulation has executed)
        if (result != null) {
            val res = result!!
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Simulation Trajectory Forecast",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Mini wave chart of forecasted score
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            val curve = if (res.time_series_projection.isNotEmpty()) {
                                res.time_series_projection.map { it.simulated_health_score.toFloat() }
                            } else {
                                listOf(92f, 85f, 80f, 78f, 78f, 82f, 88f)
                            }
                            ContinuousWaveChart(
                                dataPoints = curve,
                                lineColor = Color(0xFFFF5252),
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Text(
                            text = "Physiological Guidance Recommendations:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        res.recommendations.forEach { rec ->
                            Text(
                                text = "• $rec",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = { Icon(Icons.Rounded.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("What-If Simulation Sandbox") },
            text = {
                Text(
                    text = "Simulations run against your personal digital twin physiological model without altering historical observation records. Use it to forecast circadian disruption, glycemic surges, and training strain.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("Got it") }
            }
        )
    }
}

@Composable
private fun ProjectedMetricTile(
    title: String,
    fromValue: String,
    toValue: String,
    isProjectedNegative: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = fromValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "→",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = toValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isProjectedNegative) Color(0xFFFF5252) else Color(0xFF00E5BE)
                )
            }
            Text(
                text = "Projected",
                style = MaterialTheme.typography.labelSmall,
                color = if (isProjectedNegative) Color(0xFFFF5252).copy(alpha = 0.8f) else Color(0xFF00E5BE).copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}
