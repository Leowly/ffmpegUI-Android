package com.leowly.ffmpegui.http

/**
 * A centralized object to hold all API endpoint paths for the application.
 */
object ApiRoutes {
    // BASE URL is handled in the HttpClient, these are the paths.

    // 1. User Management
    const val TOKEN = "/token"
    const val USERS = "/users/"
    const val USERS_ME = "/users/me"

    // 2. File Management (prefixed with /api)
    private const val API_PREFIX = "/api"
    const val FILES_UPLOAD = "$API_PREFIX/files/upload"
    const val FILES_LIST = "$API_PREFIX/files"
    const val FILES_DOWNLOAD = "$API_PREFIX/files/download-file" // Append /{file_id}
    const val FILES_INFO = "$API_PREFIX/files/file-info"
    const val FILES_DELETE = "$API_PREFIX/files/delete-file"
    const val FILES_PROCESS = "$API_PREFIX/files/process"

    // 3. Task Management (prefixed with /api)
    const val TASKS_LIST = "$API_PREFIX/tasks"
    const val TASKS_STATUS = "$API_PREFIX/tasks/task-status" // Append /{taskId}
    const val TASKS_DELETE = "$API_PREFIX/tasks" // Append /{task_id}

    // 4. Real-time Communication
    const val WS_PROGRESS = "/ws/progress" // Append /{task_id}
}
