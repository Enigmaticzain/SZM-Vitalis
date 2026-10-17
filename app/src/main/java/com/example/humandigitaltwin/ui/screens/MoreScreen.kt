package com.example.humandigitaltwin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.humandigitaltwin.ui.components.LotusLogoBadge

@Composable
fun MoreScreen(
    onNavigateToDevices: () -> Unit,
    onNavigateToAnatomy: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToSimulation: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToHospital: () -> Unit,
    onNavigateToDoctor: () -> Unit,
    onNavigateToRemoteCare: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Brand Header
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LotusLogoBadge(size = 48.dp)
                    Column {
                        Text(
                            text = "SZM Vitalis",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "szm • Engineered by Team Beyond Horizon",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Know Today, Live Better Tomorrow",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Core Digital Twin Modules
        item {
            Text(
                text = "Twin Core & Simulation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    MoreMenuRow(
                        icon = Icons.Rounded.AccessibilityNew,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Your Digital Twin (Anatomy)",
                        subtitle = "Multi-system physiological 3D & organ diagnostics",
                        onClick = onNavigateToAnatomy
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    MoreMenuRow(
                        icon = Icons.Rounded.Tune,
                        iconTint = Color(0xFF38BDF8),
                        title = "Simulation Sandbox",
                        subtitle = "Predict what-if effects of sleep, caffeine & carbs",
                        onClick = onNavigateToSimulation
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    MoreMenuRow(
                        icon = Icons.Rounded.Timeline,
                        iconTint = Color(0xFFA78BFA),
                        title = "Insights & Timeline",
                        subtitle = "Biometric patterns and chronological audit log",
                        onClick = onNavigateToInsights
                    )
                }
            }
        }

        // Hardware & Sovereignty
        item {
            Text(
                text = "Devices & Privacy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    MoreMenuRow(
                        icon = Icons.Rounded.Devices,
                        iconTint = Color(0xFF00E5BE),
                        title = "Connected Devices",
                        subtitle = "Garmin, Health Connect, CGM, BP monitors & file import",
                        onClick = onNavigateToDevices
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    MoreMenuRow(
                        icon = Icons.Rounded.Security,
                        iconTint = Color(0xFF34D399),
                        title = "Privacy & Data Governance",
                        subtitle = "Device-only storage, zero trackers, data export & purge",
                        onClick = onNavigateToPrivacy
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    MoreMenuRow(
                        icon = Icons.Rounded.Description,
                        iconTint = Color(0xFFFB923C),
                        title = "Lab Biomarker Reports",
                        subtitle = "Comprehensive blood assays, lipid panels & hs-CRP",
                        onClick = onNavigateToReports
                    )
                }
            }
        }

        // Clinical Step-Down & Hospital Modules
        item {
            Text(
                text = "Clinical & Care Suite",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    MoreMenuRow(
                        icon = Icons.Rounded.MedicalServices,
                        iconTint = Color(0xFF38BDF8),
                        title = "Attending Doctor Console",
                        subtitle = "Clinical overrides, triage protocols & SOAP notes",
                        onClick = onNavigateToDoctor
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    MoreMenuRow(
                        icon = Icons.Rounded.LocalHospital,
                        iconTint = Color(0xFFFF5252),
                        title = "Hospital Step-Down Unit",
                        subtitle = "Continuous ward telemetry & telemetry escalation",
                        onClick = onNavigateToHospital
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    MoreMenuRow(
                        icon = Icons.Rounded.Healing,
                        iconTint = Color(0xFFA78BFA),
                        title = "Remote Post-Discharge Care",
                        subtitle = "Recovery milestones, wound check-ins & vitals sync",
                        onClick = onNavigateToRemoteCare
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    MoreMenuRow(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Welcome & Onboarding Guide",
                        subtitle = "App boundaries, data provenance & device features",
                        onClick = onNavigateToOnboarding
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MoreMenuRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1
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
