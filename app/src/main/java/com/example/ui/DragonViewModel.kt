package com.example.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.SettingsEntity
import com.example.data.database.UserEntity
import com.example.data.database.VideoHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DragonViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.appDao()

    // Active User State
    val currentUser: StateFlow<UserEntity?> = dao.getPrimaryUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Settings State
    val settings: StateFlow<SettingsEntity?> = dao.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Video History State
    val videos: StateFlow<List<VideoHistoryEntity>> = dao.getAllVideos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI state for authentication flow
    val authState = MutableStateFlow<String?>(null) // null = idle, "LOADING", "SUCCESS", or Error msg

    // Active Generation Progress (for real-time dashboard tracking)
    val activeGeneration = MutableStateFlow<VideoHistoryEntity?>(null)

    // Last completed video generation (to show as latest output display)
    val recentlyCompletedVideo = MutableStateFlow<VideoHistoryEntity?>(null)

    init {
        // Initialize default app state if empty
        viewModelScope.launch(Dispatchers.IO) {
            // Setup default settings if not exists
            dao.getSettings().collect { currentSettings ->
                if (currentSettings == null) {
                    dao.insertSettings(SettingsEntity())
                }
            }
        }
    }

    // --- Authentication Actions ---
    fun registerOrLogin(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authState.value = "LOADING"
            delay(1200) // Beautiful simulated secure loading delay
            val sanitizedEmail = email.trim()
            if (sanitizedEmail.isBlank() || !sanitizedEmail.contains("@")) {
                authState.value = "Invalid email format."
                return@launch
            }

            val cleanEmail = sanitizedEmail.lowercase()
            val isApproved = cleanEmail == "khawajashahidabbas471@gmail.com" ||
                    cleanEmail.endsWith("@dragon.ai") ||
                    cleanEmail == "preapproved@gmail.com" ||
                    cleanEmail == "admin@dragon.ai" ||
                    cleanEmail == "creator@dragon.ai" ||
                    cleanEmail == "investor@dragon.ai"

            if (!isApproved) {
                authState.value = "Access Denied: This email is not on the private whitelist of Dragon AI."
                return@launch
            }

            val userId = sanitizedEmail.replace(".", "_")
            val user = UserEntity(
                id = userId,
                email = sanitizedEmail,
                displayName = sanitizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                avatar = "img_user_avatar",
                credits = 500, // Private premium tester credits pool
                joinedAt = System.currentTimeMillis(),
                isAdmin = cleanEmail.startsWith("admin") || cleanEmail == "khawajashahidabbas471@gmail.com"
            )
            
            // Clear prior user sessions and establish this one
            dao.clearUsers()
            dao.insertUser(user)
            authState.value = "SUCCESS"
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearUsers()
            authState.value = null
        }
    }

    fun updateUserAvatar(avatarName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            currentUser.value?.let { user ->
                dao.updateUser(user.copy(avatar = avatarName))
            }
        }
    }

    fun addUserCredits(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            currentUser.value?.let { user ->
                dao.updateUser(user.copy(credits = user.credits + amount))
            }
        }
    }

    // --- Settings Actions ---
    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settings.value ?: SettingsEntity()
            dao.insertSettings(current.copy(isDark = isDark))
        }
    }

    fun updateLanguage(langCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settings.value ?: SettingsEntity()
            dao.insertSettings(current.copy(language = langCode))
        }
    }

    fun updateMockRenderTime(seconds: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settings.value ?: SettingsEntity()
            dao.insertSettings(current.copy(mockRenderTime = seconds.coerceIn(2, 30)))
        }
    }

    // --- Video Generator Actions ---
    fun generateVideo(
        prompt: String,
        negativePrompt: String,
        aspectRatio: String,
        quality: String,
        imageInput: String? = null, // image path or base64 if selected
        durationSec: Int = 5,
        transitionEffect: String = "Fade"
    ) {
        val userVal = currentUser.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            // Clear prior completed preview
            recentlyCompletedVideo.value = null

            // Initialize rendering model simulation
            val totalSeconds = settings.value?.mockRenderTime ?: 5
            
            // Randomly select cover image representing our cinematic engine outputs
            val coverImage = if (imageInput != null && imageInput.isNotBlank()) {
                imageInput // Keep selected image
            } else {
                val rand = (1..5).random()
                if (rand == 1) "img_video_cover_1" 
                else if (rand == 2) "img_video_cover_2" 
                else if (rand == 3) "img_dragon_hero" 
                else if (rand == 4) "img_dragon_logo"
                else "img_video_cover_1"
            }

            // Create initial pending video record
            val initialVideo = VideoHistoryEntity(
                prompt = prompt,
                negativePrompt = negativePrompt,
                aspectRatio = aspectRatio,
                quality = quality,
                coverImage = if (coverImage.startsWith("img_") || coverImage.startsWith("/") || coverImage.startsWith("content://") || coverImage.startsWith("file://")) coverImage else "img_video_cover_1",
                status = "Initializing Cinematic Core...",
                progress = 0,
                durationSec = durationSec,
                resolution = when (aspectRatio) {
                    "16:9" -> if (quality == "4K") "3840x2160" else "1920x1080"
                    "9:16" -> if (quality == "4K") "2160x3840" else "1080x1920"
                    else -> if (quality == "4K") "2160x2160" else "1080x1080"
                },
                transitionEffect = transitionEffect
            )

            val videoId = dao.insertVideo(initialVideo).toInt()
            var currentVideo = initialVideo.copy(id = videoId)
            
            activeGeneration.value = currentVideo

            // Call the decoupled video generation backend
            val finalCoverImage = com.example.data.api.VideoBackendFactory.getBackend().generateVideo(
                prompt = prompt,
                negativePrompt = negativePrompt,
                aspectRatio = aspectRatio,
                quality = quality,
                imageInput = imageInput,
                durationSec = durationSec,
                transitionEffect = transitionEffect,
                totalRenderTimeSec = totalSeconds,
                onProgress = { progress, statusText ->
                    currentVideo = currentVideo.copy(
                        progress = progress,
                        status = if (progress == 100) "Ready" else statusText
                    )
                    dao.updateVideo(currentVideo)
                    activeGeneration.value = if (progress == 100) null else currentVideo
                }
            )

            currentVideo = currentVideo.copy(
                coverImage = if (finalCoverImage.startsWith("img_") || finalCoverImage.startsWith("/") || finalCoverImage.startsWith("content://") || finalCoverImage.startsWith("file://")) finalCoverImage else "img_video_cover_1",
                progress = 100,
                status = "Ready"
            )
            dao.updateVideo(currentVideo)
            recentlyCompletedVideo.value = currentVideo
            activeGeneration.value = null
        }
    }

    // Delete single video
    fun deleteVideo(video: VideoHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteVideo(video)
        }
    }

    // Toggle favorite state of a video
    fun toggleFavoriteVideo(video: VideoHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateVideo(video.copy(isFavorite = !video.isFavorite))
        }
    }

    // Delete all video history (Admin/User option)
    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearHistory()
        }
    }
}
