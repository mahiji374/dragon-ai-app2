package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "admin_user",
    val email: String = "admin@dragon.ai",
    val displayName: String = "Dragon Maker",
    val avatar: String = "img_user_avatar",
    val credits: Int = 100,
    val joinedAt: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = true
) : Serializable

@Entity(tableName = "video_history")
data class VideoHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val negativePrompt: String = "",
    val aspectRatio: String = "16:9",
    val quality: String = "HD",
    val coverImage: String = "img_video_cover_1",
    val status: String = "Ready", // "Idle", "Analyzing Prompt...", "Synthesizing Motion...", "Upscaling Frames...", "Ready"
    val progress: Int = 100, // 0 to 100
    val timestamp: Long = System.currentTimeMillis(),
    val durationSec: Int = 5,
    val resolution: String = "1920x1080",
    val isFavorite: Boolean = false,
    val transitionEffect: String = "Fade"
) : Serializable

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isDark: Boolean = true,
    val language: String = "en", // "en", "es", "hi", "zh", "fr"
    val autoEnhance: Boolean = true,
    val mockRenderTime: Int = 5 // seconds it takes to simulate rendering
) : Serializable
