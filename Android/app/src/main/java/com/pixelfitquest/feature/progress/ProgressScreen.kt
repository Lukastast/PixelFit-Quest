package com.pixelfitquest.feature.progress

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.feature.progress.model.ExerciseProgressSeries
import com.pixelfitquest.feature.progress.model.ProgressDataSource
import com.pixelfitquest.feature.progress.model.SessionProgressPoint
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workout.model.enums.displayName
import com.pixelfitquest.ui.theme.BronzeCopper
import com.pixelfitquest.ui.theme.CrystalCyan
import com.pixelfitquest.ui.theme.FireOrange
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.PixelFitWidthClass
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.SlateSurface
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.ui.theme.spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
    onScreenReady: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) onScreenReady()
    }

    val spacing = MaterialTheme.spacing
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape || spacing.widthClass != PixelFitWidthClass.Compact

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = ImperialGold)
                Spacer(modifier = Modifier.height(spacing.md))
                Text(
                    text = stringResource(R.string.loading),
                    color = SilverSteel,
                    fontFamily = determination,
                    fontSize = 16.sp
                )
            }
        }
        return
    }

    val series = uiState.selectedSeries
    val sourceLabel = when (uiState.overview.source) {
        ProgressDataSource.LOCAL -> stringResource(R.string.progress_source_local)
        ProgressDataSource.WORKOUT_LOG -> stringResource(R.string.progress_source_workout_log)
        ProgressDataSource.SAMPLE -> stringResource(R.string.progress_source_sample)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = if (useTwoPane) spacing.md else spacing.screen,
                vertical = if (useTwoPane) spacing.xs else spacing.sm
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top Header
        ProgressHeader(
            onBack = onBack,
            sourceLabel = sourceLabel,
            isSample = uiState.overview.isSample,
            useTwoPane = useTwoPane,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(if (useTwoPane) spacing.xs else spacing.sm))

        if (uiState.overview.isSample) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateGroove)
                    .border(1.dp, TorchAmber.copy(alpha = 0.5f), RoundedCornerShape(spacing.cornerXs))
                    .padding(horizontal = spacing.sm, vertical = spacing.scale(6))
            ) {
                Text(
                    text = stringResource(R.string.progress_sample_banner),
                    color = ImperialGold,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(spacing.xs))
        }

        if (uiState.overview.series.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.progress_no_exercises),
                    color = SilverSlate,
                    textAlign = TextAlign.Center,
                    fontFamily = determination,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(spacing.xl),
                )
            }
        } else if (useTwoPane) {
            // Two-Pane (Landscape / Tablet): Balanced side-by-side columns
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                // Left Column: Exercise selector, Hero Overview, Est. 1RM & Form Quality
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    ExerciseChipRow(
                        series = uiState.overview.series,
                        selected = uiState.selectedExercise,
                        onSelect = viewModel::selectExercise,
                    )

                    if (series != null) {
                        ProgressHeroStage(series = series)
                        Estimated1RmCard(series = series)
                        MovementQualityCard(series = series)
                    }
                }

                // Right Column: Charts & Recent Sessions Log
                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    if (series != null) {
                        WeightChartCard(series = series)
                        VolumeChartCard(series = series)
                        RecentSessionsCard(series = series)
                    }
                }
            }
        } else {
            // Portrait: Single vertical scrollable column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                ExerciseChipRow(
                    series = uiState.overview.series,
                    selected = uiState.selectedExercise,
                    onSelect = viewModel::selectExercise,
                )

                if (series != null) {
                    ProgressHeroStage(series = series)
                    WeightChartCard(series = series)
                    VolumeChartCard(series = series)
                    Estimated1RmCard(series = series)
                    MovementQualityCard(series = series)
                    RecentSessionsCard(series = series)
                    Spacer(modifier = Modifier.height(spacing.md))
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(
    onBack: () -> Unit,
    sourceLabel: String,
    isSample: Boolean,
    useTwoPane: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.scale(46))
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
            .padding(horizontal = spacing.xs, vertical = spacing.scale(4)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(spacing.scale(36))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back_desc),
                    tint = SilverSteel,
                    modifier = Modifier.size(spacing.scale(20))
                )
            }

            Spacer(modifier = Modifier.width(spacing.xxs))

            Text(
                text = stringResource(R.string.progress_title),
                color = ImperialGold,
                fontFamily = determination,
                fontWeight = FontWeight.Bold,
                fontSize = if (useTwoPane) 16.sp else 18.sp,
                modifier = Modifier.weight(1f)
            )

            // Source indicator badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateGroove)
                    .border(
                        1.dp,
                        if (isSample) TorchAmber.copy(alpha = 0.5f) else SlateBorderSubtle,
                        RoundedCornerShape(spacing.cornerXs)
                    )
                    .padding(horizontal = spacing.scale(8), vertical = spacing.scale(4)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sourceLabel,
                    color = if (isSample) TorchAmber else SilverSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(spacing.xs))
        }
    }
}

