package com.aleksa.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AuthLoginResponse(
    val authenticated: Boolean,
)
