package com.pixelfitquest.feature.progress

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.pixelfitquest.ui.theme.FireOrange
import com.pixelfitquest.ui.theme.QuestBlue
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.typography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.loading), color = Color.White, fontSize = 16.sp)
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.info_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back_desc),
                        tint = Color.White,
                    )
                }
                Text(
                    text = stringResource(R.string.progress_title),
                    style = typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = sourceLabel,
            color = if (uiState.overview.isSample) RewardGold else Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState.overview.isSample) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.progress_sample_banner),
                color = RewardGold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.overview.series.isEmpty()) {
            Text(
                text = stringResource(R.string.progress_no_exercises),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp),
            )
        } else {
            ExerciseChipRow(
                series = uiState.overview.series,
                selected = uiState.selectedExercise,
                onSelect = viewModel::selectExercise,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (series != null) {
                StatsRow(series = series)
                Spacer(modifier = Modifier.height(12.dp))
                WeightChartCard(series = series)
                Spacer(modifier = Modifier.height(12.dp))
                VolumeChartCard(series = series)
                Spacer(modifier = Modifier.height(12.dp))
                QualityStubCard(
                    title = stringResource(R.string.progress_rom_chart_title),
                    accent = VitalGreen,
                )
                Spacer(modifier = Modifier.height(12.dp))
                QualityStubCard(
                    title = stringResource(R.string.progress_stability_chart_title),
                    accent = QuestBlue,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ExerciseChipRow(
    series: List<ExerciseProgressSeries>,
    selected: ExerciseType?,
    onSelect: (ExerciseType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        series.forEach { item ->
            val isSelected = item.exerciseType == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) FireOrange else Color.White.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) RewardGold else Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable { onSelect(item.exerciseType) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = item.exerciseType.displayName(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun StatsRow(series: ExerciseProgressSeries) {
    val latest = series.latestWeightKg
    val delta = series.weightDeltaKg
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatChip(
            label = stringResource(R.string.progress_stat_latest, latest?.let { formatKg(it) } ?: "—"),
        )
        if (delta != null) {
            val deltaText = if (delta >= 0f) "+${formatKg(delta)}" else formatKg(delta)
            StatChip(label = stringResource(R.string.progress_stat_delta, deltaText))
        }
        StatChip(
            label = stringResource(R.string.progress_stat_sessions, series.sessions.size),
        )
    }
}

@Composable
private fun StatChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(text = label, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
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
        description = description,
        height = 220.dp,
    ) {
        if (points.isEmpty() || points.all { it.value <= 0f }) {
            EmptyChartLabel(stringResource(R.string.progress_empty_chart))
        } else {
            ProgressLineChart(
                points = points,
                lineColor = FireOrange,
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
        description = stringResource(R.string.progress_volume_chart_desc),
        height = 200.dp,
    ) {
        if (points.isEmpty() || points.all { it.value <= 0f }) {
            EmptyChartLabel(stringResource(R.string.progress_empty_volume))
        } else {
            ProgressLineChart(
                points = points,
                lineColor = RewardGold,
                yUnit = stringResource(R.string.kg_unit),
            )
        }
    }
}

@Composable
private fun QualityStubCard(
    title: String,
    accent: Color,
) {
    ChartBoard(
        title = title,
        description = stringResource(R.string.progress_quality_stub),
        height = 140.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ProgressLineChart(
                points = stubPreviewPoints(),
                lineColor = accent,
                stub = true,
            )
            Text(
                text = stringResource(R.string.progress_quality_stub),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun ChartBoard(
    title: String,
    description: String,
    height: Dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .semantics { contentDescription = description },
    ) {
        Image(
            painter = painterResource(id = R.drawable.questloginboard_wider),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun EmptyChartLabel(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
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
        markerLabel = "${formatKg(value(session))} · ${chartDateFormatter.format(instant)}",
    )
}

private fun stubPreviewPoints(): List<ChartPoint> = listOf(
    ChartPoint(" ", 62f),
    ChartPoint(" ", 68f),
    ChartPoint(" ", 64f),
    ChartPoint(" ", 72f),
    ChartPoint(" ", 70f),
    ChartPoint(" ", 76f),
)