@Composable
private fun ExerciseChipRow(
    series: List<ExerciseProgressSeries>,
    selected: ExerciseType?,
    onSelect: (ExerciseType) -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        series.forEach { item ->
            val isSelected = item.exerciseType == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(if (isSelected) FireOrange.copy(alpha = 0.22f) else SlateDeep)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) ImperialGold else SlateBorder,
                        shape = RoundedCornerShape(spacing.cornerXs),
                    )
                    .clickable { onSelect(item.exerciseType) }
                    .padding(horizontal = spacing.scale(12), vertical = spacing.scale(7)),
            ) {
                Text(
                    text = item.exerciseType.displayName(),
                    color = if (isSelected) ImperialGold else SilverSteel,
                    fontFamily = determination,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun ProgressHeroStage(series: ExerciseProgressSeries) {
    val spacing = LocalSpacing.current
    val delta = series.weightDeltaKg
    val isPositive = delta != null && delta >= 0f
    val deltaColor = if (isPositive) VitalGreen else BronzeCopper
    val deltaText = if (delta != null) {
        if (delta >= 0f) "+${formatKg(delta)} kg" else "${formatKg(delta)} kg"
    } else "—"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
            .padding(spacing.sm)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            // Header Row: Exercise Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = series.exerciseType.displayName().uppercase(),
                    color = ImperialGold,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                val badgeText = if (delta != null && delta > 0.01f) {
                    "STRENGTH SURGE ($deltaText)"
                } else if (delta != null && delta < -0.01f) {
                    "RECOVERY ($deltaText)"
                } else {
                    "CONSISTENT"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(deltaColor.copy(alpha = 0.16f))
                        .border(1.dp, deltaColor.copy(alpha = 0.5f), RoundedCornerShape(spacing.cornerXs))
                        .padding(horizontal = spacing.scale(6), vertical = spacing.scale(2)),
                ) {
                    Text(
                        text = badgeText,
                        color = deltaColor,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            // Subtitle
            Text(
                text = stringResource(R.string.progress_hero_subtitle),
                color = SilverSlate,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )

            // 4 Grid Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
            ) {
                ProgressMetricBox(
                    label = "WORKING WT",
                    value = "${formatKg(series.latestWeightKg ?: 0f)} kg",
                    valueColor = SilverSteel,
                    modifier = Modifier.weight(1f)
                )
                ProgressMetricBox(
                    label = "NET DELTA",
                    value = deltaText,
                    valueColor = deltaColor,
                    modifier = Modifier.weight(1f)
                )
                ProgressMetricBox(
                    label = "EST. 1RM",
                    value = "${formatKg(series.bestEst1RmKg)} kg",
                    valueColor = ImperialGold,
                    modifier = Modifier.weight(1f)
                )
                ProgressMetricBox(
                    label = "TOTAL VOL",
                    value = "${formatKg(series.totalVolumeKg)} kg",
                    valueColor = TorchAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProgressMetricBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(SlateGroove)
            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(spacing.cornerXs))
            .padding(horizontal = spacing.scale(6), vertical = spacing.scale(5))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = SilverSlate,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(spacing.xxxs))
            Text(
                text = value,
                color = valueColor,
                fontFamily = determination,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WeightChartCard(series: ExerciseProgressSeries) {
    val points = series.sessions.toChartPoints { it.workingWeightKg }
    val description = stringResource(
        R.string.progress_weight_chart_desc,
        series.exerciseType.displayName(),
        series.sessions.size,
    )
    ChartBoard(
        title = stringResource(R.string.progress_weight_chart_title),
        subtitle = "${series.sessions.size} sessions tracked · Latest: ${formatKg(series.latestWeightKg ?: 0f)} kg",
        description = description,
        height = 230.dp,
    ) {
        if (points.isEmpty() || points.all { it.value <= 0f }) {
            EmptyChartLabel(stringResource(R.string.progress_empty_chart))
        } else {
            ProgressLineChart(
                points = points,
                lineColor = TorchAmber,
                yUnit = stringResource(R.string.kg_unit),
            )
        }
    }
}

@Composable
private fun VolumeChartCard(series: ExerciseProgressSeries) {
    val points = series.sessions.toChartPoints { it.volumeKg }
    ChartBoard(
        title = stringResource(R.string.progress_volume_chart_title),
        subtitle = "Session tonnage (weight × reps) · Peak: ${formatKg(series.maxVolumeKg)} kg",
        description = stringResource(R.string.progress_volume_chart_desc),
        height = 210.dp,
    ) {
        if (points.isEmpty() || points.all { it.value <= 0f }) {
            EmptyChartLabel(stringResource(R.string.progress_empty_volume))
        } else {
            ProgressLineChart(
                points = points,
                lineColor = ImperialGold,
                yUnit = stringResource(R.string.kg_unit),
            )
        }
    }
}

@Composable
private fun Estimated1RmCard(series: ExerciseProgressSeries) {
    val spacing = LocalSpacing.current
    val firstEst1Rm = series.firstEst1RmKg ?: 0f
    val latestEst1Rm = series.latestEst1RmKg ?: 0f
    val bestEst1Rm = series.bestEst1RmKg
    val delta1Rm = if (firstEst1Rm > 0f) latestEst1Rm - firstEst1Rm else 0f
    val pctGain = if (firstEst1Rm > 0f) ((delta1Rm / firstEst1Rm) * 100).toInt() else 0

    ChartBoard(
        title = stringResource(R.string.progress_1rm_title),
        subtitle = stringResource(R.string.progress_1rm_desc),
        description = stringResource(R.string.progress_1rm_desc),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                ProgressMetricBox(
                    label = "INITIAL 1RM",
                    value = "${formatKg(firstEst1Rm)} kg",
                    valueColor = SilverSlate,
                    modifier = Modifier.weight(1f)
                )
                ProgressMetricBox(
                    label = "CURRENT 1RM",
                    value = "${formatKg(latestEst1Rm)} kg",
                    valueColor = SilverSteel,
                    modifier = Modifier.weight(1f)
                )
                ProgressMetricBox(
                    label = "PEAK 1RM",
                    value = "${formatKg(bestEst1Rm)} kg",
                    valueColor = ImperialGold,
                    modifier = Modifier.weight(1f)
                )
            }

            if (delta1Rm != 0f) {
                val isPositive = delta1Rm >= 0f
                val deltaColor = if (isPositive) VitalGreen else BronzeCopper
                val text = if (isPositive) "+${formatKg(delta1Rm)} kg (+${pctGain}%)" else "${formatKg(delta1Rm)} kg (${pctGain}%)"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1RM Strength Progression:",
                        color = SilverSlate,
                        fontSize = 11.sp
                    )
                    Text(
                        text = text,
                        color = deltaColor,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = stringResource(R.string.progress_1rm_formula_hint),
                color = SilverSlate.copy(alpha = 0.8f),
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun MovementQualityCard(series: ExerciseProgressSeries) {
    val spacing = LocalSpacing.current
    val hasRom = series.avgRomScore > 0f
    val hasStability = series.avgStabilityScore > 0f

    ChartBoard(
        title = stringResource(R.string.progress_form_quality_title),
        subtitle = "Sensor ROM & bar balance telemetry",
        description = "Movement quality score",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                ProgressMetricBox(
                    label = "FULL ROM SCORE",
                    value = if (hasRom) "${(series.avgRomScore * 100).toInt()}%" else "—",
                    valueColor = if (hasRom) VitalGreen else SilverSlate,
                    modifier = Modifier.weight(1f)
                )
                ProgressMetricBox(
                    label = "BAR STABILITY",
                    value = if (hasStability) "${(series.avgStabilityScore * 100).toInt()}%" else "—",
                    valueColor = if (hasStability) CrystalCyan else SilverSlate,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = stringResource(R.string.progress_form_sensor_hint),
                color = SilverSlate.copy(alpha = 0.8f),
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun RecentSessionsCard(series: ExerciseProgressSeries) {
    val spacing = LocalSpacing.current
    val recent = series.sessions.takeLast(5).reversed()

    ChartBoard(
        title = stringResource(R.string.progress_recent_sessions_title),
        subtitle = "Last ${recent.size} sessions recorded for this lift",
        description = "Recent sessions log",
    ) {
        if (recent.isEmpty()) {
            EmptyChartLabel(stringResource(R.string.progress_no_sessions_yet))
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.xxs)
            ) {
                recent.forEach { session ->
                    val instant = Instant.ofEpochMilli(session.timestampMillis)
                    val dateStr = chartDateFormatter.format(instant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(SlateGroove)
                            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(spacing.cornerXs))
                            .padding(horizontal = spacing.sm, vertical = spacing.scale(6)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateStr,
                            color = SilverSlate,
                            fontSize = 11.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${session.setCount} sets",
                                color = SilverSlate,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${formatKg(session.workingWeightKg)} kg",
                                color = SilverSteel,
                                fontFamily = determination,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${formatKg(session.volumeKg)} kg vol",
                                color = ImperialGold,
                                fontFamily = determination,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartBoard(
    title: String,
    subtitle: String? = null,
    description: String,
    height: Dp? = null,
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current
    val boxModifier = Modifier
        .fillMaxWidth()
        .then(if (height != null) Modifier.height(height) else Modifier)
        .clip(RoundedCornerShape(spacing.cornerSm))
        .background(SlateDeep.copy(alpha = 0.94f))
        .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
        .padding(spacing.sm)
        .semantics { contentDescription = description }

    Box(modifier = boxModifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = ImperialGold,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = SilverSlate,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(spacing.xs))
            Box(
                modifier = Modifier
                    .weight(1f, fill = height != null)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateGroove)
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(spacing.cornerXs))
                    .padding(spacing.xs)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun EmptyChartLabel(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = SilverSlate.copy(alpha = 0.8f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

private val chartDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM").withZone(ZoneId.systemDefault())

private fun List<SessionProgressPoint>.toChartPoints(
    value: (SessionProgressPoint) -> Float,
): List<ChartPoint> = map { session ->
    val instant = Instant.ofEpochMilli(session.timestampMillis)
    ChartPoint(
        xLabel = chartDateFormatter.format(instant),
        value = value(session),
        markerLabel = "${formatKg(value(session))} kg · ${chartDateFormatter.format(instant)}",
    )
}
