package com.leowly.ffmpegui.http

/**
 * A centralized object to hold all API endpoint paths for the application.
 * Refactored based on openapi.json.
 */
object ApiRoutes {
    // BASE URL is handled in the HttpClient, these are the paths.

    // 1. User Management
    const val TOKEN = "/token"
    const val USERS = "/users/"
    const val USERS_ME = "/users/me"

    // 2. API endpoints (prefixed with /api)
    private const val API_PREFIX = "/api"

    // System Capabilities
    const val CAPABILITIES = "$API_PREFIX/capabilities"

    // File Management
    const val UPLOAD_FILE = "$API_PREFIX/upload"
    const val LIST_FILES = "$API_PREFIX/files"
    const val DOWNLOAD_FILE = "$API_PREFIX/download-file" // Append /{file_id}
    const val FILE_INFO = "$API_PREFIX/file-info" // Append ?filename={filename}
    const val DELETE_FILE = "$API_PREFIX/delete-file" // Append ?filename={filename}
    const val PROCESS_FILES = "$API_PREFIX/process"

    // Task Management
    const val TASKS_LIST = "$API_PREFIX/tasks"
    const val TASKS_DELETE = "$API_PREFIX/tasks" // Append /{task_id}
    const val TASK_STATUS = "$API_PREFIX/task-status" // Append /{taskId}

    // 3. Root
    const val ROOT = "/"

    // 4. Real-time Communication (WebSocket)
    const val WS_PROGRESS = "/ws/progress" // Append /{task_id}, not part of OpenAPI spec
}
