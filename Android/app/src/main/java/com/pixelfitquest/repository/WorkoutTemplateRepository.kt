package com.pixelfitquest.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pixelfitquest.model.WorkoutTemplate
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@ViewModelScoped
class WorkoutTemplateRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val usersCollection = firestore.collection("users")
    private val templatesSubcollection get() = usersCollection.document(currentUserId()).collection("templates")

    private fun currentUserId(): String = auth.currentUser?.uid ?: throw IllegalStateException("No user logged in")

    // Save a template (creates or overwrites by ID)
    suspend fun saveTemplate(template: WorkoutTemplate) {
        val uid = currentUserId()  // Throws if no user
        Log.d("TemplateRepo", "Saving for UID: $uid, ID: ${template.id}")
        try {
            templatesSubcollection.document(template.id).set(template.toMap()).await()
        } catch (e: Exception) {
            throw e  // Let ViewModel handle (e.g., show error)
        }
    }

    fun getTemplates(): Flow<List<WorkoutTemplate>> = callbackFlow {
        val listener = templatesSubcollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                } else {
                    val templates = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: emptyMap()
                            WorkoutTemplate.fromMap(data).copy(id = doc.id)
                        } catch (ex: Exception) {
                            null
                        }
                    } ?: emptyList()
                    trySend(templates)
                }
            }
        awaitClose { listener.remove() }
    }

    // One-time fetch (e.g., for offline or specific queries)
    suspend fun fetchTemplatesOnce(limit: Int = 50): List<WorkoutTemplate> {
        return try {
            val snapshot = templatesSubcollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: emptyMap()
                    WorkoutTemplate.fromMap(data).copy(id = doc.id)
                } catch (ex: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Delete a template by ID
    suspend fun deleteTemplate(templateId: String) {
        try {
            templatesSubcollection.document(templateId).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Optional: Fetch by name (for search/edit)
    suspend fun fetchTemplateByName(name: String): WorkoutTemplate? {
        return try {
            val snapshot = templatesSubcollection
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.let { doc ->
                try {
                    val data = doc.data ?: emptyMap()
                    WorkoutTemplate.fromMap(data).copy(id = doc.id)
                } catch (ex: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}