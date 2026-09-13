package com.pixelfitquest.local.export

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.helpers.ERROR_TAG
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocalExportUiState(
    val workoutCount: Int = 0,
    val setCount: Int = 0,
    val inProgress: Boolean = false,
    val message: String? = null,
)

sealed class LocalExportEvent {
    data class Save(val files: List<PreparedExport>) : LocalExportEvent()
    data class Shared(val fileCount: Int) : LocalExportEvent()
}

@HiltViewModel
class LocalExportViewModel @Inject constructor(
    private val exportService: LocalExportService,
    @ApplicationContext private val appContext: Context,
) : PixelFitViewModel() {

    private val _state = MutableStateFlow(LocalExportUiState())
    val state: StateFlow<LocalExportUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LocalExportEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<LocalExportEvent> = _events.asSharedFlow()

    init {
        refreshCounts()
    }

    fun refreshCounts() {
        viewModelScope.launch {
            try {
                val counts = exportService.counts()
                _state.value = _state.value.copy(
                    workoutCount = counts.workoutCount,
                    setCount = counts.setCount,
                )
            } catch (e: Exception) {
                Log.e(ERROR_TAG, "Export count refresh failed", e)
            }
        }
    }

    fun share(format: ExportFormat) = prepareAndRun(format, save = false)

    fun save(format: ExportFormat) = prepareAndRun(format, save = true)

    private fun prepareAndRun(format: ExportFormat, save: Boolean) {
        if (_state.value.inProgress) return
        viewModelScope.launch {
            _state.value = _state.value.copy(inProgress = true, message = null)
            try {
                val counts = exportService.counts()
                _state.value = _state.value.copy(
                    workoutCount = counts.workoutCount,
                    setCount = counts.setCount,
                )
                val prepared = exportService.prepareExports(format)
                if (prepared.isEmpty()) {
                    _state.value = _state.value.copy(message = "Nothing to export")
                    return@launch
                }
                if (save) {
                    _events.emit(LocalExportEvent.Save(prepared))
                } else {
                    exportService.sharePrepared(appContext, prepared)
                    _events.emit(LocalExportEvent.Shared(prepared.size))
                }
            } catch (e: Exception) {
                Log.e(ERROR_TAG, "Export failed", e)
                _state.value = _state.value.copy(
                    message = e.message ?: "Export failed",
                )
            } finally {
                _state.value = _state.value.copy(inProgress = false)
            }
        }
    }
}
