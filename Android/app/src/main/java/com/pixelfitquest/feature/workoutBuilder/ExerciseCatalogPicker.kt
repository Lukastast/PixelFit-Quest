package com.pixelfitquest.feature.workoutBuilder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.workout.catalog.ExerciseCatalog
import com.pixelfitquest.feature.workout.catalog.ExerciseCategory
import com.pixelfitquest.feature.workout.catalog.ExerciseDefinition
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlanItem
import com.pixelfitquest.ui.theme.typography

private enum class TrackingFilter { ALL, IMU, LOG, SELECTED }

@Composable
fun ExerciseCatalogPicker(
    selections: Map<ExerciseType, WorkoutPlanItem>,
    listState: LazyListState,
    onToggle: (ExerciseType, Int, Float) -> Unit,
    onUpdateSets: (ExerciseType, Int) -> Unit,
    onUpdateWeight: (ExerciseType, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ExerciseCategory?>(null) }
    var trackingFilter by remember { mutableStateOf(TrackingFilter.ALL) }

    val visible = remember(searchQuery, selectedCategory, trackingFilter, selections) {
        ExerciseCatalog.search(
            query = searchQuery,
            category = selectedCategory,
            imuOnly = trackingFilter == TrackingFilter.IMU,
            logOnly = trackingFilter == TrackingFilter.LOG,
            selectedTypes = if (trackingFilter == TrackingFilter.SELECTED) selections.keys else null,
        )
    }
    val grouped = remember(visible) { ExerciseCatalog.grouped(visible) }

    Column(modifier = modifier) {
        Text(
            text = stringResource(
                R.string.exercise_catalog_stats,
                ExerciseCatalog.all.size,
                ExerciseCatalog.imuSupportedCount,
            ),
            style = typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Text(
            text = stringResource(R.string.exercise_catalog_hint),
            style = typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.inputfield),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(R.string.exercise_search_hint),
                        color = Color.Black.copy(alpha = 0.55f),
                        style = typography.bodyMedium,
                    )
                },
                modifier = Modifier.fillMaxWidth(0.96f),
                textStyle = typography.bodyMedium,
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
                    cursorColor = Color.Black,
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CatalogChip(
                label = stringResource(R.string.exercise_filter_all),
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
            )
            ExerciseCategory.entries.forEach { category ->
                CatalogChip(
                    label = category.label,
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = if (selectedCategory == category) null else category
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CatalogChip(
                label = stringResource(R.string.exercise_filter_imu),
                selected = trackingFilter == TrackingFilter.IMU,
                onClick = {
                    trackingFilter =
                        if (trackingFilter == TrackingFilter.IMU) TrackingFilter.ALL else TrackingFilter.IMU
                },
            )
            CatalogChip(
                label = stringResource(R.string.exercise_filter_log),
                selected = trackingFilter == TrackingFilter.LOG,
                onClick = {
                    trackingFilter =
                        if (trackingFilter == TrackingFilter.LOG) TrackingFilter.ALL else TrackingFilter.LOG
                },
            )
            CatalogChip(
                label = stringResource(R.string.exercise_filter_selected, selections.size),
                selected = trackingFilter == TrackingFilter.SELECTED,
                onClick = {
                    trackingFilter =
                        if (trackingFilter == TrackingFilter.SELECTED) TrackingFilter.ALL else TrackingFilter.SELECTED
                },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .padding(end = 16.dp)
                .simpleVerticalScrollbar(listState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (visible.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.exercise_no_matches),
                        style = typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                grouped.forEach { (category, defs) ->
                    item(key = "header_${category.name}") {
                        Text(
                            text = category.label.uppercase(),
                            style = typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                        )
                    }
                    items(defs, key = { it.type.name }) { definition ->
                        ExercisePickerRow(
                            definition = definition,
                            selectedItem = selections[definition.type],
                            onToggle = onToggle,
                            onUpdateSets = onUpdateSets,
                            onUpdateWeight = onUpdateWeight,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = typography.bodyMedium,
        color = if (selected) Color.Black else Color.White,
        modifier = Modifier
            .background(
                if (selected) Color.White else Color.Black.copy(alpha = 0.55f),
                RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun TrackingBadge(imuSupported: Boolean) {
    val label = stringResource(
        if (imuSupported) R.string.exercise_badge_imu else R.string.exercise_badge_log,
    )
    val bg = if (imuSupported) Color(0xFF1B5E20) else Color(0xFF424242)
    Text(
        text = label,
        style = typography.bodyMedium,
        color = Color.White,
        fontSize = 11.sp,
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
private fun ExercisePickerRow(
    definition: ExerciseDefinition,
    selectedItem: WorkoutPlanItem?,
    onToggle: (ExerciseType, Int, Float) -> Unit,
    onUpdateSets: (ExerciseType, Int) -> Unit,
    onUpdateWeight: (ExerciseType, Float) -> Unit,
) {
    val exercise = definition.type
    var isSelected by remember(exercise, selectedItem) { mutableStateOf(selectedItem != null) }
    var localSets by remember(exercise, selectedItem) {
        mutableStateOf(selectedItem?.sets?.toString() ?: "3")
    }
    var localWeight by remember(exercise, selectedItem) {
        mutableStateOf(selectedItem?.weight?.toString() ?: "0")
    }

    LaunchedEffect(selectedItem) {
        selectedItem?.let { item ->
            localSets = item.sets.toString()
            localWeight = item.weight.toString()
        } ?: run {
            localSets = "3"
            localWeight = "0"
        }
        isSelected = selectedItem != null
    }

    val focusRequesterSets = remember { FocusRequester() }
    val focusRequesterWeight = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isSelected) 156.dp else 96.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background_wider_workout),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        isSelected = !isSelected
                        onToggle(
                            exercise,
                            localSets.toIntOrNull() ?: 3,
                            localWeight.toFloatOrNull() ?: 0f,
                        )
                    },
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            isSelected = it
                            onToggle(
                                exercise,
                                localSets.toIntOrNull() ?: 3,
                                localWeight.toFloatOrNull() ?: 0f,
                            )
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.Black,
                            uncheckedColor = Color.Black,
                        ),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = definition.displayName.uppercase(),
                            style = typography.bodyMedium,
                            color = Color.Black,
                        )
                        Text(
                            text = definition.subtitle,
                            style = typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                        )
                    }
                    TrackingBadge(imuSupported = definition.imuSupported)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (isSelected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Text(
                            stringResource(R.string.sets_input_label),
                            style = typography.bodyMedium,
                            color = Color.Black,
                        )
                        OutlinedTextField(
                            value = localSets,
                            onValueChange = { newValue: String ->
                                localSets = newValue.filter { char -> char.isDigit() }
                            },
                            modifier = Modifier
                                .width(60.dp)
                                .height(50.dp)
                                .focusRequester(focusRequesterSets)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused) {
                                        val finalSets = localSets.toIntOrNull() ?: 3
                                        onUpdateSets(exercise, finalSets)
                                    }
                                },
                            textStyle = typography.bodyMedium,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusRequesterSets.freeFocus()
                                    val finalSets = localSets.toIntOrNull() ?: 3
                                    onUpdateSets(exercise, finalSets)
                                },
                            ),
                            colors = catalogFieldColors(),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.weight_input_label),
                            style = typography.bodyMedium,
                            color = Color.Black,
                        )
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
                                        onUpdateWeight(exercise, finalWeight)
                                    }
                                },
                            textStyle = typography.bodyMedium,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusRequesterWeight.freeFocus()
                                    val finalWeight = localWeight.toFloatOrNull() ?: 0f
                                    onUpdateWeight(exercise, finalWeight)
                                },
                            ),
                            colors = catalogFieldColors(),
                        )
                        Text(
                            stringResource(R.string.kg_unit),
                            style = typography.bodyMedium,
                            color = Color.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun catalogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.Black,
    errorTextColor = Color.Black,
    cursorColor = Color.Black,
    focusedBorderColor = Color.Black,
    unfocusedBorderColor = Color.Black,
    disabledBorderColor = Color.Black,
    errorBorderColor = Color.Black,
)
