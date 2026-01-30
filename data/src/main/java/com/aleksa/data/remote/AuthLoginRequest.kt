package com.aleksa.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AuthLoginRequest(
    val username: String,
    val password: String,
)
