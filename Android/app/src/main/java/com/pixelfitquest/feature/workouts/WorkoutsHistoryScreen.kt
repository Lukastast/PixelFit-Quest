package com.pixelfitquest.feature.workouts

import android.content.res.Configuration
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.components.molecules.formatDate
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import com.pixelfitquest.ui.theme.PixelFitWidthClass
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.spacing

enum class WorkoutsTab {
    TEMPLATES,
    HISTORY
}

@Composable
fun WorkoutsHistoryScreen(
    onWorkoutClick: (String) -> Unit,
    onStartNewWorkout: () -> Unit,
    onCreateTemplate: () -> Unit = onStartNewWorkout,
    onEditTemplate: (String) -> Unit = {},
    onStartWorkout: (WorkoutPlan, String) -> Unit = { _, _ -> },
    viewModel: WorkoutsHistoryViewModel = hiltViewModel(),
) {
    val workouts by viewModel.workouts.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val spacing = MaterialTheme.spacing
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape || spacing.widthClass != PixelFitWidthClass.Compact

    var selectedTab by remember { mutableStateOf(WorkoutsTab.TEMPLATES) }
    var templateToDelete by remember { mutableStateOf<WorkoutTemplate?>(null) }

    if (useTwoPane) {
        // Landscape & Foldables: Single 2-Pane view with Templates on Left and History on Right
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.screen, vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            // Left Pane: Templates
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.workouts_tab_templates) + if (templates.isNotEmpty()) " (${templates.size})" else "",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(spacing.xs))
                    PixelArtButton(
                        onClick = onCreateTemplate,
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .width(spacing.scale(140))
                            .height(spacing.scale(36))
                    ) {
                        Text(
                            text = "⚔ " + stringResource(R.string.create_template_button),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                TemplatesList(
                    templates = templates,
                    onStartWorkout = onStartWorkout,
                    onEditTemplate = onEditTemplate,
                    onDeleteTemplate = { templateToDelete = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Right Pane: History
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.workouts_tab_history) + if (workouts.isNotEmpty()) " (${workouts.size})" else "",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(spacing.xs))
                    PixelArtButton(
                        onClick = onStartNewWorkout,
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .width(spacing.scale(140))
                            .height(spacing.scale(36))
                    ) {
                        Text(
                            text = stringResource(R.string.start_new_workout),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HistoryList(
                    workouts = workouts,
                    isLoading = isLoading,
                    onWorkoutClick = onWorkoutClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        // Portrait 1-Pane: Toggle between Templates and History tabs
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.screen, vertical = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (selectedTab == WorkoutsTab.TEMPLATES) {
                    stringResource(R.string.your_templates_title)
                } else {
                    stringResource(R.string.workouts_tab_history)
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = spacing.xs)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                PixelArtButton(
                    onClick = { selectedTab = WorkoutsTab.TEMPLATES },
                    imageRes = if (selectedTab == WorkoutsTab.TEMPLATES) R.drawable.button_clicked else R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(spacing.scale(42))
                ) {
                    Text(
                        text = stringResource(R.string.workouts_tab_templates) + if (templates.isNotEmpty()) " (${templates.size})" else "",
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == WorkoutsTab.TEMPLATES) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White
                    )
                }

                PixelArtButton(
                    onClick = { selectedTab = WorkoutsTab.HISTORY },
                    imageRes = if (selectedTab == WorkoutsTab.HISTORY) R.drawable.button_clicked else R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(spacing.scale(42))
                ) {
                    Text(
                        text = stringResource(R.string.workouts_tab_history) + if (workouts.isNotEmpty()) " (${workouts.size})" else "",
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == WorkoutsTab.HISTORY) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.sm))

            if (selectedTab == WorkoutsTab.TEMPLATES) {
                PixelArtButton(
                    onClick = onCreateTemplate,
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.buttonHeight)
                ) {
                    Text(
                        text = "⚔ " + stringResource(R.string.create_template_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                PixelArtButton(
                    onClick = onStartNewWorkout,
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.buttonHeight)
                ) {
                    Text(
                        text = stringResource(R.string.start_new_workout),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.md))

            when (selectedTab) {
                WorkoutsTab.TEMPLATES -> {
                    TemplatesList(
                        templates = templates,
                        onStartWorkout = onStartWorkout,
                        onEditTemplate = onEditTemplate,
                        onDeleteTemplate = { templateToDelete = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                WorkoutsTab.HISTORY -> {
                    HistoryList(
                        workouts = workouts,
                        isLoading = isLoading,
                        onWorkoutClick = onWorkoutClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    templateToDelete?.let { template ->
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = { Text(stringResource(R.string.delete_template_desc)) },
            text = { Text("Are you sure you want to delete template \"${template.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTemplate(template.id)
                        templateToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TemplatesList(
    templates: List<WorkoutTemplate>,
    onStartWorkout: (WorkoutPlan, String) -> Unit,
    onEditTemplate: (String) -> Unit,
    onDeleteTemplate: (WorkoutTemplate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    if (templates.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.no_templates_msg),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = spacing.md)
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = stringResource(R.string.create_first_template_hint),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = spacing.lg)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            contentPadding = PaddingValues(bottom = spacing.md)
        ) {
            items(templates, key = { it.id }) { template ->
                WorkoutTemplateRow(
                    template = template,
                    onStart = { onStartWorkout(template.plan, template.name) },
                    onEdit = { onEditTemplate(template.id) },
                    onDelete = { onDeleteTemplate(template) }
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    workouts: List<Workout>,
    isLoading: Boolean,
    onWorkoutClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else if (workouts.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No workouts yet!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = "Complete your first workout session to earn XP, coins, and level up your dwelling!",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = spacing.lg)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            contentPadding = PaddingValues(bottom = spacing.md)
        ) {
            items(workouts, key = { it.id }) { workout ->
                WorkoutHistoryRow(
                    workout = workout,
                    onClick = { onWorkoutClick(workout.id) }
                )
            }
        }
    }
}

@Composable
private fun WorkoutTemplateRow(
    template: WorkoutTemplate,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val totalSets = template.plan.items.sumOf { it.sets.coerceAtLeast(1) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.scale(80))
            .clickable { onStart() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = template.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(spacing.xxxs))
                Text(
                    text = stringResource(R.string.template_stats, template.plan.items.size, totalSets),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xxxs)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(spacing.scale(32))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_template_desc),
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(spacing.scale(18))
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(spacing.scale(32))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_template_desc),
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(spacing.scale(18))
                    )
                }

                Spacer(modifier = Modifier.width(spacing.xxs))

                PixelArtButton(
                    onClick = onStart,
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier
                        .width(spacing.scale(64))
                        .height(spacing.scale(34))
                ) {
                    Text(
                        text = stringResource(R.string.start_template),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryRow(
    workout: Workout,
    onClick: () -> Unit,
) {
    val spacing = MaterialTheme.spacing

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.scale(74))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = workout.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(spacing.xxxs))
                Text(
                    text = workout.date.formatDate(),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(spacing.xs))
            Text(
                text = "View >",
                color = RewardGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
