package com.pixelfitquest.model.service

import com.pixelfitquest.model.User
import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUser: Flow<User?>
    val currentUserId: String
    fun hasUser(): Boolean
    fun getUserProfile(): User
    suspend fun updateDisplayName(newDisplayName: String)
    suspend fun linkAccountWithGoogle(idToken: String)
    suspend fun linkAccountWithEmail(email: String, password: String)
    suspend fun signInWithGoogle(idToken: String)

    suspend fun createAccountWithEmail(email: String, password: String)
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signOut()
    suspend fun deleteAccount()
}