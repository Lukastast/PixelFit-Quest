package com.pixelfitquest.feature.workoutBuilder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.workout.catalog.ExerciseCatalog
import com.pixelfitquest.feature.workout.catalog.ExerciseCategory
import com.pixelfitquest.feature.workout.catalog.ExerciseDefinition
import com.pixelfitquest.feature.workout.catalog.HeroPoseVisuals
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlanItem
import com.pixelfitquest.ui.theme.HeartRuby
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.SlateSurface
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.spacing
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
    var showImuInfoDialog by remember { mutableStateOf(false) }

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

    val spacing = MaterialTheme.spacing

    if (showImuInfoDialog) {
        AlertDialog(
            onDismissRequest = { showImuInfoDialog = false },
            containerColor = SlateDeep,
            titleContentColor = ImperialGold,
            textContentColor = SilverSteel,
            shape = RoundedCornerShape(spacing.cornerMd),
            modifier = Modifier.border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd)),
            title = {
                Text(
                    text = stringResource(R.string.imu_info_title),
                    fontWeight = FontWeight.Bold,
                    color = ImperialGold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.imu_info_body,
                        ExerciseCatalog.all.size,
                        ExerciseCatalog.imuSupportedCount,
                    ),
                    color = SilverSteel
                )
            },
            confirmButton = {
                TextButton(onClick = { showImuInfoDialog = false }) {
                    Text("Got it", color = ImperialGold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.scale(48))
                .padding(horizontal = spacing.md)
                .clip(RoundedCornerShape(spacing.cornerSm))
                .background(SlateGroove)
                .border(BorderStroke(1.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm)),
            contentAlignment = Alignment.Center,
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(R.string.exercise_search_hint),
                        color = SilverSlate,
                        style = typography.bodyMedium,
                    )
                },
                modifier = Modifier
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
                    cursorColor = ImperialGold,
                ),
            )
        }

        Spacer(modifier = Modifier.height(spacing.xs))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
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

        Spacer(modifier = Modifier.height(spacing.xxs))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            CatalogChip(
                label = stringResource(R.string.exercise_filter_imu),
                selected = trackingFilter == TrackingFilter.IMU,
                onClick = {
                    trackingFilter =
                        if (trackingFilter == TrackingFilter.IMU) TrackingFilter.ALL else TrackingFilter.IMU
                },
                onInfoClick = { showImuInfoDialog = true }
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

        Spacer(modifier = Modifier.height(spacing.xs))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.md)
                .padding(end = spacing.xs)
                .simpleVerticalScrollbar(listState),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            if (visible.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.exercise_no_matches),
                        style = typography.bodyMedium,
                        color = SilverSlate,
                        modifier = Modifier.padding(spacing.md),
                    )
                }
            } else {
                grouped.forEach { (category, defs) ->
                    item(key = "header_${category.name}") {
                        Text(
                            text = category.label.uppercase(),
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImperialGold,
                            modifier = Modifier.padding(top = spacing.xs, bottom = spacing.xxs),
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
    onInfoClick: (() -> Unit)? = null,
) {
    val spacing = MaterialTheme.spacing
    val bg = if (selected) SlateSurface else SlateGroove.copy(alpha = 0.85f)
    val borderStroke = if (selected) BorderStroke(1.5.dp, ImperialGold) else BorderStroke(1.dp, SlateBorder)
    val textColor = if (selected) ImperialGold else SilverSteel

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(bg)
            .border(borderStroke, RoundedCornerShape(spacing.cornerSm))
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.scale(10), vertical = spacing.scale(6)),
    ) {
        Text(
            text = label,
            style = typography.bodyMedium,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
        if (onInfoClick != null) {
            Spacer(modifier = Modifier.width(spacing.xxs))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "IMU Info",
                tint = if (selected) ImperialGold else SilverSlate,
                modifier = Modifier
                    .size(spacing.scale(16))
                    .clickable { onInfoClick() }
            )
        }
    }
}

@Composable
private fun TrackingBadge(imuSupported: Boolean) {
    val spacing = MaterialTheme.spacing
    val label = stringResource(
        if (imuSupported) R.string.exercise_badge_imu else R.string.exercise_badge_log,
    )
    val bg = if (imuSupported) VitalGreen.copy(alpha = 0.25f) else SlateGroove
    val borderColor = if (imuSupported) VitalGreen else SlateBorder
    val textColor = if (imuSupported) VitalGreen else SilverSlate

    Text(
        text = label,
        style = typography.bodyMedium,
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(bg)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(spacing.cornerXs))
            .padding(horizontal = spacing.scale(6), vertical = spacing.scale(2)),
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
    val spacing = MaterialTheme.spacing

    val cardBg = if (isSelected) SlateSurface.copy(alpha = 0.94f) else SlateDeep.copy(alpha = 0.94f)
    val cardBorder = if (isSelected) BorderStroke(1.5.dp, ImperialGold) else BorderStroke(1.dp, SlateBorder)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerMd))
            .background(cardBg)
            .border(cardBorder, RoundedCornerShape(spacing.cornerMd))
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
            )
            .padding(spacing.sm),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        checkedColor = ImperialGold,
                        uncheckedColor = SilverSteel.copy(alpha = 0.7f),
                        checkmarkColor = SlateDeep,
                    ),
                )
                HeroPoseVisuals.poseRes(exercise)?.let { poseRes ->
                    Image(
                        painter = painterResource(id = poseRes),
                        contentDescription = definition.displayName,
                        modifier = Modifier
                            .size(spacing.scale(52))
                            .padding(end = spacing.xs),
                        contentScale = ContentScale.Fit,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = definition.displayName.uppercase(),
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) ImperialGold else Color.White,
                    )
                    Text(
                        text = definition.subtitle,
                        style = typography.bodyMedium,
                        color = SilverSlate,
                        fontSize = 12.sp,
                    )
                }
                TrackingBadge(imuSupported = definition.imuSupported)
                Spacer(modifier = Modifier.width(spacing.xs))
            }

            if (isSelected) {
                Spacer(modifier = Modifier.height(spacing.xs))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(spacing.cornerSm))
                        .background(SlateGroove.copy(alpha = 0.6f))
                        .padding(horizontal = spacing.sm, vertical = spacing.xs),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text(
                        text = stringResource(R.string.sets_input_label),
                        style = typography.bodyMedium,
                        color = SilverSteel,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = localSets,
                        onValueChange = { newValue: String ->
                            localSets = newValue.filter { char -> char.isDigit() }
                        },
                        modifier = Modifier
                            .width(spacing.scale(60))
                            .height(spacing.scale(48))
                            .focusRequester(focusRequesterSets)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    val finalSets = localSets.toIntOrNull() ?: 3
                                    onUpdateSets(exercise, finalSets)
                                }
                            },
                        textStyle = typography.bodyMedium.copy(color = Color.White),
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
                    Spacer(modifier = Modifier.width(spacing.xs))
                    Text(
                        text = stringResource(R.string.weight_input_label),
                        style = typography.bodyMedium,
                        color = SilverSteel,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = localWeight,
                        onValueChange = { newValue: String ->
                            localWeight =
                                newValue.filter { char -> char.isDigit() || char == '.' }
                        },
                        modifier = Modifier
                            .width(spacing.scale(75))
                            .height(spacing.scale(48))
                            .focusRequester(focusRequesterWeight)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    val finalWeight = localWeight.toFloatOrNull() ?: 0f
                                    onUpdateWeight(exercise, finalWeight)
                                }
                            },
                        textStyle = typography.bodyMedium.copy(color = Color.White),
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
                        text = stringResource(R.string.kg_unit),
                        style = typography.bodyMedium,
                        color = SilverSlate,
                    )
                }
            }
        }
    }
}

@Composable
private fun catalogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = SilverSteel,
    disabledTextColor = SilverSlate,
    errorTextColor = HeartRuby,
    cursorColor = ImperialGold,
    focusedBorderColor = ImperialGold,
    unfocusedBorderColor = SlateBorder,
    disabledBorderColor = SlateBorderSubtle,
    errorBorderColor = HeartRuby,
    focusedContainerColor = SlateGroove,
    unfocusedContainerColor = SlateGroove,
)
