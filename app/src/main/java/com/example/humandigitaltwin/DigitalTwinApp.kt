package com.example.humandigitaltwin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.humandigitaltwin.ui.screens.*
import com.example.humandigitaltwin.viewmodel.DigitalNurseViewModel
import com.example.humandigitaltwin.viewmodel.DigitalTwinViewModel

sealed interface TwinRoute {
    data object Dashboard : TwinRoute
    data object Vitals : TwinRoute
    data object Nutrition : TwinRoute
    data object Chat : TwinRoute
    data object More : TwinRoute

    // Secondary sub-screens
    data object Anatomical : TwinRoute
    data object Simulation : TwinRoute
    data object Timeline : TwinRoute
    data object DeviceHub : TwinRoute
    data object Privacy : TwinRoute
    data object Reports : TwinRoute
    data object Hospital : TwinRoute
    data object Doctor : TwinRoute
    data object RemoteCare : TwinRoute
    data object Confidence : TwinRoute
    data object Performance : TwinRoute
    data object Onboarding : TwinRoute
}

@Composable
fun DigitalTwinApp() {
    val viewModel: DigitalTwinViewModel = viewModel()
    val nurseViewModel: DigitalNurseViewModel = viewModel()
    var currentRoute by remember { mutableStateOf<TwinRoute>(TwinRoute.Dashboard) }

    // Systematic Android Back handling: Return to dashboard before exiting
    BackHandler(enabled = currentRoute !is TwinRoute.Dashboard) {
        currentRoute = when (currentRoute) {
            is TwinRoute.Anatomical,
            is TwinRoute.Simulation,
            is TwinRoute.Timeline,
            is TwinRoute.DeviceHub,
            is TwinRoute.Privacy,
            is TwinRoute.Reports,
            is TwinRoute.Hospital,
            is TwinRoute.Doctor,
            is TwinRoute.RemoteCare,
            is TwinRoute.Confidence,
            is TwinRoute.Performance -> TwinRoute.More
            else -> TwinRoute.Dashboard
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute !is TwinRoute.Onboarding) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 6.dp
                ) {
                    // 1. Home
                    NavigationBarItem(
                        selected = currentRoute is TwinRoute.Dashboard,
                        onClick = { currentRoute = TwinRoute.Dashboard },
                        icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 2. Vitals
                    NavigationBarItem(
                        selected = currentRoute is TwinRoute.Vitals,
                        onClick = { currentRoute = TwinRoute.Vitals },
                        icon = { Icon(Icons.Rounded.Favorite, contentDescription = "Vitals") },
                        label = { Text("Vitals") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 3. Nutrition
                    NavigationBarItem(
                        selected = currentRoute is TwinRoute.Nutrition,
                        onClick = { currentRoute = TwinRoute.Nutrition },
                        icon = { Icon(Icons.Rounded.Restaurant, contentDescription = "Nutrition") },
                        label = { Text("Nutrition") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 4. Coach
                    NavigationBarItem(
                        selected = currentRoute is TwinRoute.Chat,
                        onClick = { currentRoute = TwinRoute.Chat },
                        icon = { Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = "Coach") },
                        label = { Text("Coach") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 5. More
                    val isMoreActive = currentRoute is TwinRoute.More ||
                            currentRoute is TwinRoute.Anatomical ||
                            currentRoute is TwinRoute.Simulation ||
                            currentRoute is TwinRoute.Timeline ||
                            currentRoute is TwinRoute.DeviceHub ||
                            currentRoute is TwinRoute.Privacy ||
                            currentRoute is TwinRoute.Reports ||
                            currentRoute is TwinRoute.Hospital ||
                            currentRoute is TwinRoute.Doctor ||
                            currentRoute is TwinRoute.RemoteCare

                    NavigationBarItem(
                        selected = isMoreActive,
                        onClick = { currentRoute = TwinRoute.More },
                        icon = { Icon(Icons.Rounded.MoreHoriz, contentDescription = "More") },
                        label = { Text("More") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (currentRoute) {
                // 1. Onboarding
                is TwinRoute.Onboarding -> OnboardingScreen(
                    onCompleteOnboarding = { _, _ ->
                        currentRoute = TwinRoute.Dashboard
                    }
                )

                // 2. Dashboard (Home)
                is TwinRoute.Dashboard -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToVitals = { currentRoute = TwinRoute.Vitals },
                    onNavigateToAnatomy = { currentRoute = TwinRoute.Anatomical },
                    onNavigateToNutrition = { currentRoute = TwinRoute.Nutrition },
                    onNavigateToDeviceHub = { currentRoute = TwinRoute.DeviceHub }
                )

                // 3. Vitals
                is TwinRoute.Vitals -> VitalsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentRoute = TwinRoute.Dashboard }
                )

                // 4. Nutrition
                is TwinRoute.Nutrition -> NutritionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentRoute = TwinRoute.Dashboard }
                )

                // 5. AI Health Coach
                is TwinRoute.Chat -> ChatScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentRoute = TwinRoute.Dashboard }
                )

                // 6. More Hub
                is TwinRoute.More -> MoreScreen(
                    onNavigateToDevices = { currentRoute = TwinRoute.DeviceHub },
                    onNavigateToAnatomy = { currentRoute = TwinRoute.Anatomical },
                    onNavigateToInsights = { currentRoute = TwinRoute.Timeline },
                    onNavigateToSimulation = { currentRoute = TwinRoute.Simulation },
                    onNavigateToPrivacy = { currentRoute = TwinRoute.Privacy },
                    onNavigateToReports = { currentRoute = TwinRoute.Reports },
                    onNavigateToHospital = { currentRoute = TwinRoute.Hospital },
                    onNavigateToDoctor = { currentRoute = TwinRoute.Doctor },
                    onNavigateToRemoteCare = { currentRoute = TwinRoute.RemoteCare },
                    onNavigateToOnboarding = { currentRoute = TwinRoute.Onboarding }
                )

                // 7. Connected Devices
                is TwinRoute.DeviceHub -> DeviceHubScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentRoute = TwinRoute.More }
                )

                // 8. Anatomy (Digital Twin)
                is TwinRoute.Anatomical -> AnatomicalScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentRoute = TwinRoute.More }
                )

                // 9. Insights & Timeline
                is TwinRoute.Timeline -> TimelineScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentRoute = TwinRoute.More }
                )

                // 10. Simulation Sandbox
                is TwinRoute.Simulation -> SimulationScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentRoute = TwinRoute.More }
                )

                // 11. Privacy & Data
                is TwinRoute.Privacy -> PrivacyScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentRoute = TwinRoute.More }
                )

                // Supplementary clinical screens
                is TwinRoute.Reports -> ReportsScreen(viewModel)
                is TwinRoute.Hospital -> HospitalScreen(nurseViewModel)
                is TwinRoute.Doctor -> DoctorScreen(nurseViewModel)
                is TwinRoute.RemoteCare -> RemoteCareScreen(nurseViewModel)
                is TwinRoute.Confidence -> ConfidenceScreen(viewModel)
                is TwinRoute.Performance -> PerformanceScreen(viewModel)
            }
        }
    }
}
