package com.pixelfitquest.feature.health

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.health.HealthMetrics
import com.pixelfitquest.health.HealthPermissions
import com.pixelfitquest.health.HealthRepository
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthCenterViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
) : PixelFitViewModel() {

    private val _healthStatus = MutableStateFlow(HealthConnectStatus.UNAVAILABLE)
    val healthStatus: StateFlow<HealthConnectStatus> = _healthStatus.asStateFlow()

    private val _healthMetrics = MutableStateFlow(HealthMetrics.EMPTY)
    val healthMetrics: StateFlow<HealthMetrics> = _healthMetrics.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    val healthPermissions: Set<String> = HealthPermissions.required()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _healthStatus.value = healthRepository.availability()
                val granted = healthRepository.grantedPermissions()
                _permissionsGranted.value = HealthPermissions.hasStepsRead(granted)
                if (_healthStatus.value == HealthConnectStatus.AVAILABLE) {
                    _healthMetrics.value = healthRepository.readTodayMetrics()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        _permissionsGranted.value = HealthPermissions.hasStepsRead(granted)
        refresh()
    }
}
