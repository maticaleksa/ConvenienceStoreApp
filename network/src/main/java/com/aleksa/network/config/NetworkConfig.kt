package com.aleksa.network.config

data class NetworkConfig(
    val baseUrl: String,
    val isDebug: Boolean = false,
    val timeoutMillis: Long = 30_000L,
)
