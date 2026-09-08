package com.pixelfitquest.feature.workoutBuilder

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.components.atoms.PixelArtButton
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
    modifier: Modifier = Modifier,
    onScreenReady: () -> Unit = {}
) {
    val viewModel: WorkoutCustomizationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val templates by viewModel.templates.collectAsState()

    var selectedTemplateId by remember { mutableStateOf<String?>(null) }
    val spacing = MaterialTheme.spacing

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    LaunchedEffect(uiState.isSaving, uiState.editMode, uiState.error) {
        if (!uiState.isSaving && !uiState.editMode && uiState.error == null) {
            selectedTemplateId = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (uiState.selections.isNotEmpty() && uiState.templateName.isNotBlank()) {
                        PixelArtButton(
                            onClick = {
                                if (!uiState.isSaving && uiState.templateName.isNotBlank()) {
                                    viewModel.saveTemplate()
                                }
                            },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.width(spacing.scale(200)).height(spacing.buttonHeight)
                        ) {
                            Text(
                                if (uiState.editMode) stringResource(R.string.update_template) else stringResource(R.string.save_as_template)
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
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier.width(spacing.scale(250)).height(spacing.buttonHeight)
                    ) {
                        Text(stringResource(R.string.start_workout))
                    }

                    if (uiState.isSaving) {
                        Spacer(modifier = Modifier.height(spacing.xs))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.scale(50))
                        .padding(horizontal = spacing.md)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.info_background),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    Text(
                        text = stringResource(R.string.customize_workout_title),
                        style = typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.padding(top = spacing.xs))

                if (uiState.editMode || uiState.selections.isNotEmpty()) {
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
                                text = if (uiState.editMode) stringResource(R.string.edit_template_name) else stringResource(R.string.template_name_label),
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
                    Spacer(modifier = Modifier.height(spacing.md))
                }

                Spacer(modifier = Modifier.height(spacing.md))

                val exercisesListState = rememberLazyListState()

                ExerciseCatalogPicker(
                    selections = uiState.selections,
                    listState = exercisesListState,
                    onToggle = { exercise, sets, weight ->
                        viewModel.toggleExercise(exercise, sets, weight)
                    },
                    onUpdateSets = viewModel::updateSets,
                    onUpdateWeight = viewModel::updateWeight,
                    modifier = Modifier.weight(1.5f),
                )

                if (templates.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(spacing.md))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(spacing.scale(50))
                            .padding(horizontal = spacing.md)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.info_background),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                        Text(
                            text = stringResource(R.string.your_templates_title),
                            style = typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.padding(top = spacing.xs))

                    val templatesListState = rememberLazyListState()

                    LazyColumn(
                        state = templatesListState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = spacing.md)
                            .padding(end = spacing.md)
                            .padding(bottom = spacing.md)
                            .simpleVerticalScrollbar(templatesListState, width = spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        items(templates, key = { it.id }) { template ->
                            val totalSets =
                                template.plan.items.sumOf { (it.sets.coerceAtLeast(1)) }
                            Log.d("TemplateUI", "Template ${template.name}: total sets = $totalSets")

                            val isSelected = template.id == selectedTemplateId

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(spacing.scale(72))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.info_background_wider_workout),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillBounds
                                )
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
                                            stringResource(R.string.template_stats, template.plan.items.size, totalSets),
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
                                                    contentDescription = stringResource(R.string.edit_template_desc),
                                                    tint = Color.Black
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteTemplate(template.id) }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.delete_template_desc),
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
                        text = stringResource(R.string.no_templates_msg),
                        style = typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(spacing.md)
                    )
                }

            }
        }
    }
}
