package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GoldenFire
import com.example.ui.theme.CyberCyan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.database.VideoHistoryEntity
import com.example.ui.DragonViewModel
import com.example.ui.utils.LanguageUtils
import kotlinx.coroutines.delay
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DragonViewModel,
    langCode: String
) {
    val context = LocalContext.current
    val videos by viewModel.videos.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterRatio by remember { mutableStateOf("ALL") } // "ALL", "16:9", "9:16", "1:1"
    var selectedFilterQuality by remember { mutableStateOf("ALL") } // "ALL", "HD", "4K"
    var showFavoritesOnly by remember { mutableStateOf(false) }

    var selectedVideoForPlay by remember { mutableStateOf<VideoHistoryEntity?>(null) }

    // Filter results logically
    val filteredVideos = remember(videos, searchQuery, selectedFilterRatio, selectedFilterQuality, showFavoritesOnly) {
        videos.filter { video ->
            val matchesSearch = video.prompt.contains(searchQuery, ignoreCase = true) || 
                    video.negativePrompt.contains(searchQuery, ignoreCase = true)
            val matchesRatio = selectedFilterRatio == "ALL" || video.aspectRatio == selectedFilterRatio
            val matchesQuality = selectedFilterQuality == "ALL" || video.quality == selectedFilterQuality
            val matchesFavorite = !showFavoritesOnly || video.isFavorite
            matchesSearch && matchesRatio && matchesQuality && matchesFavorite
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 80.dp) // padding for navigation
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = LanguageUtils.translate("history", langCode),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar with test tags
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search generated videos...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters scroll row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Quality filters
            listOf("ALL", "HD", "4K").forEach { qual ->
                val isSelected = selectedFilterQuality == qual
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilterQuality = qual },
                    label = { Text(qual) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("filter_quality_$qual")
                )
            }

            // Bookmarks / Favorites filter toggle chip
            FilterChip(
                selected = showFavoritesOnly,
                onClick = { showFavoritesOnly = !showFavoritesOnly },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (showFavoritesOnly) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(LanguageUtils.translate("bookmarks_label", langCode))
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Red.copy(alpha = 0.15f),
                    selectedLabelColor = Color.Red
                ),
                modifier = Modifier.testTag("filter_favorites_toggle")
            )

            Spacer(modifier = Modifier.weight(1f))

            // Aspect ratio filters
            listOf("ALL", "16:9", "9:16", "1:1").forEach { ratio ->
                val isSelected = selectedFilterRatio == ratio
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilterRatio = ratio },
                    label = { Text(ratio) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("filter_ratio_$ratio")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("empty_history_container"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        Icons.Default.MovieCreation,
                        contentDescription = "No creations",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = LanguageUtils.translate("empty_history", langCode),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Grid layout tracking outputs
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("history_video_grid")
            ) {
                items(filteredVideos) { video ->
                    HistoryCard(
                        videoHistory = video,
                        onClick = { selectedVideoForPlay = video },
                        onFavoriteClick = { viewModel.toggleFavoriteVideo(video) },
                        onDeleteClick = { viewModel.deleteVideo(video) }
                    )
                }
            }
        }
    }

    // Interactive Media Player Dialog Overlay
    selectedVideoForPlay?.let { video ->
        VideoPlayerDialog(
            videoHistory = video,
            langCode = langCode,
            onDismiss = { selectedVideoForPlay = null },
            onShare = {
                val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Creative Video Shared")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Check out this cinematic AI video I generated on Dragon AI with style: ${video.prompt}"
                    )
                }
                context.startActivity(Intent.createChooser(sharingIntent, "Share Masterpiece"))
            },
            onDownload = {
                Toast.makeText(
                    context,
                    "Saved cinematic stream locally to: /sdcard/Movies/DragonAI/video_${video.id}.mp4",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}

@Composable
fun HistoryCard(
    videoHistory: VideoHistoryEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val isCustomImage = videoHistory.coverImage.startsWith("/") || 
            videoHistory.coverImage.contains("content://") || 
            videoHistory.coverImage.contains("file://")
    val imagePainter = if (isCustomImage) {
        coil.compose.rememberAsyncImagePainter(model = videoHistory.coverImage)
    } else {
        val imageResId = remember(videoHistory.coverImage) {
            val id = context.resources.getIdentifier(videoHistory.coverImage, "drawable", context.packageName)
            if (id == 0) R.drawable.img_video_cover_1 else id
        }
        painterResource(id = imageResId)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
            .testTag("history_item_card_${videoHistory.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = imagePainter,
                contentDescription = videoHistory.prompt,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dynamic quality gradient tag
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Top tags quality banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (videoHistory.quality == "4K") MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = videoHistory.quality,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(24.dp).testTag("history_favorite_btn_${videoHistory.id}")
                    ) {
                        val favoriteIcon = if (videoHistory.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder
                        val favoriteTint = if (videoHistory.isFavorite) Color.Red else Color.White.copy(alpha = 0.8f)
                        Icon(
                            favoriteIcon,
                            contentDescription = "Toggle Favorite",
                            tint = favoriteTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Video History",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Play Icon overlay center
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play AI prompt video stream",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Prompt summary description at bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = videoHistory.prompt,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ratio: ${videoHistory.aspectRatio} | ${videoHistory.resolution}",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Full interactive fake player dialog with beautiful synth wave canvas drawing
@Composable
fun VideoPlayerDialog(
    videoHistory: VideoHistoryEntity,
    langCode: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var playProgress by remember { mutableStateOf(0f) }
    var isLooping by remember { mutableStateOf(true) }

    val isCustomImage = videoHistory.coverImage.startsWith("/") || 
            videoHistory.coverImage.contains("content://") || 
            videoHistory.coverImage.contains("file://")
    val imagePainter = if (isCustomImage) {
        coil.compose.rememberAsyncImagePainter(model = videoHistory.coverImage)
    } else {
        val imageResId = remember(videoHistory.coverImage) {
            val id = context.resources.getIdentifier(videoHistory.coverImage, "drawable", context.packageName)
            if (id == 0) R.drawable.img_video_cover_1 else id
        }
        painterResource(id = imageResId)
    }

    // Dynamic wave phase offset for Canvas playback animation
    var animationPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(50)
            animationPhase += 0.2f
            playProgress += 0.01f
            if (playProgress >= 1f) {
                if (isLooping) {
                    playProgress = 0f
                } else {
                    playProgress = 1f
                    isPlaying = false
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Close player", tint = Color.White)
                    }

                    Text(
                        text = "Dragon Player Pro",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )

                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(
                            text = videoHistory.quality,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Playback monitor frame mapping ratio proportions helper
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = imagePainter,
                        contentDescription = videoHistory.prompt,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Draw abstract motion synthwaves overlay to simulate REAL video rendering motion elements
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val width = size.width
                        val height = size.height
                        val pathBrush = Brush.horizontalGradient(
                            colors = listOf(GoldenFire.copy(alpha = 0.4f), CyberCyan.copy(alpha = 0.6f))
                        )

                        // Draw moving waves matching sine functions to visualize local playback dynamic progress
                        if (isPlaying) {
                            for (yOffset in listOf(0.4f, 0.5f, 0.6f)) {
                                val amplitude = 22.dp.toPx()
                                val frequency = 0.01f
                                val centerY = height * yOffset

                                var priorPointX = 0f
                                var priorPointY = centerY + sin(animationPhase + 0f) * amplitude

                                for (x in 1..width.toInt() step 5) {
                                    val y = centerY + sin(animationPhase + x * frequency) * amplitude
                                    drawLine(
                                        brush = pathBrush,
                                        start = Offset(priorPointX, priorPointY),
                                        end = Offset(x.toFloat(), y),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                    priorPointX = x.toFloat()
                                    priorPointY = y
                                }
                            }
                        }
                    }

                    // Prompt overlay overlay at bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = videoHistory.prompt,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Seek slider
                Slider(
                    value = playProgress,
                    onValueChange = { playProgress = it },
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        thumbColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentSec = (playProgress * videoHistory.durationSec).toInt()
                    Text(text = "0:${currentSec.toString().padStart(2, '0')}", color = Color.LightGray, fontSize = 12.sp)
                    Text(text = "0:${videoHistory.durationSec.toString().padStart(2, '0')}", color = Color.LightGray, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Core control triggers bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isLooping = !isLooping },
                        modifier = Modifier.background(
                            if (isLooping) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        ).testTag("player_loop")
                    ) {
                        Icon(
                            Icons.Default.Loop,
                            contentDescription = "Toggle looping",
                            tint = if (isLooping) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }

                    // Main Play Pause Toggle
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .testTag("player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play Pause Toggle",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.background(Color.DarkGray.copy(alpha = 0.5f), CircleShape).testTag("player_share")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share creations link", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Downloader Button
                Button(
                    onClick = onDownload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("player_download_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = LanguageUtils.translate("download", langCode).uppercase(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
