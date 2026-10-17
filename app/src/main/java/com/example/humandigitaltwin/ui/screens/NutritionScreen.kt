package com.example.humandigitaltwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import com.example.humandigitaltwin.data.models.NutritionLog
import com.example.humandigitaltwin.ui.components.MacroDonutChart
import com.example.humandigitaltwin.ui.components.PillTabSelector
import com.example.humandigitaltwin.viewmodel.DigitalTwinViewModel

@Composable
fun NutritionScreen(
    viewModel: DigitalTwinViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val logs by viewModel.nutritionLogs.collectAsState()
    var selectedTimeframe by remember { mutableStateOf(0) } // 0: Today, 1: Week, 2: Month
    var showAddMealDialog by remember { mutableStateOf(false) }
    var mealToDelete by remember { mutableStateOf<NutritionLog?>(null) }

    // Seed realistic default meals from reference if empty
    LaunchedEffect(Unit) {
        if (logs.isEmpty()) {
            viewModel.addNutritionLog("Quinoa Salmon Bowl", 45.0, 30.0, 15.0)
            viewModel.addNutritionLog("Greek Yogurt & Berries", 30.0, 20.0, 5.0)
            viewModel.addNutritionLog("Mixed Nuts", 10.0, 6.0, 18.0)
        }
    }

    val totalCalories = if (logs.isNotEmpty()) logs.sumOf { it.calories } else 1420.0
    val totalCarbs = if (logs.isNotEmpty()) logs.sumOf { it.carbs_g } else 160.0
    val totalProtein = if (logs.isNotEmpty()) logs.sumOf { it.protein_g } else 89.0
    val totalFat = if (logs.isNotEmpty()) logs.sumOf { it.fat_g } else 47.0

    val carbCals = totalCarbs * 4.0
    val proteinCals = totalProtein * 4.0
    val fatCals = totalFat * 9.0
    val sumCals = (carbCals + proteinCals + fatCals).coerceAtLeast(1.0)

    val carbsPercent = ((carbCals / sumCals) * 100).toInt()
    val proteinPercent = ((proteinCals / sumCals) * 100).toInt()
    val fatPercent = ((fatCals / sumCals) * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header: Back button, centered "Nutrition", '+' Add meal button
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
                    text = "Nutrition",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { showAddMealDialog = true }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Meal",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. Segmented Pill Tabs: [ Today | Week | Month ]
        item {
            PillTabSelector(
                options = listOf("Today", "Week", "Month"),
                selectedIndex = selectedTimeframe,
                onSelect = { selectedTimeframe = it }
            )
        }

        // 3. Macro Donut Card (matching Screen 4 from reference)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Donut chart with calories
                    MacroDonutChart(
                        carbsPercent = carbsPercent.toFloat(),
                        proteinPercent = proteinPercent.toFloat(),
                        fatPercent = fatPercent.toFloat(),
                        centerKcal = totalCalories.toInt(),
                        size = 130.dp,
                        strokeWidth = 14.dp
                    )

                    // Right side: Macro Legend Breakdown
                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        MacroLegendItem(
                            label = "Carbs",
                            percent = carbsPercent,
                            dotColor = Color(0xFF38BDF8)
                        )
                        MacroLegendItem(
                            label = "Protein",
                            percent = proteinPercent,
                            dotColor = Color(0xFF00E5BE)
                        )
                        MacroLegendItem(
                            label = "Fat",
                            percent = fatPercent,
                            dotColor = Color(0xFFFBBF24)
                        )
                    }
                }
            }
        }

        // 4. "Log a Meal" Full-width Action Pill Button
        item {
            Button(
                onClick = { showAddMealDialog = true },
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
                    imageVector = Icons.Rounded.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log a Meal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 5. Section Header: Today's Meals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Meals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (logs.isNotEmpty()) {
                    Text(
                        text = "${logs.size} logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 6. Today's Meals List (matching Screen 4 in reference)
        items(logs) { meal ->
            MealRowCard(
                meal = meal,
                onDelete = { mealToDelete = meal }
            )
        }
    }

    // Add Meal Dialog
    if (showAddMealDialog) {
        AddMealDialog(
            onDismiss = { showAddMealDialog = false },
            onSave = { name, carbs, protein, fat ->
                viewModel.addNutritionLog(name, carbs, protein, fat)
                showAddMealDialog = false
            }
        )
    }

    // Delete Confirmation
    if (mealToDelete != null) {
        AlertDialog(
            onDismissRequest = { mealToDelete = null },
            title = { Text("Delete Meal Entry?") },
            text = { Text("Remove '${mealToDelete!!.meal_name}' from your daily nutrition log?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNutritionLogByName(mealToDelete!!.meal_name)
                    mealToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mealToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MacroLegendItem(label: String, percent: Int, dotColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MealRowCard(
    meal: NutritionLog,
    onDelete: () -> Unit
) {
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dish thumbnail avatar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                meal.meal_name.contains("Salmon", ignoreCase = true) -> Icons.Rounded.SetMeal
                                meal.meal_name.contains("Yogurt", ignoreCase = true) -> Icons.Rounded.Icecream
                                meal.meal_name.contains("Nut", ignoreCase = true) -> Icons.Rounded.Grass
                                else -> Icons.Rounded.LunchDining
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = meal.meal_name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${meal.carbs_g.toInt()}g C • ${meal.protein_g.toInt()}g P • ${meal.fat_g.toInt()}g F",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${meal.calories.toInt()} kcal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Peak ~${meal.predicted_glucose_spike.toInt()} mg/dL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun AddMealDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, carbs: Double, protein: Double, fat: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf(45f) }
    var protein by remember { mutableStateOf(30f) }
    var fat by remember { mutableStateOf(15f) }
    var error by remember { mutableStateOf<String?>(null) }

    val cals = (carbs * 4 + protein * 4 + fat * 9).toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a New Meal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Meal Description") },
                    placeholder = { Text("e.g. Avocado Toast with Poached Eggs") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Carbs: ${carbs.toInt()}g", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("${(carbs * 4).toInt()} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Slider(value = carbs, onValueChange = { carbs = it }, valueRange = 0f..180f)
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Protein: ${protein.toInt()}g", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("${(protein * 4).toInt()} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Slider(value = protein, onValueChange = { protein = it }, valueRange = 0f..120f)
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fat: ${fat.toInt()}g", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("${(fat * 9).toInt()} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Slider(value = fat, onValueChange = { fat = it }, valueRange = 0f..80f)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated Energy:", style = MaterialTheme.typography.bodySmall)
                        Text("$cals kcal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) {
                    error = "Please specify meal description"
                } else {
                    onSave(name.trim(), carbs.toDouble(), protein.toDouble(), fat.toDouble())
                }
            }) {
                Text("Save Meal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
