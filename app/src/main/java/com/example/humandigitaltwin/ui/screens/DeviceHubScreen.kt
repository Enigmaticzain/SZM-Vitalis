package com.example.humandigitaltwin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.humandigitaltwin.data.connectors.BatchImportSummary
import com.example.humandigitaltwin.viewmodel.DigitalTwinViewModel
import kotlinx.coroutines.launch

@Composable
fun DeviceHubScreen(
    viewModel: DigitalTwinViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isGlobalSyncing by viewModel.isDeviceSyncing.collectAsState()

    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importResultSummary by remember { mutableStateOf<BatchImportSummary?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header: Back button, centered "Connected Devices", sync icon
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
                    text = "Connected Devices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.syncAllDevices(context)
                            Toast.makeText(context, "Synchronizing devices...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isGlobalSyncing
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = "Sync",
                        tint = if (isGlobalSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. "+ Add Device" Dashed Border Pill Button (matching Screen 6 in reference)
        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .clickable { showAddDeviceDialog = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Device",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 3. Device Cards List (matching Screen 6 in reference)
        // Item 1: Android Health Connect
        item {
            ConnectedDeviceCard(
                icon = Icons.Rounded.Favorite,
                iconBg = Color(0xFF00382E),
                iconTint = Color(0xFF00E5BE),
                title = "Android Health Connect",
                status = "Connected • Last sync: 10:42",
                isConnected = true,
                metricsDescription = "Steps, Heart Rate, Sleep, Weight",
                onClick = {
                    coroutineScope.launch {
                        viewModel.syncAllDevices(context)
                        Toast.makeText(context, "Health Connect synchronized", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Item 2: Garmin Watch
        item {
            ConnectedDeviceCard(
                icon = Icons.Rounded.Watch,
                iconBg = Color(0xFF1E293B),
                iconTint = Color(0xFF38BDF8),
                title = "Garmin Watch",
                status = "Connected • Last sync: 10:35",
                isConnected = true,
                metricsDescription = "Heart Rate, HRV, Steps, Sleep",
                onClick = {
                    Toast.makeText(context, "Garmin Watch: Battery 88% • Signal: -62 dBm", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Item 3: Omron BP Monitor
        item {
            ConnectedDeviceCard(
                icon = Icons.Rounded.Speed,
                iconBg = Color(0xFF1F2428),
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "Omron BP Monitor",
                status = "Not Connected",
                isConnected = false,
                metricsDescription = "Tap to pair via Bluetooth",
                onClick = {
                    Toast.makeText(context, "Scanning for Bluetooth BLE Omron cuff...", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Item 4: Dexcom CGM
        item {
            ConnectedDeviceCard(
                icon = Icons.Rounded.Sensors,
                iconBg = Color(0xFF1F2428),
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "Dexcom CGM",
                status = "Not Connected",
                isConnected = false,
                metricsDescription = "Tap to set up",
                onClick = {
                    Toast.makeText(context, "Opening Dexcom API auth portal...", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 4. File Import Card for Health Records
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showImportDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.FileUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Import Health Records",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Upload CSV / JSON exports from Apple Health or Garmin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
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
    }

    // Add Device Modal
    if (showAddDeviceDialog) {
        AlertDialog(
            onDismissRequest = { showAddDeviceDialog = false },
            title = { Text("Pair New Health Device") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select hardware protocol to scan and bind:")
                    listOf("Bluetooth Low Energy (GATT)", "Continuous Glucose Monitor (CGM)", "Smart Scale (BLE Body Composition)", "Medical Oximeter").forEach { proto ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Scanning for $proto...", Toast.LENGTH_SHORT).show()
                                    showAddDeviceDialog = false
                                }
                        ) {
                            Text(proto, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDeviceDialog = false }) { Text("Close") }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        FileImportDialog(
            onDismiss = { showImportDialog = false },
            onImportCsv = { csv ->
                importResultSummary = viewModel.importCsvData(csv)
                showImportDialog = false
            },
            onImportJson = { json ->
                importResultSummary = viewModel.importJsonData(json)
                showImportDialog = false
            }
        )
    }

    // Import summary modal
    importResultSummary?.let { summaryResult ->
        AlertDialog(
            onDismissRequest = { importResultSummary = null },
            icon = { Icon(Icons.Rounded.TaskAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Import Completed") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Records Evaluated: ${summaryResult.totalRows}")
                    Text("• Accepted & Normalized: ${summaryResult.acceptedObservations.size}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("• Deduplicated: ${summaryResult.duplicateCount}")
                }
            },
            confirmButton = {
                TextButton(onClick = { importResultSummary = null }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun ConnectedDeviceCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    status: String,
    isConnected: Boolean,
    metricsDescription: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                    shape = RoundedCornerShape(14.dp),
                    color = iconBg,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isConnected) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5BE))
                            )
                        }
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isConnected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = metricsDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
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

@Composable
fun FileImportDialog(
    onDismiss: () -> Unit,
    onImportCsv: (String) -> Unit,
    onImportJson: (String) -> Unit
) {
    val sampleCsv = """
metric,value,unit,timestamp
HEART_RATE,68,bpm,1773500000000
RESTING_HEART_RATE,61,bpm,1773500000000
BLOOD_GLUCOSE,94,mg/dL,1773500000000
STEPS,8450,steps,1773500000000
BODY_WEIGHT,164.2,lbs,1773500000000
SLEEP_DURATION,7.8,hrs,1773500000000
HRV,65,ms,1773500000000
    """.trimIndent()

    val sampleJson = """
[
  {"metric": "HEART_RATE", "value": 72, "unit": "bpm"},
  {"metric": "BLOOD_GLUCOSE", "value": 5.2, "unit": "mmol/L"},
  {"metric": "BODY_WEIGHT", "value": 74.5, "unit": "kg"},
  {"metric": "STEPS", "value": 9200, "unit": "steps"}
]
    """.trimIndent()

    var activeText by remember { mutableStateOf(sampleCsv) }
    var isCsvMode by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Health Records") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Upload historical export data from Apple Health, Garmin, Dexcom, or lab spreadsheets.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isCsvMode,
                        onClick = {
                            isCsvMode = true
                            activeText = sampleCsv
                        },
                        label = { Text("CSV Sample") }
                    )
                    FilterChip(
                        selected = !isCsvMode,
                        onClick = {
                            isCsvMode = false
                            activeText = sampleJson
                        },
                        label = { Text("JSON Sample") }
                    )
                }

                OutlinedTextField(
                    value = activeText,
                    onValueChange = { activeText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                    label = { Text(if (isCsvMode) "CSV Payload" else "JSON Payload") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isCsvMode) onImportCsv(activeText) else onImportJson(activeText)
                }
            ) {
                Text("Process Ingestion")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

