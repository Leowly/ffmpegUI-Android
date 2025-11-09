package com.leowly.ffmpegui.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Generic API response wrapper to match the backend
@Serializable
data class APIResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

// For the login form, not serialized directly
data class TokenRequest(
    val username: String,
    val password: String
)

// Represents the "data" object in a successful token response
@Serializable
data class Token(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String
)
