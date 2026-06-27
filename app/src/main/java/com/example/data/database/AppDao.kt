package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Users ---
    @Query("SELECT * FROM users LIMIT 1")
    fun getPrimaryUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUsers()

    // --- Video History ---
    @Query("SELECT * FROM video_history ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<VideoHistoryEntity>>

    @Query("SELECT * FROM video_history WHERE id = :id")
    suspend fun getVideoById(id: Int): VideoHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoHistoryEntity): Long

    @Update
    suspend fun updateVideo(video: VideoHistoryEntity)

    @Delete
    suspend fun deleteVideo(video: VideoHistoryEntity)

    @Query("DELETE FROM video_history WHERE id = :id")
    suspend fun deleteVideoById(id: Int)

    @Query("DELETE FROM video_history")
    suspend fun clearHistory()

    // --- Settings ---
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)

    @Update
    suspend fun updateSettings(settings: SettingsEntity)
}
