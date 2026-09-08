package com.pixelfitquest.feature.progress

import com.pixelfitquest.feature.progress.data.ProgressRepository
import com.pixelfitquest.feature.progress.model.ExerciseProgressSeries
import com.pixelfitquest.feature.progress.model.ProgressAggregator
import com.pixelfitquest.feature.progress.model.ProgressDataSource
import com.pixelfitquest.feature.progress.model.ProgressOverview
import com.pixelfitquest.feature.progress.model.SampleProgressData
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ProgressUiState(
    val isLoading: Boolean = true,
    val overview: ProgressOverview = ProgressOverview(emptyList(), ProgressDataSource.SAMPLE),
    val selectedExercise: ExerciseType? = null,
) {
    val selectedSeries: ExerciseProgressSeries?
        get() = overview.series.find { it.exerciseType == selectedExercise }
            ?: overview.series.firstOrNull()
}

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
) : PixelFitViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchCatching(
            onError = {
                val sample = ProgressOverview(
                    series = ProgressAggregator.aggregate(SampleProgressData.records()),
                    source = ProgressDataSource.SAMPLE,
                )
                _uiState.value = ProgressUiState(
                    isLoading = false,
                    overview = sample,
                    selectedExercise = ExerciseType.BENCH_PRESS,
                )
            },
        ) {
            _uiState.update { it.copy(isLoading = true) }
            val overview = progressRepository.loadOverview()
            val preferred = _uiState.value.selectedExercise
            val selected = when {
                preferred != null && overview.series.any { it.exerciseType == preferred } -> preferred
                overview.series.any { it.exerciseType == ExerciseType.BENCH_PRESS } -> ExerciseType.BENCH_PRESS
                else -> overview.series.firstOrNull()?.exerciseType
            }
            _uiState.value = ProgressUiState(
                isLoading = false,
                overview = overview,
                selectedExercise = selected,
            )
        }
    }

    fun selectExercise(type: ExerciseType) {
        _uiState.update { it.copy(selectedExercise = type) }
    }
}
