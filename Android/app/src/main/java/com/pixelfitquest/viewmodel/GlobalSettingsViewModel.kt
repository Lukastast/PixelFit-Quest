package com.pixelfitquest.viewmodel

import androidx.lifecycle.ViewModel
import com.pixelfitquest.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GlobalSettingsViewModel @Inject constructor(
    val userRepository: UserRepository
) : ViewModel()