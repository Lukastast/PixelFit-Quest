package com.pixelfitquest.feature.workoutBuilder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.ui.theme.spacing
import com.pixelfitquest.ui.theme.typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
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
    onBack: () -> Unit = {},
    isTemplateMode: Boolean = false,
    onTemplateSaved: () -> Unit = onBack,
    modifier: Modifier = Modifier,
    onScreenReady: () -> Unit = {}
) {
    val viewModel: WorkoutCustomizationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val inTemplateMode = isTemplateMode || uiState.isTemplateMode || uiState.editMode

    val spacing = MaterialTheme.spacing
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        onScreenReady()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Template saved!")
            }
            viewModel.resetSaveSuccess()
            if (inTemplateMode) {
                onTemplateSaved()
            }
        }
    }

    // Navigate to workout when startWorkout() emits — handles both save-then-start and
    // direct-start flows, so the button always works even after "Save as Template" cleared the form.
    LaunchedEffect(Unit) {
        viewModel.startWorkoutEvent.collect { (plan, name) ->
            onStartWorkout(plan, name)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = spacing.md, vertical = spacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isSaving) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(spacing.xs))
                    }

                    if (inTemplateMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PixelArtButton(
                                onClick = {
                                    if (uiState.selections.isEmpty()) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Please select at least one exercise")
                                        }
                                    } else if (!uiState.isSaving) {
                                        viewModel.saveTemplate {
                                            onTemplateSaved()
                                        }
                                    }
                                },
                                imageRes = R.drawable.button_unclicked,
                                pressedRes = R.drawable.button_clicked,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(spacing.buttonHeight)
                            ) {
                                Text(
                                    text = if (uiState.editMode) {
                                        stringResource(R.string.update_template)
                                    } else {
                                        stringResource(R.string.create_template_button)
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            PixelArtButton(
                                onClick = {
                                    if (uiState.selections.isEmpty()) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Please select at least one exercise")
                                        }
                                    } else if (!uiState.isSaving) {
                                        val planToStart = viewModel.getWorkoutPlan()
                                        val nameToStart = uiState.templateName.trim().ifBlank { "Custom Routine" }
                                        viewModel.saveTemplate {
                                            planToStart?.let { plan ->
                                                onStartWorkout(plan, nameToStart)
                                            }
                                        }
                                    }
                                },
                                imageRes = R.drawable.button_unclicked,
                                pressedRes = R.drawable.button_clicked,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(spacing.buttonHeight)
                            ) {
                                Text(
                                    text = "Save & Start",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.selections.isNotEmpty()) {
                                PixelArtButton(
                                    onClick = {
                                        if (!uiState.isSaving) {
                                            viewModel.saveTemplate()
                                        }
                                    },
                                    imageRes = R.drawable.button_unclicked,
                                    pressedRes = R.drawable.button_clicked,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(spacing.buttonHeight)
                                ) {
                                    Text(
                                        text = stringResource(R.string.save_as_template),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            PixelArtButton(
                                onClick = { viewModel.startWorkout() },
                                imageRes = R.drawable.button_unclicked,
                                pressedRes = R.drawable.button_clicked,
                                modifier = Modifier
                                    .weight(if (uiState.selections.isNotEmpty() && uiState.templateName.isNotBlank()) 1.2f else 1f)
                                    .height(spacing.buttonHeight)
                            ) {
                                Text(
                                    text = stringResource(R.string.start_workout),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                // Template Name Input (Always visible in template mode, or when exercises are selected)
                if (inTemplateMode || uiState.selections.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(spacing.barLg)
                            .padding(horizontal = spacing.md),
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
                                .padding(spacing.xs),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (uiState.editMode) {
                                    stringResource(R.string.edit_template_name)
                                } else {
                                    stringResource(R.string.template_name_label)
                                },
                                style = typography.bodyMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(spacing.xs))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(spacing.inputHeight),
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
                                    placeholder = {
                                        Text(
                                            text = stringResource(R.string.default_workout_name),
                                            color = Color.Black.copy(alpha = 0.5f),
                                            style = typography.bodyMedium
                                        )
                                    },
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
                    Spacer(modifier = Modifier.height(spacing.xs))
                }

                val exercisesListState = rememberLazyListState()

                // Exercise Catalog Picker now gets the entire remaining screen height!
                ExerciseCatalogPicker(
                    selections = uiState.selections,
                    listState = exercisesListState,
                    onToggle = { exercise, sets, weight ->
                        viewModel.toggleExercise(exercise, sets, weight)
                    },
                    onUpdateSets = viewModel::updateSets,
                    onUpdateWeight = viewModel::updateWeight,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }
    }
}
