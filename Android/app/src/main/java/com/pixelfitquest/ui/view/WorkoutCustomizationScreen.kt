package com.pixelfitquest.ui.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.model.ExerciseType
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.ui.components.PixelArtButton
import com.pixelfitquest.ui.theme.typography
import com.pixelfitquest.viewmodel.WorkoutCustomizationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.simpleVerticalScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    width: Dp = 8.dp
): Modifier = drawWithContent {
    drawContent()
    val layoutInfo = state.layoutInfo
    val needDrawScrollbar = if (layoutInfo.totalItemsCount > 0 && layoutInfo.visibleItemsInfo.isNotEmpty()) {
        val itemSize = layoutInfo.visibleItemsInfo[0].size.toFloat()
        val totalHeight = layoutInfo.totalItemsCount * itemSize
        val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
        totalHeight > viewportSize
    } else {
        false
    }

    if (needDrawScrollbar) {
        val firstVisibleElementIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
        val elementHeight = size.height / layoutInfo.totalItemsCount.coerceAtLeast(1)
        val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
        val scrollbarHeight = (layoutInfo.visibleItemsInfo.size * elementHeight).coerceAtMost(size.height)

        drawRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(size.width - width.toPx(), scrollbarOffsetY),
            size = Size(width.toPx(), scrollbarHeight)
        )
    }
}

@Composable
fun WorkoutCustomizationScreen(
    onStartWorkout: (WorkoutPlan, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: WorkoutCustomizationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val templates by viewModel.templates.collectAsState()

    // NEW: Track the currently selected template ID for visual indication
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),  // Make bottom bar transparent
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
        },
        containerColor = Color.Transparent,  // Make Scaffold transparent
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Padding
            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Exercises Section: Separate Scrollable View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)  // Adjust height for image
                    .padding(horizontal = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.info_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                Text(
                    text = "Customize Workout",
                    style = typography.bodyMedium,  // Smaller font
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }

            // Template Name Input (if editing or saving)
            if (uiState.editMode || uiState.selections.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)  // Adjust height for background image + field
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.info_background_higher),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.editMode) "Edit Template Name" else "Template Name",
                            style = typography.bodyMedium,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.inputfield),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )
                            TextField(
                                singleLine = true,
                                value = uiState.templateName,
                                onValueChange = viewModel::setTemplateName,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.96f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    disabledTextColor = Color.Black,
                                    errorTextColor = Color.Black,
                                    cursorColor = Color.Black
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val exercisesListState = rememberLazyListState()

            LazyColumn(
                state = exercisesListState,
                modifier = Modifier
                    .weight(1.5f)
                    .padding(horizontal = 16.dp)
                    .padding(end = 16.dp)
                    .simpleVerticalScrollbar(exercisesListState),
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isSelected) 140.dp else 80.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.info_background_wider_workout),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        isSelected = !isSelected
                                        viewModel.toggleExercise(
                                            exercise,
                                            localSets.toIntOrNull() ?: 3,
                                            localWeight.toFloatOrNull() ?: 0f
                                        )
                                    }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
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
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color.Black,
                                            uncheckedColor = Color.Black
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = exercise.name.replace("_", " ").uppercase(),
                                        modifier = Modifier
                                            .weight(1f),
                                        style = typography.bodyMedium,
                                        color = Color.Black
                                    )
                                }
                                if (isSelected) {
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Text("Sets: ", style = typography.bodyMedium, color = Color.Black)
                                        OutlinedTextField(
                                            value = localSets,
                                            onValueChange = { newValue: String ->
                                                localSets =
                                                    newValue.filter { char -> char.isDigit() }
                                            },
                                            modifier = Modifier
                                                .width(60.dp)
                                                .height(50.dp)
                                                .focusRequester(focusRequesterSets)
                                                .onFocusChanged { focusState ->
                                                    if (!focusState.isFocused) {
                                                        val finalSets = localSets.toIntOrNull() ?: 3
                                                        viewModel.updateSets(exercise, finalSets)
                                                    }
                                                },
                                            textStyle = MaterialTheme.typography.bodyMedium,
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
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black,
                                                disabledTextColor = Color.Black,
                                                errorTextColor = Color.Black,
                                                cursorColor = Color.Black,
                                                focusedBorderColor = Color.Black,
                                                unfocusedBorderColor = Color.Black,
                                                disabledBorderColor = Color.Black,
                                                errorBorderColor = Color.Black
                                            )
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Text("Weight:", style = typography.bodyMedium, color = Color.Black)
                                        OutlinedTextField(
                                            value = localWeight,
                                            onValueChange = { newValue: String ->
                                                localWeight =
                                                    newValue.filter { char -> char.isDigit() || char == '.' }
                                            },
                                            modifier = Modifier
                                                .width(75.dp)
                                                .height(50.dp)
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
                                            textStyle = typography.bodyMedium,
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
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black,
                                                disabledTextColor = Color.Black,
                                                errorTextColor = Color.Black,
                                                cursorColor = Color.Black,
                                                focusedBorderColor = Color.Black,
                                                unfocusedBorderColor = Color.Black,
                                                disabledBorderColor = Color.Black,
                                                errorBorderColor = Color.Black
                                            )
                                        )
                                        Text("Kg", style = typography.bodyMedium, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            if (templates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.info_background),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    Text(
                        text = "Your Templates",
                        style = typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.padding(top = 8.dp))

                val templatesListState = rememberLazyListState()

                LazyColumn(
                    state = templatesListState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .padding(end = 16.dp)
                        .padding(bottom = 16.dp)
                        .simpleVerticalScrollbar(templatesListState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templates, key = { it.id }) { template ->
                        val totalSets =
                            template.plan.items.sumOf { (it.sets.coerceAtLeast(1)) }
                        Log.d("TemplateUI", "Template ${template.name}: total sets = $totalSets")

                        // NEW: Check if this template is currently selected
                        val isSelected = template.id == selectedTemplateId

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.info_background_wider_workout),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )
                            // NEW: Dark overlay for selected template (50% opacity)
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                )
                            }
                            ListItem(
                                headlineContent = { Text(template.name, color = Color.Black) },
                                supportingContent = {
                                    Text(
                                        "Exercises: ${template.plan.items.size} | Sets: $totalSets",
                                        color = Color.Black
                                    )
                                },
                                trailingContent = {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                if (selectedTemplateId == template.id) {
                                                    selectedTemplateId = null
                                                    viewModel.clearTemplate()
                                                } else {
                                                    selectedTemplateId = template.id
                                                    viewModel.loadTemplate(template)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Template",
                                                tint = Color.Black
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteTemplate(template.id) }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Template",
                                                tint = Color.Black
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            if (selectedTemplateId == template.id) {
                                                selectedTemplateId = null
                                                viewModel.clearTemplate()
                                            } else {
                                                selectedTemplateId = template.id
                                                viewModel.loadTemplate(template)
                                            }
                                        }
                                    ),
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "No templates yet. Name & save one to get started!",
                    style = typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
        }
    }
}