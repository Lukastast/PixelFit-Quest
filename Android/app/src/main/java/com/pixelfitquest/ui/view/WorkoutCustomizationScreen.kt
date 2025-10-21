package com.pixelfitquest.ui.screens

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.model.WorkoutType
import com.pixelfitquest.viewmodel.WorkoutCustomizationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCustomizationScreen(
    onStartWorkout: (WorkoutPlan) -> Unit,  // Callback to pass plan and navigate
    modifier: Modifier = Modifier
) {
    val viewModel: WorkoutCustomizationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val templates by viewModel.templates.collectAsState()

    // Snackbar Host State for bottom positioning
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show error Snackbar when error changes
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },  // Positions Snackbar at bottom
        bottomBar = {
            // Fixed Bottom Bar with Buttons
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Save Template Button (Conditional)
                    if (uiState.selections.isNotEmpty() && uiState.templateName.isNotBlank()) {
                        Button(
                            onClick = {
                                viewModel.saveTemplate()
                            },
                            enabled = !uiState.isSaving && uiState.templateName.isNotBlank()
                        ) {
                            Text(
                                if (uiState.editMode) "Update Template" else "Save as Template"
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Start Workout Button (Only if selections not empty)
                    if (uiState.selections.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(32.dp))
                        Button(
                            onClick = {
                                viewModel.getWorkoutPlan()?.let { plan ->
                                    onStartWorkout(plan)
                                }
                            },
                            enabled = !uiState.isSaving
                        ) {
                            Text("Start Workout")
                        }
                    }

                    // Loading Indicator in Bottom Bar
                    if (uiState.isSaving) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Padding
            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Template Name Input (if editing or saving)
            if (uiState.editMode || uiState.selections.isNotEmpty()) {
                OutlinedTextField(
                    value = uiState.templateName,
                    onValueChange = viewModel::setTemplateName,
                    label = { Text(if (uiState.editMode) "Edit Template Name" else "Template Name (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Exercises Section: Separate Scrollable View
            Text(
                text = "Select Exercises",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .weight(2f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(WorkoutType.entries.toTypedArray()) { exercise ->
                    val isSelected by remember { derivedStateOf { uiState.selections.containsKey(exercise) } }
                    val currentSets by remember { derivedStateOf { uiState.selections[exercise] ?: 1 } }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.toggleExercise(exercise, currentSets)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    viewModel.toggleExercise(exercise, currentSets)
                                }
                            )
                            Text(
                                text = exercise.name.replace("_", " ").uppercase(),
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Sets: ")
                                    OutlinedTextField(
                                        value = currentSets.toString(),
                                        onValueChange = { newValue ->
                                            val newSets = newValue.toIntOrNull() ?: 3
                                            viewModel.toggleExercise(exercise, newSets)  // Updates via VM
                                        },
                                        modifier = Modifier.width(60.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (templates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Templates",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templates, key = { it.id }) { template ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ListItem(
                                headlineContent = { Text(template.name) },
                                supportingContent = {
                                    Text(
                                        "Exercises: ${template.plan.items.size} | Sets: ${template.plan.items.sumOf { it.sets }}"
                                    )
                                },
                                trailingContent = {
                                    Row {
                                        IconButton(
                                            onClick = { viewModel.loadTemplate(template) }
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Template"
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteTemplate(template.id) }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Template"
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                if (templates.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "No templates yet. Save one to get started!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}