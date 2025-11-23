---

# 🎥 FFmpeg UI 后端 API 文档

**基础信息**
*   **Base URL (开发环境):** `http://<你的电脑IP>:8000`
    *   *注意：如果是 Android 模拟器，请使用 `http://10.0.2.2:8000`*
    *   *注意：如果是真机调试，请确保手机和电脑在同一 WiFi，并使用电脑的局域网 IP (如 192.168.x.x)*
*   **认证方式:** Bearer Token (JWT)
    *   除了登录和注册接口，其余所有接口都需要在 Header 中携带：`Authorization: Bearer <access_token>`

---

## 1. 用户认证 (Auth)

### 1.1 用户注册
*   **接口:** `POST /users/`
*   **描述:** 创建新用户。
*   **Body (JSON):**
    ```json
    {
      "username": "android_user",
      "password": "Password123" // 必须包含大小写字母和数字，至少8位
    }
    ```
*   **响应:**
    ```json
    {
      "username": "android_user",
      "id": 1,
      "files": [],
      "tasks": []
    }
    ```

### 1.2 用户登录 (获取 Token)
*   **接口:** `POST /token`
*   **描述:** 登录并获取访问令牌。
*   **Content-Type:** `application/x-www-form-urlencoded` (注意：不是 JSON)
*   **Body (Form Data):**
    *   `username`: "android_user"
    *   `password`: "Password123"
*   **响应:**
    ```json
    {
      "success": true,
      "data": {
        "access_token": "eyJhbGciOiJIUzI1NiIsInR...",
        "token_type": "bearer"
      },
      "message": "登录成功！"
    }
    ```

### 1.3 获取当前用户信息
*   **接口:** `GET /users/me`
*   **描述:** 验证 Token 有效性并获取用户详情。
*   **响应:** 同注册接口响应。

---

## 2. 文件管理 (Files)

所有接口前缀：`/api`

### 2.1 上传文件
*   **接口:** `POST /api/upload`
*   **Content-Type:** `multipart/form-data`
*   **Body:**
    *   `file`: [二进制文件数据]
*   **响应:**
    ```json
    {
      "uid": "1",
      "id": "1",
      "name": "video.mp4",
      "status": "done",
      "size": 10485760,
      "response": {
        "file_id": "1",
        "original_name": "video.mp4",
        "temp_path": "..."
      }
    }
    ```

### 2.2 获取文件列表
*   **接口:** `GET /api/files`
*   **响应:** 返回文件对象数组（结构同上传响应）。

### 2.3 获取文件详细信息 (FFprobe)
*   **接口:** `GET /api/file-info`
*   **参数:** `?filename=<file_id>` (注意：虽然参数名叫 filename，但实际传的是文件 ID)
*   **响应:**
    ```json
    {
      "streams": [
        {
          "codec_type": "video",
          "codec_name": "h264",
          "width": 1920,
          "height": 1080,
          ...
        },
        {
          "codec_type": "audio",
          "codec_name": "aac",
          ...
        }
      ],
      "format": {
        "format_name": "mov,mp4,m4a,3gp,3g2,mj2",
        "duration": "120.5",
        "size": "...",
        "bit_rate": "..."
      }
    }
    ```

### 2.4 下载文件
*   **接口:** `GET /api/download-file/{file_id}`
*   **描述:** 下载原始文件或处理后的文件。
*   **响应:** 二进制流 (Blob)。Header 中包含 `Content-Disposition`。

### 2.5 删除文件
*   **接口:** `DELETE /api/delete-file`
*   **参数:** `?filename=<file_id>`
*   **响应:** `{"message": "File ... deleted."}`

---

## 3. 视频处理与任务 (Processing & Tasks)

所有接口前缀：`/api`

### 3.1 获取系统能力 (硬件加速检测)
*   **接口:** `GET /api/capabilities`
*   **描述:** 用于判断是否显示“硬件加速”开关。
*   **响应:**
    ```json
    {
      "has_hardware_acceleration": true,
      "hardware_type": "nvidia" // 可能值: 'nvidia', 'intel', 'amd', 'mac', null
    }
    ```

### 3.2 创建处理任务 (核心接口)
*   **接口:** `POST /api/process`
*   **Body (JSON):**
    ```json
    {
      "files": ["1"], // 文件ID列表
      "container": "mp4", // 目标容器格式
      "startTime": 0, // 开始时间(秒)
      "endTime": 60, // 结束时间(秒)
      "totalDuration": 100, // 总时长(用于校验)
      "videoCodec": "libx264", // 或 'copy', 'h264_nvenc' 等
      "audioCodec": "aac", // 或 'copy'
      "videoBitrate": 2000, // 可选 (kbps)
      "audioBitrate": 192, // 可选 (kbps)
      "resolution": { // 可选
        "width": 1280,
        "height": 720,
        "keepAspectRatio": true
      },
      "useHardwareAcceleration": true, // 新增：是否启用硬件加速
      "preset": "balanced" // 新增：'fast', 'balanced', 'quality'
    }
    ```
*   **响应:** 返回创建的任务对象列表。

### 3.3 获取任务列表
*   **接口:** `GET /api/tasks`
*   **响应:**
    ```json
    [
      {
        "id": 10,
        "status": "processing", // pending, processing, completed, failed
        "progress": 45,
        "source_filename": "video.mp4",
        "output_path": "...",
        "result_file_id": null,
        ...
      }
    ]
    ```

### 3.4 获取单个任务状态
*   **接口:** `GET /api/task-status/{taskId}`
*   **响应:** 同单个任务对象。

### 3.5 删除任务 (取消任务)
*   **接口:** `DELETE /api/tasks/{task_id}`
*   **描述:** 从数据库删除任务记录。如果任务正在运行，**会终止后台 FFmpeg 进程**。
*   **响应:** HTTP 204 No Content (无 Body)。

---

## 4. 实时进度 (WebSocket)

用于在 Android 界面上实时显示进度条。

*   **URL:** `ws://<你的IP>:8000/ws/progress/{task_id}`
*   **交互流程:**
    1.  建立连接。
    2.  服务器推送 JSON 消息。
    3.  任务结束或失败时，服务器断开连接。
*   **接收消息格式:**
    ```json
    {
      "progress": 50, // 0-100 的整数
      "status": "processing" // 可选字段，任务状态
    }
    ```

---

## Android 开发建议 (Retrofit 定义示例)

如果你使用 Retrofit，接口定义大致如下：

```kotlin
interface ApiService {
    // Auth
    @FormUrlEncoded
    @POST("token")
    suspend fun login(@Field("username") user: String, @Field("password") pass: String): Response<AuthResponse>

    // Files
    @Multipart
    @POST("api/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<FileResponse>

    @GET("api/files")
    suspend fun getFiles(): Response<List<FileResponse>>

    // Processing
    @POST("api/process")
    suspend fun createProcess(@Body payload: ProcessPayload): Response<List<Task>>
    
    @GET("api/capabilities")
    suspend fun getCapabilities(): Response<SystemCapabilities>
}
```