package com.pixelfitquest.firebase.repository

import android.util.Log
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import com.pixelfitquest.local.LocalPixelFitStore
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class WorkoutTemplateRepository @Inject constructor(
    private val localStore: LocalPixelFitStore,
) {
    suspend fun saveTemplate(template: WorkoutTemplate) {
        Log.d("TemplateRepo", "Saving local template ID: ${template.id}")
        localStore.saveTemplate(template)
    }

    fun getTemplates(): Flow<List<WorkoutTemplate>> = localStore.observeTemplates()

    suspend fun fetchTemplatesOnce(limit: Int = 50): List<WorkoutTemplate> {
        return try {
            localStore.getTemplates(limit)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun deleteTemplate(templateId: String) {
        localStore.deleteTemplate(templateId)
    }

    suspend fun fetchTemplateByName(name: String): WorkoutTemplate? {
        return try {
            localStore.getTemplateByName(name)
        } catch (_: Exception) {
            null
        }
    }
}
