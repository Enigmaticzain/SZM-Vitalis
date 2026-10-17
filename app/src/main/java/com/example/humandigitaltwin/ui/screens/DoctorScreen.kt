package com.example.humandigitaltwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.humandigitaltwin.data.models.DoctorPatient
import com.example.humandigitaltwin.viewmodel.DigitalNurseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorScreen(viewModel: DigitalNurseViewModel) {
    val patients by viewModel.doctorPatients.collectAsState()
    val alerts by viewModel.doctorAlerts.collectAsState()

    var selectedPatientForReview by remember { mutableStateOf<DoctorPatient?>(null) }
    var reviewNotes by remember { mutableStateOf("") }
    var actionItem by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.refreshAllModules()
    }

    // Doctor Review Dialog — rendered at the composable level (not inside LazyColumn)
    if (selectedPatientForReview != null) {
        AlertDialog(
            onDismissRequest = { selectedPatientForReview = null },
            title = { Text("Clinical Review: ${selectedPatientForReview?.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = reviewNotes,
                        onValueChange = { reviewNotes = it },
                        label = { Text("Clinical Assessment Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = actionItem,
                        onValueChange = { actionItem = it },
                        label = { Text("Prescribed Action / Treatment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedPatientForReview?.let {
                            viewModel.submitDoctorReview(it.patient_id, reviewNotes, actionItem)
                        }
                        selectedPatientForReview = null
                    }
                ) {
                    Text("Save Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPatientForReview = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Demo disclaimer banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Physician Console Demonstration — Synthetic Cohort. Not linked to production hospital systems.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Clinical Alerts Banner
        if (alerts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Active Clinical Alerts (${alerts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        alerts.forEach { alert ->
                            Text(
                                text = "• ${alert.patient_name}: ${alert.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Monitored Patient Cohort",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(patients) { patient ->
            val riskColor = when (patient.risk_status) {
                "Critical" -> Color(0xFFB71C1C)
                "Warning" -> Color(0xFFE65100)
                else -> Color(0xFF2E7D32)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = patient.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Age: ${patient.age} | ID: ${patient.patient_id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = riskColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = patient.risk_status,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Health Score", style = MaterialTheme.typography.labelSmall)
                            Text(text = "${patient.health_score}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "Recovery Index", style = MaterialTheme.typography.labelSmall)
                            Text(text = "${patient.recovery_index}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "Heart Rate", style = MaterialTheme.typography.labelSmall)
                            Text(text = "${patient.latest_vitals["hr"] ?: "N/A"} bpm", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Notes: ${patient.clinical_notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            selectedPatientForReview = patient
                            reviewNotes = ""
                            actionItem = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Clinical Review & Action")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun DoctorScreenPreview() {
    // Preview stub
}
