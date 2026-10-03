package com.pixelfitquest.feature.workoutBuilder

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.ui.theme.HeartRuby
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.PixelFitWidthClass
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
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
    onStartWorkout: (WorkoutPlan, String) -> Unit,
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
            onTemplateSaved()
        }
    }

    // Navigate to workout when startWorkout() emits — handles both save-then-start and
    // direct-start flows, so the button always works even after "Save as Template" cleared the form.
    LaunchedEffect(Unit) {
        viewModel.startWorkoutEvent.collect { (plan, name) ->
            onStartWorkout(plan, name)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape || spacing.widthClass != PixelFitWidthClass.Compact

    @Composable
    fun TemplateNameBox(modifier: Modifier = Modifier) {
        if (inTemplateMode || uiState.selections.isNotEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(spacing.cornerMd))
                    .background(SlateDeep.copy(alpha = 0.94f))
                    .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd))
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.editMode) {
                            stringResource(R.string.edit_template_name)
                        } else {
                            stringResource(R.string.template_name_label)
                        },
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ImperialGold
                    )

                    Spacer(modifier = Modifier.height(spacing.xs))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(spacing.inputHeight)
                            .clip(RoundedCornerShape(spacing.cornerSm))
                            .background(SlateGroove)
                            .border(BorderStroke(1.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm)),
                        contentAlignment = Alignment.Center
                    ) {
                        TextField(
                            singleLine = true,
                            value = uiState.templateName,
                            onValueChange = viewModel::setTemplateName,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.default_workout_name),
                                    color = SilverSlate,
                                    style = typography.bodyMedium
                                )
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth()
                                .padding(horizontal = spacing.xs),
                            textStyle = typography.bodyMedium.copy(color = Color.White),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = SilverSteel,
                                disabledTextColor = SilverSlate,
                                errorTextColor = HeartRuby,
                                cursorColor = ImperialGold
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ActionButtons(isVertical: Boolean = false) {
        val buttonHeight = if (isVertical) spacing.scale(38) else spacing.buttonHeight
        if (inTemplateMode) {
            if (isVertical) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
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
                        modifier = Modifier.fillMaxWidth().height(buttonHeight)
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
                        modifier = Modifier.fillMaxWidth().height(buttonHeight)
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
                        modifier = Modifier.weight(1f).height(buttonHeight)
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
                        modifier = Modifier.weight(1f).height(buttonHeight)
                    ) {
                        Text(
                            text = "Save & Start",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            if (isVertical) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
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
                            modifier = Modifier.fillMaxWidth().height(buttonHeight)
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
                        modifier = Modifier.fillMaxWidth().height(buttonHeight)
                    ) {
                        Text(
                            text = stringResource(R.string.start_workout),
                            fontSize = 15.sp,
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
                            modifier = Modifier.weight(1f).height(buttonHeight)
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
                            .height(buttonHeight)
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
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (useTwoPane) {
            val exercisesListState = rememberLazyListState()
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                // Left Pane (34%): Template info + Selected count + Actions
                Column(
                    modifier = Modifier
                        .weight(0.34f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    TemplateNameBox()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(spacing.cornerSm))
                            .background(SlateGroove.copy(alpha = 0.85f))
                            .border(BorderStroke(1.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
                            .padding(horizontal = spacing.sm, vertical = spacing.xs),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${uiState.selections.size} Exercises Selected",
                            color = ImperialGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (uiState.isSaving) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = ImperialGold,
                            trackColor = SlateGroove,
                        )
                    }

                    ActionButtons(isVertical = true)
                }

                // Right Pane (66%): Exercise Catalog Picker
                ExerciseCatalogPicker(
                    selections = uiState.selections,
                    listState = exercisesListState,
                    onToggle = { exercise, sets, weight ->
                        viewModel.toggleExercise(exercise, sets, weight)
                    },
                    onUpdateSets = viewModel::updateSets,
                    onUpdateWeight = viewModel::updateWeight,
                    modifier = Modifier
                        .weight(0.66f)
                        .fillMaxHeight(),
                )
            }
        } else {
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
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = ImperialGold,
                                trackColor = SlateGroove,
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                        }

                        ActionButtons(isVertical = false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = paddingValues.calculateBottomPadding())
                ) {
                    TemplateNameBox(modifier = Modifier.padding(horizontal = spacing.md))
                    Spacer(modifier = Modifier.height(spacing.xs))

                    val exercisesListState = rememberLazyListState()

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
}
