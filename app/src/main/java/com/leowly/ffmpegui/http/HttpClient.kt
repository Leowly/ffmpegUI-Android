package com.leowly.ffmpegui.http

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException

object HttpClient {
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    // Separate client for authenticated requests
    private fun getAuthenticatedClient(token: String) = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(token, "")
                }
            }
        }
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpRedirect)
    }

    private fun resolveBaseUrl(serverAddress: String): String {
        val trimmedAddress = serverAddress.trim().removeSuffix("/")
        return if (trimmedAddress.startsWith("http://") || trimmedAddress.startsWith("https://")) {
            trimmedAddress
        } else {
            // Default to http for local development as per documentation recommendation
            "http://$trimmedAddress"
        }
    }

    // --- File Management ---

    suspend fun getFiles(serverAddress: String, token: String): Result<List<FileItem>> {
        return try {
            val baseUrl = resolveBaseUrl(serverAddress)
            val response: List<FileItem> = getAuthenticatedClient(token).get("$baseUrl${ApiRoutes.LIST_FILES}").body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(serverAddress: String, token: String, fileName: String, fileBytes: ByteArray): Result<FileItem> {
        return try {
            val baseUrl = resolveBaseUrl(serverAddress)
            val response: FileItem = getAuthenticatedClient(token).post("$baseUrl${ApiRoutes.UPLOAD_FILE}") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("file", fileBytes, Headers.build {
                            append(HttpHeaders.ContentType, "application/octet-stream")
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        })
                    }
                ))
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(serverAddress: String, token: String, fileId: String): Result<DeleteResponse> {
        return try {
            val baseUrl = resolveBaseUrl(serverAddress)
            val response: DeleteResponse = getAuthenticatedClient(token).delete("$baseUrl${ApiRoutes.DELETE_FILE}") {
                url {
                    parameters.append("filename", fileId)
                }
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // --- Auth ---
    suspend fun register(serverAddress: String, userCreateRequest: UserCreateRequest): Result<User> {
        if (serverAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("Server address cannot be empty."))
        }

        val trimmedAddress = serverAddress.trim().removeSuffix("/")

        val urlsToTry = if (trimmedAddress.startsWith("http://") || trimmedAddress.startsWith("https://")) {
            listOf(trimmedAddress)
        } else {
            listOf("https://$trimmedAddress", "http://$trimmedAddress")
        }

        var lastException: Exception? = null

        for (baseUrl in urlsToTry) {
            val finalUrl = "$baseUrl${ApiRoutes.USERS}"
            try {
                val response: HttpResponse = client.post(finalUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(userCreateRequest)
                }

                return when (response.status) {
                    HttpStatusCode.OK -> {
                        Result.success(response.body<User>())
                    }
                    HttpStatusCode.BadRequest -> {
                        val errorDetail = response.body<RegistrationErrorDetail>()
                        Result.failure(Exception(errorDetail.detail))
                    }
                    else -> {
                        Result.failure(Exception("Registration failed with status: ${response.status.value}"))
                    }
                }

            } catch (networkError: IOException) {
                lastException = if (networkError.message?.contains("getaddrinfo failed") == true || networkError.message?.contains("EAI_NODATA") == true) {
                    Exception("The server address could not be found. Please ensure it is correct.", networkError)
                } else {
                    Exception("A network error occurred while trying to connect to $finalUrl. Please check your connection.", networkError)
                }
                // Continue to the next URL
            } catch (e: Exception) {
                lastException = Exception("An unexpected error occurred: ${e.message}", e)
                break // Break on other errors
            }
        }
        return Result.failure(lastException ?: Exception("Unknown error occurred during registration."))
    }


    suspend fun login(serverAddress: String, tokenRequest: TokenRequest): Result<APIResponse<Token>> {
        if (serverAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("Server address cannot be empty."))
        }

        val trimmedAddress = serverAddress.trim().removeSuffix("/")

        // Create a list of URLs to try
        val urlsToTry = if (trimmedAddress.startsWith("http://") || trimmedAddress.startsWith("https://")) {
            listOf(trimmedAddress)
        } else {
            listOf("https://$trimmedAddress", "http://$trimmedAddress")
        }

        var lastException: Exception? = null

        for (baseUrl in urlsToTry) {
            val finalUrl = "$baseUrl${ApiRoutes.TOKEN}"
            try {
                // Attempt the request
                val response: HttpResponse = client.submitForm(
                    url = finalUrl,
                    formParameters = Parameters.build {
                        append("username", tokenRequest.username)
                        append("password", tokenRequest.password)
                    }
                )

                val responseBody = response.bodyAsText()

                // If we get a response, try to parse it.
                return try {
                    val apiResponse = json.decodeFromString<APIResponse<Token>>(responseBody)
                    if (apiResponse.success) {
                        Result.success(apiResponse)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } catch (_: Exception) {
                    // This is a successful connection but bad JSON, return the detailed error
                    Result.failure(
                        Exception(
                            "Failed to parse server response. Status: ${response.status.value}. URL: $finalUrl. Body: $responseBody"
                        )
                    )
                }

            } catch (networkError: IOException) { // Catch specific network errors to allow fallback
                lastException = if (networkError.message?.contains("getaddrinfo failed") == true || networkError.message?.contains("EAI_NODATA") == true) {
                    Exception("The server address could not be found. Please ensure it is correct.", networkError)
                } else {
                    Exception("A network error occurred while trying to connect to $finalUrl. Please check your connection.", networkError)
                }
                // Continue to the next URL in the list (http)
            } catch (e: Exception) { // Catch any other unexpected errors during the request
                lastException = Exception("An unexpected error occurred: ${e.message}", e)
                // For other errors, don't try the next URL, just fail.
                break
            }
        }

        // If the loop completes, all URLs failed. Return the last error.
        return Result.failure(lastException ?: Exception("Unknown error occurred after trying all connection protocols."))
    }
}
