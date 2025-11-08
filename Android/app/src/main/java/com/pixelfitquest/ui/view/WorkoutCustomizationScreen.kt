package com.pixelfitquest.ui.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.model.ExerciseType
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.ui.components.PixelArtButton
import com.pixelfitquest.viewmodel.WorkoutCustomizationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCustomizationScreen(
    onStartWorkout: (WorkoutPlan, String?) -> Unit,
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
                        PixelArtButton(
                            onClick = {
                                if (!uiState.isSaving && uiState.templateName.isNotBlank()) {
                                    viewModel.saveTemplate()
                                }
                            },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.width(200.dp).height(60.dp)
                        ) {
                            Text(
                                if (uiState.editMode) "Update Template" else "Save as Template"
                            )
                        }
                    }

                    PixelArtButton(
                        onClick = {
                            viewModel.getWorkoutPlan()?.let { plan ->
                                val templateName = uiState.templateName.ifBlank { "Workout" }
                                onStartWorkout(plan, templateName)
                            }
                        },
                        imageRes = R.drawable.button_unclicked,  // Your normal PNG
                        pressedRes = R.drawable.button_clicked,  // Your pressed PNG
                        modifier = Modifier.width(250.dp).height(60.dp)  // Reduced width from 350.dp
                    ) {
                        Text("Start Workout")
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
                    .weight(1.5f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ExerciseType.entries) { exercise ->
                    var isSelected by remember {
                        mutableStateOf(
                            uiState.selections.containsKey(
                                exercise
                            )
                        )
                    }
                    var localSets by remember(exercise, uiState.selections) {
                        mutableStateOf(uiState.selections[exercise]?.sets?.toString() ?: "3")
                    }
                    var localWeight by remember(exercise, uiState.selections) {
                        mutableStateOf(uiState.selections[exercise]?.weight?.toString() ?: "3")
                    }

                    // Sync local state when uiState.selections changes (e.g., loadTemplate)
                    LaunchedEffect(uiState.selections[exercise]) {
                        uiState.selections[exercise]?.let { item ->
                            localSets = item.sets.toString()
                            localWeight = item.weight.toString()
                        } ?: run {
                            localSets = "3"
                            localWeight = "0"
                        }
                        isSelected = uiState.selections.containsKey(exercise)
                    }

                    val focusRequesterSets = remember { FocusRequester() }
                    val focusRequesterWeight = remember { FocusRequester() }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    isSelected = !isSelected
                                    viewModel.toggleExercise(
                                        exercise,
                                        localSets.toIntOrNull() ?: 3,
                                        localWeight.toFloatOrNull() ?: 0f)
                                }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { isSelected = it
                                        viewModel.toggleExercise(
                                            exercise,
                                            localSets.toIntOrNull() ?: 3,
                                            localWeight.toFloatOrNull() ?: 0f
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = exercise.name.replace("_", " ").uppercase(),
                                    modifier = Modifier
                                        .weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                                if (isSelected) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly) {
                                            Text("Sets: ")
                                            OutlinedTextField(
                                                value = localSets,
                                                onValueChange = { newValue: String ->
                                                    localSets =
                                                        newValue.filter { char -> char.isDigit() }
                                                },
                                                modifier = Modifier
                                                    .width(50.dp)
                                                    .focusRequester(focusRequesterSets)
                                                    .onFocusChanged { focusState ->
                                                        if (!focusState.isFocused) {
                                                            val finalSets = localSets.toIntOrNull() ?: 3
                                                            viewModel.updateSets(exercise, finalSets)
                                                        }
                                                    },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = {
                                                        focusRequesterSets.freeFocus()
                                                        val finalSets = localSets.toIntOrNull() ?: 3
                                                        viewModel.updateSets(exercise, finalSets)
                                                    }
                                                )
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text("Weight:")
                                            OutlinedTextField(
                                                value = localWeight,
                                                onValueChange = { newValue: String ->
                                                    localWeight =
                                                        newValue.filter { char -> char.isDigit() || char == '.' }
                                                },
                                                modifier = Modifier
                                                    .width(65.dp)
                                                    .focusRequester(focusRequesterWeight)
                                                    .onFocusChanged { focusState ->
                                                        if (!focusState.isFocused) {
                                                            val finalWeight = localWeight.toFloatOrNull() ?: 0f
                                                            viewModel.updateWeight(
                                                                exercise,
                                                                finalWeight
                                                            )
                                                        }
                                                    },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Decimal,
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = {
                                                        focusRequesterWeight.freeFocus()
                                                        val finalWeight = localWeight.toFloatOrNull() ?: 3f
                                                        viewModel.updateWeight(
                                                            exercise,
                                                            finalWeight
                                                        )
                                                    }
                                                )
                                            )
                                            Text("Kg")
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
                        val totalSets =
                            template.plan.items.sumOf { (it.sets.coerceAtLeast(1)) }
                        Log.d("TemplateUI", "Template ${template.name}: total sets = $totalSets")

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ListItem(
                                headlineContent = { Text(template.name) },
                                supportingContent = {
                                    Text(
                                        "Exercises: ${template.plan.items.size} | Sets: $totalSets"
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
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "No templates yet. Name & save one to get started!",
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