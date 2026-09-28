package com.pixelfitquest.feature.workout

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.workout.catalog.ExerciseCatalog
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlanItem
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.PixelFitWidthClass
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.ui.theme.spacing
import java.util.Locale

@Composable
fun PreWorkoutLoadoutScreen(
    plan: WorkoutPlan,
    templateName: String?,
    exerciseLastReps: Map<String, Int>,
    isWeightSuggestionEnabled: Boolean,
    targetRepThreshold: Int,
    onBack: () -> Unit,
    onStartWorkout: (adjustedPlan: WorkoutPlan) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    val spacing = MaterialTheme.spacing
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape || spacing.widthClass != PixelFitWidthClass.Compact

    val editedItems = remember(plan) {
        mutableStateListOf<WorkoutPlanItem>().apply { addAll(plan.items) }
    }
    val appliedSuggestions = remember { mutableStateMapOf<Int, Boolean>() }

    val readyToOverloadCount = remember(editedItems, exerciseLastReps, isWeightSuggestionEnabled, targetRepThreshold) {
        if (!isWeightSuggestionEnabled) 0
        else editedItems.count { item ->
            val reps = exerciseLastReps[item.exercise.type] ?: 0
            reps >= targetRepThreshold
        }
    }

    val totalSets = remember(editedItems) { editedItems.sumOf { it.sets } }
    val totalEstVolume = remember(editedItems) {
        editedItems.sumOf { (it.weight * it.sets * 8).toDouble() }.toFloat()
    }

    val displayName = templateName?.takeIf { it.isNotBlank() } ?: "CUSTOM ROUTINE"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDeep.copy(alpha = 0.98f))
    ) {
        if (useTwoPane) {
            // Landscape & Wide Displays (Two-Pane)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.screen, vertical = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                // Left Pane: Routine info, progression summary & Action Buttons
                Column(
                    modifier = Modifier
                        .weight(0.36f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    LoadoutHeaderPlaque()

                    // Routine Summary Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(spacing.cornerSm))
                            .background(SlateGroove)
                            .border(BorderStroke(1.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
                            .padding(spacing.sm)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(spacing.scale(4))
                        ) {
                            Text(
                                text = displayName,
                                fontFamily = determination,
                                color = ImperialGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                            ) {
                                Text(
                                    text = "${editedItems.size} EXERCISES",
                                    color = SilverSteel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = "•", color = SilverSlate, fontSize = 11.sp)
                                Text(
                                    text = "$totalSets TOTAL SETS",
                                    color = SilverSteel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Smart Overload Summary Badge / Box
                    if (isWeightSuggestionEnabled && readyToOverloadCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(spacing.cornerSm))
                                .background(VitalGreen.copy(alpha = 0.12f))
                                .border(BorderStroke(1.5.dp, VitalGreen.copy(alpha = 0.7f)), RoundedCornerShape(spacing.cornerSm))
                                .padding(spacing.xs)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = VitalGreen,
                                    modifier = Modifier.size(spacing.scale(18))
                                )
                                Column {
                                    Text(
                                        text = if (readyToOverloadCount == 1) {
                                            stringResource(R.string.loadout_ready_badge, readyToOverloadCount)
                                        } else {
                                            stringResource(R.string.loadout_ready_badge_plural, readyToOverloadCount)
                                        },
                                        fontFamily = determination,
                                        color = VitalGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Hit target of $targetRepThreshold reps in last workout.",
                                        color = SilverSteel,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    } else if (isWeightSuggestionEnabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(spacing.cornerSm))
                                .background(SlateGroove.copy(alpha = 0.6f))
                                .border(BorderStroke(1.dp, SlateBorderSubtle), RoundedCornerShape(spacing.cornerSm))
                                .padding(spacing.xs)
                        ) {
                            Text(
                                text = "Progression target: hit $targetRepThreshold reps to unlock +2.5 kg suggestions.",
                                color = SilverSlate,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Left Column Action Buttons
                    PixelArtButton(
                        onClick = { onStartWorkout(WorkoutPlan(items = editedItems.toList())) },
                        imageRes = R.drawable.button_green,
                        pressedRes = R.drawable.button_green_clicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(spacing.scale(42))
                    ) {
                        Text(
                            text = "⚔ " + stringResource(R.string.loadout_start_button),
                            fontFamily = determination,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    PixelArtButton(
                        onClick = onBack,
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(spacing.scale(42))
                    ) {
                        Text(
                            text = stringResource(R.string.loadout_cancel_button),
                            fontFamily = determination,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Right Pane: Scrollable List of Exercise Loadout Cards
                LazyColumn(
                    modifier = Modifier
                        .weight(0.64f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    contentPadding = PaddingValues(bottom = spacing.sm)
                ) {
                    itemsIndexed(editedItems) { index, item ->
                        val lastReps = exerciseLastReps[item.exercise.type] ?: 0
                        val isProgressionHit = isWeightSuggestionEnabled && lastReps >= targetRepThreshold
                        val isApplied = appliedSuggestions[index] == true

                        LoadoutExerciseCard(
                            item = item,
                            lastReps = lastReps,
                            targetRepThreshold = targetRepThreshold,
                            isProgressionHit = isProgressionHit,
                            isApplied = isApplied,
                            onApplySuggestion = {
                                val updated = Math.round((item.weight + 2.5f) * 10f) / 10f
                                editedItems[index] = item.copy(weight = updated)
                                appliedSuggestions[index] = true
                            },
                            onAdjustWeight = { delta ->
                                val updated = (item.weight + delta).coerceAtLeast(0f)
                                val rounded = Math.round(updated * 10f) / 10f
                                editedItems[index] = item.copy(weight = rounded)
                            }
                        )
                    }
                }
            }
        } else {
            // Single-Pane Portrait Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.screen, vertical = spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadoutHeaderPlaque()

                Spacer(modifier = Modifier.height(spacing.xxs))

                // Routine info & Progression Hit banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove)
                        .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs))
                        .padding(horizontal = spacing.sm, vertical = spacing.xxs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            fontFamily = determination,
                            color = ImperialGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${editedItems.size} EXERCISES • $totalSets SETS",
                            color = SilverSteel,
                            fontSize = 10.sp
                        )
                    }

                    if (isWeightSuggestionEnabled && readyToOverloadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(spacing.cornerXs))
                                .background(VitalGreen.copy(alpha = 0.2f))
                                .border(1.dp, VitalGreen, RoundedCornerShape(spacing.cornerXs))
                                .padding(horizontal = spacing.xs, vertical = spacing.scale(2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "★ $readyToOverloadCount READY",
                                fontFamily = determination,
                                color = VitalGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.xs))

                // Scrollable List of Exercise Cards
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    contentPadding = PaddingValues(bottom = spacing.xs)
                ) {
                    itemsIndexed(editedItems) { index, item ->
                        val lastReps = exerciseLastReps[item.exercise.type] ?: 0
                        val isProgressionHit = isWeightSuggestionEnabled && lastReps >= targetRepThreshold
                        val isApplied = appliedSuggestions[index] == true

                        LoadoutExerciseCard(
                            item = item,
                            lastReps = lastReps,
                            targetRepThreshold = targetRepThreshold,
                            isProgressionHit = isProgressionHit,
                            isApplied = isApplied,
                            onApplySuggestion = {
                                val updated = Math.round((item.weight + 2.5f) * 10f) / 10f
                                editedItems[index] = item.copy(weight = updated)
                                appliedSuggestions[index] = true
                            },
                            onAdjustWeight = { delta ->
                                val updated = (item.weight + delta).coerceAtLeast(0f)
                                val rounded = Math.round(updated * 10f) / 10f
                                editedItems[index] = item.copy(weight = rounded)
                            }
                        )
                    }
                }

                // Sticky Bottom Action Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.xxs)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelArtButton(
                            onClick = onBack,
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier
                                .weight(1f)
                                .height(spacing.buttonHeight)
                        ) {
                            Text(
                                text = stringResource(R.string.loadout_cancel_button),
                                fontFamily = determination,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        PixelArtButton(
                            onClick = { onStartWorkout(WorkoutPlan(items = editedItems.toList())) },
                            imageRes = R.drawable.button_green,
                            pressedRes = R.drawable.button_green_clicked,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(spacing.buttonHeight)
                        ) {
                            Text(
                                text = "⚔ " + stringResource(R.string.loadout_start_button),
                                fontFamily = determination,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadoutHeaderPlaque(modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .height(spacing.scale(42)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.info_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = stringResource(R.string.loadout_title),
            fontFamily = determination,
            color = ImperialGold,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadoutExerciseCard(
    item: WorkoutPlanItem,
    lastReps: Int,
    targetRepThreshold: Int,
    isProgressionHit: Boolean,
    isApplied: Boolean,
    onApplySuggestion: () -> Unit,
    onAdjustWeight: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val definition = remember(item.exercise) {
        ExerciseCatalog.definition(item.exercise)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs)
        ) {
            // Row 1: Exercise Name & Tracking Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = definition.displayName,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.scale(6)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(if (definition.imuSupported) VitalGreen.copy(alpha = 0.16f) else SlateGroove)
                            .border(
                                1.dp,
                                if (definition.imuSupported) VitalGreen.copy(alpha = 0.6f) else SlateBorderSubtle,
                                RoundedCornerShape(spacing.cornerXs)
                            )
                            .padding(horizontal = spacing.scale(5), vertical = spacing.scale(2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (definition.imuSupported) "IMU" else "LOG",
                            color = if (definition.imuSupported) VitalGreen else SilverSlate,
                            fontFamily = determination,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${item.sets} SETS",
                        fontFamily = determination,
                        color = SilverSlate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Row 2: Smart Overload Banner (only when target was hit in previous session!)
            if (isProgressionHit) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(VitalGreen.copy(alpha = 0.12f))
                        .border(
                            1.dp,
                            VitalGreen.copy(alpha = 0.6f),
                            RoundedCornerShape(spacing.cornerXs)
                        )
                        .padding(horizontal = spacing.xs, vertical = spacing.scale(4))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.scale(5))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = VitalGreen,
                                modifier = Modifier.size(spacing.scale(14))
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.loadout_progression_hit_title),
                                    fontFamily = determination,
                                    color = VitalGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = stringResource(R.string.loadout_progression_hit_desc, lastReps, targetRepThreshold),
                                    color = SilverSteel,
                                    fontSize = 9.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(spacing.xs))

                        // Suggestion Action Chip
                        if (isApplied) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(spacing.cornerXs))
                                    .background(VitalGreen.copy(alpha = 0.25f))
                                    .border(1.dp, VitalGreen, RoundedCornerShape(spacing.cornerXs))
                                    .padding(horizontal = spacing.scale(6), vertical = spacing.scale(3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.loadout_suggestion_applied),
                                    fontFamily = determination,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VitalGreen
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(spacing.cornerXs))
                                    .background(VitalGreen)
                                    .clickable { onApplySuggestion() }
                                    .padding(horizontal = spacing.scale(8), vertical = spacing.scale(4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.loadout_apply_suggestion),
                                    fontFamily = determination,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // Row 3: Current Weight Display & Stepper + Plate Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.scale(2)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Weight Display with Minus / Plus Steppers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.scale(5))
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.scale(26))
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(SlateGroove)
                            .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs))
                            .clickable { onAdjustWeight(-2.5f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease weight",
                            tint = SilverSteel,
                            modifier = Modifier.size(spacing.scale(14))
                        )
                    }

                    Text(
                        text = if (item.weight % 1f == 0f) "${item.weight.toInt()} KG" else String.format(Locale.US, "%.1f KG", item.weight),
                        fontFamily = determination,
                        fontSize = 14.sp,
                        color = ImperialGold,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = spacing.scale(54))
                    )

                    Box(
                        modifier = Modifier
                            .size(spacing.scale(26))
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(SlateGroove)
                            .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs))
                            .clickable { onAdjustWeight(2.5f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase weight",
                            tint = SilverSteel,
                            modifier = Modifier.size(spacing.scale(14))
                        )
                    }
                }

                // Plate jump chips (-5, -2.5, +2.5, +5)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.scale(3)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoadoutPlateChip(text = "-5", delta = -5f, onClick = onAdjustWeight)
                    LoadoutPlateChip(text = "-2.5", delta = -2.5f, onClick = onAdjustWeight)
                    LoadoutPlateChip(text = "+2.5", delta = 2.5f, onClick = onAdjustWeight)
                    LoadoutPlateChip(text = "+5", delta = 5f, onClick = onAdjustWeight)
                }
            }
        }
    }
}

@Composable
private fun LoadoutPlateChip(
    text: String,
    delta: Float,
    onClick: (Float) -> Unit,
) {
    val spacing = LocalSpacing.current
    val isAdd = delta > 0
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(SlateGroove)
            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(spacing.cornerXs))
            .clickable { onClick(delta) }
            .padding(horizontal = spacing.scale(6), vertical = spacing.scale(3)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = determination,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAdd) VitalGreen else SilverSteel
        )
    }
}
