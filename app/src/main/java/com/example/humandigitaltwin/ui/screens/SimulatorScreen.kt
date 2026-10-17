package com.example.humandigitaltwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.humandigitaltwin.ui.components.VitalCard
import com.example.humandigitaltwin.viewmodel.DigitalTwinViewModel

@Composable
fun SimulatorScreen(viewModel: DigitalTwinViewModel) {
    val wearable by viewModel.wearableData.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Wearable Simulator", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Simulate streaming biometric telemetry feeds.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { viewModel.simulateWearable(false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Tick Normal")
                }
                Button(
                    onClick = { viewModel.simulateWearable(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Activity Spike")
                }
            }
        }

        item {
            VitalCard(title = "Heart Rate", value = "${wearable?.heart_rate ?: 0.0}", unit = "bpm", icon = Icons.Rounded.Favorite, description = "Realtime stream")
        }
        item {
            VitalCard(title = "HRV", value = "${wearable?.hrv ?: 0.0}", unit = "ms", icon = Icons.Rounded.ShowChart, description = "Autonomic tracking")
        }
        item {
            VitalCard(title = "Blood Glucose", value = "${wearable?.blood_glucose ?: 0.0}", unit = "mg/dL", icon = Icons.Rounded.Bolt, description = "Continuous glucose sensor")
        }
        item {
            VitalCard(title = "Daily Steps", value = "${wearable?.steps ?: 0}", unit = "steps", icon = Icons.Rounded.DirectionsWalk, description = "Step counter")
        }
    }
}
