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

// Request for user creation
@Serializable
data class UserCreateRequest(
    val username: String,
    val password: String
)

// Response for successful user creation
@Serializable
data class User(
    val id: Int,
    val username: String
)

// Specific error response for user registration
@Serializable
data class RegistrationErrorDetail(
    val detail: String
)

// --- File Management DTOs ---

@Serializable
data class FileItem(
    val id: String,
    val name: String,
    val size: Long,
    val status: String,
    val uid: String,
    val response: UploadResponseData? = null
)

@Serializable
data class UploadResponseData(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("original_name")
    val originalName: String,
    @SerialName("temp_path")
    val tempPath: String
)

@Serializable
data class DeleteResponse(
    val message: String
)
