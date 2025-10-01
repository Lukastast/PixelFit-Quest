package com.pixelfitquest.model

data class User(

    val id: String = "",
    val email: String = "",
    val provider: String = "",
    val displayName: String = "",
    val profilePictureUrl: String? = null,
    val isLinkedWithGoogle: Boolean = false

)
