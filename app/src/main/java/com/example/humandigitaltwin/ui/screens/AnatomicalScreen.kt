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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.humandigitaltwin.ui.components.AnatomicalSilhouette
import com.example.humandigitaltwin.ui.components.PillTabSelector
import com.example.humandigitaltwin.viewmodel.DigitalTwinViewModel

data class OrganSystem(
    val name: String,
    val score: Int,
    val icon: ImageVector,
    val iconTint: Color,
    val details: String
)

@Composable
fun AnatomicalScreen(
    viewModel: DigitalTwinViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val twinState by viewModel.twinState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Body Systems, 1: 3D View
    var selectedSystem by remember { mutableStateOf<OrganSystem?>(null) }

    val liveRR = twinState?.latest_metrics?.respiratory_rate?.toInt() ?: 16

    val systems = listOf(
        OrganSystem("Brain", 94, Icons.Rounded.Psychology, Color(0xFF818CF8), "Cognitive readiness optimal. High autonomic parasympathetic tone recorded during REM cycle."),
        OrganSystem("Cardiovascular", 92, Icons.Rounded.Favorite, Color(0xFFFF5252), "Resting heart rate 64 bpm, HRV 74 ms. Arterial elasticity and pulse pressure optimal."),
        OrganSystem("Metabolic", 88, Icons.Rounded.Bolt, Color(0xFF00E5BE), "Fasting glucose 92 mg/dL. Postprandial glucose excursions safely buffer within target envelope."),
        OrganSystem("Respiratory", 96, Icons.Rounded.Air, Color(0xFF38BDF8), "SpO2 98%, resting respiratory rate $liveRR breaths/min. Pulmonary gas exchange efficient."),
        OrganSystem("Musculoskeletal", 90, Icons.Rounded.FitnessCenter, Color(0xFFFBBF24), "Recovery phase active. Micro-repair ongoing post physical activity."),
        OrganSystem("Immune", 89, Icons.Rounded.Shield, Color(0xFFA78BFA), "hs-CRP at 0.8 mg/L. Autonomic stress index low, cellular immunity balanced.")
    )

    val overallScore = twinState?.health_score?.toInt() ?: 92

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header: Back arrow, "Your Digital Twin"
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
                    text = "Your Digital Twin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // 2. Segmented Pill Tabs: [ Body Systems | 3D View ] (matching Screen 7)
        item {
            PillTabSelector(
                options = listOf("Body Systems", "3D View"),
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
        }

        // 3. Center Section: Anatomical Silhouette on Left, System Cards on Right
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Side: Glowing Holographic Body Silhouette
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(0.48f)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnatomicalSilhouette(modifier = Modifier.fillMaxSize())
                    }
                }

                // Right Side: System Health Breakdown Cards
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    systems.forEach { sys ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSystem = sys }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = sys.icon,
                                        contentDescription = sys.name,
                                        tint = sys.iconTint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = sys.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "${sys.score}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Overall Integrity Card (matching Screen 7 in reference)
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Integrity",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$overallScore% Excellent",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Linear progress bar in vibrant teal
                    LinearProgressIndicator(
                        progress = { (overallScore.toFloat() / 100f).coerceIn(0f, 1f) },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            }
        }
    }

    if (selectedSystem != null) {
        val sys = selectedSystem!!
        AlertDialog(
            onDismissRequest = { selectedSystem = null },
            icon = { Icon(imageVector = sys.icon, contentDescription = null, tint = sys.iconTint) },
            title = { Text(sys.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "System Score: ${sys.score}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = sys.details,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSystem = null }) {
                    Text("Close")
                }
            }
        )
    }
}
