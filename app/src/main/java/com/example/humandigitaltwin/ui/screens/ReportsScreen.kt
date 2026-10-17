package com.example.humandigitaltwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.humandigitaltwin.viewmodel.DigitalTwinViewModel

@Composable
fun ReportsScreen(viewModel: DigitalTwinViewModel) {
    val reportResult by viewModel.reportResult.collectAsState()
    val isParsing by viewModel.isParsingReport.collectAsState()

    LaunchedEffect(Unit) {
        if (reportResult == null) {
            viewModel.importMockReport("Q1 2026 Comprehensive Blood Panel")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Lab Report Parser & Biomarkers", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Automated OCR & LLM extraction of blood panels, genetic risk scores, and longevity reports.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Import & Parse Mock Blood Reports", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Select a simulated clinical PDF/lab scan to ingest into your digital twin:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.importMockReport("Comprehensive Blood Panel") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Blood Panel", style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = { viewModel.importMockReport("Lipid & Metabolic Panel") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Rounded.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Lipid Panel", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        if (isParsing) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator()
                        Text(text = "Extracting lab biomarkers & verifying confidence scores...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else if (reportResult != null) {
            val result = reportResult!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(text = result.file_name, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "AI Parser Confidence: ${result.confidence_score}%", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "${result.confidence_score}%",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        HorizontalDivider()
                        Text(text = "Risk Assessment:", style = MaterialTheme.typography.labelLarge)
                        Text(text = result.risk_assessment, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Text(text = "Extracted Lab Values & Biomarkers", style = MaterialTheme.typography.titleLarge)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.biomarkers.forEach { (biomarker, value) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Rounded.Biotech, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(text = biomarker, style = MaterialTheme.typography.bodyLarge)
                                }
                                Text(text = value.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Clinical Recommendations", style = MaterialTheme.typography.titleLarge)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.recommendations.forEach { rec ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Rounded.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Text(text = rec, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
