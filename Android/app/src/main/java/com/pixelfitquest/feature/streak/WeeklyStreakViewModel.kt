package com.pixelfitquest.feature.streak

import com.pixelfitquest.feature.streak.data.WeeklyStreakRepository
import com.pixelfitquest.feature.streak.model.WeeklyStreakSnapshot
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WeeklyStreakViewModel @Inject constructor(
    private val repository: WeeklyStreakRepository,
) : PixelFitViewModel() {

    private val _snapshot = MutableStateFlow(WeeklyStreakSnapshot.Empty)
    val snapshot: StateFlow<WeeklyStreakSnapshot> = _snapshot.asStateFlow()

    init {
        launchCatching {
            repository.reconcile()
            repository.observeSnapshot().collect { _snapshot.value = it }
        }
    }

    fun setTargetSessionsPerWeek(target: Int) {
        launchCatching {
            _snapshot.value = repository.setTargetSessionsPerWeek(target)
        }
    }

    fun refresh() {
        launchCatching {
            _snapshot.value = repository.reconcile()
        }
    }
}
