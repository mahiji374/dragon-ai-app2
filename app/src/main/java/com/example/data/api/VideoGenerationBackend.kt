package com.example.data.api

import kotlinx.coroutines.delay

/**
 * Clean, decoupled interface representing the video generation backend.
 * Swapping from a local free/open-source simulation wrapper to actual
 * production engines (e.g. HunyuanVideo, CogVideo, or SVD) can be done
 * by simply registering a new implementation of this interface, without
 * affecting any Jetpack Compose UI or ViewModel logic.
 */
interface VideoGenerationBackend {
    /**
     * Executes the video generation request and reports steps/progress/status updates as it runs.
     * @return The final cover image (URL or asset name) generated for the video.
     */
    suspend fun generateVideo(
        prompt: String,
        negativePrompt: String,
        aspectRatio: String,
        quality: String,
        imageInput: String?,
        durationSec: Int,
        transitionEffect: String,
        totalRenderTimeSec: Int,
        onProgress: suspend (progress: Int, status: String) -> Unit
    ): String
}

/**
 * Fast, lightweight, free & open-source simulation backend for HunyuanVideo & SVD.
 * Generates video stream keyframes offline using rule-based aesthetic selectors.
 */
class HunyuanVideoSimulationBackend : VideoGenerationBackend {
    override suspend fun generateVideo(
        prompt: String,
        negativePrompt: String,
        aspectRatio: String,
        quality: String,
        imageInput: String?,
        durationSec: Int,
        transitionEffect: String,
        totalRenderTimeSec: Int,
        onProgress: suspend (progress: Int, status: String) -> Unit
    ): String {
        val stepsCount = 10
        val intervalMs = (totalRenderTimeSec * 1000L) / stepsCount

        // 10 pipeline steps mapping to HunyuanVideo / CogVideo model stages
        for (step in 1..stepsCount) {
            val progress = step * (100 / stepsCount)
            val statusText = when {
                progress <= 10 -> "Submitting prompt latent matrices to HunyuanVideo..."
                progress <= 20 -> "Analyzing conditional negative input guidance..."
                progress <= 35 -> "Running 3D VAE encoder for latent representation projection..."
                progress <= 50 -> "Splicing dual-attention motion latents and vectors..."
                progress <= 65 -> "Applying $transitionEffect multi-frame block latents interpolation..."
                progress <= 80 -> "Denoising flow-matching step iteration ($step/10)..."
                progress <= 90 -> "Executing $quality super-resolution upscaling filters..."
                else -> "Synthesizing free open-source AI Cinematic Video container..."
            }
            onProgress(progress, statusText)
            if (step < stepsCount) {
                delay(intervalMs)
            }
        }

        // Determine final output artwork using aesthetic prompt selectors
        return if (imageInput != null && imageInput.isNotBlank()) {
            imageInput
        } else {
            val lowercasePrompt = prompt.lowercase()
            when {
                lowercasePrompt.contains("dragon") || lowercasePrompt.contains("monster") || lowercasePrompt.contains("creature") -> "img_dragon_hero"
                lowercasePrompt.contains("logo") || lowercasePrompt.contains("brand") -> "img_dragon_logo"
                lowercasePrompt.contains("avatar") || lowercasePrompt.contains("character") || lowercasePrompt.contains("human") || lowercasePrompt.contains("user") || lowercasePrompt.contains("person") -> "img_user_avatar"
                lowercasePrompt.contains("cyber") || lowercasePrompt.contains("neon") || lowercasePrompt.contains("street") || lowercasePrompt.contains("city") -> "img_video_cover_2"
                else -> {
                    val rand = (1..2).random()
                    if (rand == 1) "img_video_cover_1" else "img_video_cover_2"
                }
            }
        }
    }
}

/**
 * Service Locator / Registry for easy backend swapping.
 */
object VideoBackendFactory {
    private var currentBackend: VideoGenerationBackend = HunyuanVideoSimulationBackend()

    fun getBackend(): VideoGenerationBackend = currentBackend

    fun setBackend(backend: VideoGenerationBackend) {
        currentBackend = backend
    }
}
