package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.DragonViewModel
import com.example.ui.utils.LanguageUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import com.example.ui.theme.GoldenFire
import com.example.ui.theme.CyberCyan
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

private data class VideoPlaceholder(
    val id: Int,
    val title: String,
    val prompt: String,
    val imageResId: Int,
    val durationSec: Int,
    val quality: String,
    val aspectRatio: String,
    val styleBadge: String,
    val glowColor: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: DragonViewModel,
    langCode: String
) {
    val user by viewModel.currentUser.collectAsState()
    val activeGen by viewModel.activeGeneration.collectAsState()
    val recentlyCompleted by viewModel.recentlyCompletedVideo.collectAsState()
    val videos by viewModel.videos.collectAsState()

    val blueprints = remember {
        listOf(
            VideoPlaceholder(
                id = -101,
                title = "Cyberpunk Dragon Nexus",
                prompt = "Ultra-realistic 3D render of a neon cyberpunk dragon flying through skyscrapers in neo-Tokyo, rain reflections, volumetric lighting, synthwave style",
                imageResId = R.drawable.img_dragon_hero,
                durationSec = 5,
                quality = "4K",
                aspectRatio = "16:9",
                styleBadge = "CYBERPUNK",
                glowColor = Color(0xFF00F0FF)
            ),
            VideoPlaceholder(
                id = -102,
                title = "Cosmic Singularity Gate",
                prompt = "Cinematic camera pan into a spinning glowing gravity vortex star portal, nebulae dust, hyper-drive light speed streams, epic scale, photorealistic 8k",
                imageResId = R.drawable.img_video_cover_1,
                durationSec = 8,
                quality = "HD",
                aspectRatio = "16:9",
                styleBadge = "SCI-FI",
                glowColor = Color(0xFFFF007F)
            ),
            VideoPlaceholder(
                id = -103,
                title = "Fluid Iridescence Symphony",
                prompt = "Macro shot of iridescent rainbow fluid swirling in turbulent motion under vibrant neon pulses, organic liquid simulation, hypnotic close-up",
                imageResId = R.drawable.img_video_cover_2,
                durationSec = 10,
                quality = "4K",
                aspectRatio = "1:1",
                styleBadge = "ABSTRACT",
                glowColor = Color(0xFF7B2CBF)
            ),
            VideoPlaceholder(
                id = -104,
                title = "Ancient Dragon Sanctuary",
                prompt = "Mystical dragon shrine on top of a sacred misty mountain temple, cherry blossoms falling, cinematic golden hour light, ethereal landscape painting style",
                imageResId = R.drawable.img_dragon_logo,
                durationSec = 5,
                quality = "HD",
                aspectRatio = "9:16",
                styleBadge = "FANTASY",
                glowColor = Color(0xFFF77F00)
            )
        )
    }

    var activeTab by remember { mutableStateOf("TEXT_TO_VIDEO") } // "TEXT_TO_VIDEO", "IMAGE_TO_VIDEO", "VIDEO_TO_VIDEO"
    var promptState by remember { mutableStateOf(TextFieldValue("")) }
    var negativePromptState by remember { mutableStateOf(TextFieldValue("")) }
    val prompt = promptState.text
    val negativePrompt = negativePromptState.text

    val updatePrompt = { newText: String ->
        promptState = TextFieldValue(
            text = newText,
            selection = TextRange(newText.length)
        )
    }
    val updateNegativePrompt = { newText: String ->
        negativePromptState = TextFieldValue(
            text = newText,
            selection = TextRange(newText.length)
        )
    }
    var aspectRatio by remember { mutableStateOf("16:9") } // "16:9", "9:16", "1:1"
    var selectedQuality by remember { mutableStateOf("HD") } // "HD", "4K"
    var selectedDurationSec by remember { mutableStateOf(5) } // 5, 8, 10 seconds
    var selectedTransition by remember { mutableStateOf("Fade") } // "Fade", "Dissolve", "Slide", "Zoom", "Wipe"
    var uploadImageUrl by remember { mutableStateOf<String?>(null) }
    var uploadVideoUrl by remember { mutableStateOf<String?>(null) }
    var selectedVideoIndex by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isGeneratingPreview by remember { mutableStateOf(false) }
    var generatedPreviewImage by remember { mutableStateOf<String?>(null) }

    // Selected starting keyframe image for Image-To-Video tab
    var selectedImageIndex by remember { mutableStateOf(0) }
    val sampleImageDrawables = listOf(
        R.drawable.img_dragon_hero,
        R.drawable.img_video_cover_1,
        R.drawable.img_video_cover_2,
        R.drawable.img_user_avatar,
        R.drawable.img_dragon_logo
    )
    val sampleImageNames = listOf(
        "img_dragon_hero",
        "img_video_cover_1",
        "img_video_cover_2",
        "img_user_avatar",
        "img_dragon_logo"
    )

    val sampleVideoDrawables = listOf(
        R.drawable.img_video_cover_2,
        R.drawable.img_video_cover_1,
        R.drawable.img_dragon_hero,
        R.drawable.img_dragon_logo
    )
    val sampleVideoNames = listOf(
        "ref_neon_city",
        "ref_fluid_flow",
        "ref_cyber_dragon",
        "ref_cosmic_pulse"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp) // Leave padding for bottom navigation
    ) {
        // Workspace Head Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_dragon_hero),
                contentDescription = "Cosmic Dragon Backdrop",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark overlay brush
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Inner User Info Row overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_user_avatar),
                        contentDescription = "User profile photo",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user?.displayName ?: "Architect Maker",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = LanguageUtils.translate("app_tagline", langCode),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }


            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Mode Select Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (activeTab == "TEXT_TO_VIDEO") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable { activeTab = "TEXT_TO_VIDEO" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = "Text to Video Mode Icon",
                            tint = if (activeTab == "TEXT_TO_VIDEO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Text to Video",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (activeTab == "TEXT_TO_VIDEO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (activeTab == "IMAGE_TO_VIDEO") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable { activeTab = "IMAGE_TO_VIDEO" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Image to Video Mode Icon",
                            tint = if (activeTab == "IMAGE_TO_VIDEO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Image to Video",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (activeTab == "IMAGE_TO_VIDEO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (activeTab == "VIDEO_TO_VIDEO") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable { activeTab = "VIDEO_TO_VIDEO" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = "Video to Video Mode Icon",
                            tint = if (activeTab == "VIDEO_TO_VIDEO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Video to Video",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (activeTab == "VIDEO_TO_VIDEO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Text input section
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeTab == "TEXT_TO_VIDEO") {
                                "Video Prompt Input"
                            } else if (activeTab == "IMAGE_TO_VIDEO") {
                                "Image Motion Guidance"
                            } else {
                                "Video Stylization & Remix"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        // Predefined prompt structures Library Dropdown helper
                        TemplateLibraryDropdown(
                            onSelectTemplate = { selectedTemplate ->
                                updatePrompt(selectedTemplate)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = promptState,
                        onValueChange = { promptState = it },
                        placeholder = {
                            Text(
                                text = if (activeTab == "VIDEO_TO_VIDEO") {
                                    "Describe style shift (e.g., convert to a glowing futuristic liquid gold metal structure, cybernetic synthwave...)"
                                } else {
                                    LanguageUtils.translate("prompt_placeholder", langCode)
                                },
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("prompt_input_field"),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            selectionColors = androidx.compose.foundation.text.selection.TextSelectionColors(
                                handleColor = MaterialTheme.colorScheme.primary,
                                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Quick Style Suffixes",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
                    )

                    val styleTags = remember {
                        listOf(
                            "Cinematic" to "Cinematic",
                            "Anime" to "Anime",
                            "Photorealistic" to "Photorealistic",
                            "Cyberpunk" to "Cyberpunk",
                            "Sci-Fi" to "Sci-Fi",
                            "3D Render" to "3D Render",
                            "Fantasy" to "Fantasy",
                            "Retro VHS" to "Retro VHS"
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("style_tags_row")
                    ) {
                        items(styleTags) { (label, value) ->
                            val isSelected = prompt.contains(value, ignoreCase = true)
                            val containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                            val borderLineWidth = if (isSelected) 1.5.dp else 1.dp
                            val borderColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            }
                            val textColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(containerColor)
                                    .border(borderLineWidth, borderColor, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val trimmedPrompt = prompt.trim()
                                        if (isSelected) {
                                            // Toggle off / remove comma style nicely
                                            var newPrompt = trimmedPrompt
                                                .replace(", $value", "", ignoreCase = true)
                                                .replace("$value,", "", ignoreCase = true)
                                                .replace(value, "", ignoreCase = true)
                                                .trim()
                                            if (newPrompt.startsWith(",")) {
                                                newPrompt = newPrompt.substring(1).trim()
                                            }
                                            if (newPrompt.endsWith(",")) {
                                                newPrompt = newPrompt.substring(0, newPrompt.length - 1).trim()
                                            }
                                            updatePrompt(newPrompt)
                                        } else {
                                            // Toggle on / append
                                            updatePrompt(
                                                if (trimmedPrompt.isEmpty()) {
                                                    value
                                                } else {
                                                    "$trimmedPrompt, $value"
                                                }
                                            )
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("style_tag_chip_$label")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Upload Reference Media Section (Visible if not Text-to-Video mode)
            AnimatedVisibility(
                visible = activeTab == "IMAGE_TO_VIDEO" || activeTab == "VIDEO_TO_VIDEO",
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("reference_media_card")
                ) {
                    val context = LocalContext.current

                    // Rich interactive Drag-and-Drop state engines
                    var isDraggingActive by remember { mutableStateOf(false) }
                    var draggedResourceId by remember { mutableStateOf<Int?>(null) }
                    var draggedResourceName by remember { mutableStateOf<String?>(null) }
                    var dragFingerOffset by remember { mutableStateOf(Offset.Zero) }
                    var isHoveredOverDropzone by remember { mutableStateOf(false) }

                    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: android.net.Uri? ->
                        if (uri != null) {
                            val savedPath = saveUriToInternalStorage(context, uri)
                            uploadImageUrl = savedPath
                        }
                    }

                    val singleVideoPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: android.net.Uri? ->
                        if (uri != null) {
                            val savedPath = saveUriToInternalStorage(context, uri)
                            uploadVideoUrl = savedPath
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (activeTab == "IMAGE_TO_VIDEO") Icons.Default.Image else Icons.Default.Videocam,
                                    contentDescription = "Upload reference media icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (activeTab == "IMAGE_TO_VIDEO") "Upload Reference Image" else "Upload Source Video",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            val hasAttachment = if (activeTab == "IMAGE_TO_VIDEO") uploadImageUrl != null else uploadVideoUrl != null
                            if (hasAttachment) {
                                TextButton(
                                    onClick = {
                                        if (activeTab == "IMAGE_TO_VIDEO") uploadImageUrl = null else uploadVideoUrl = null
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Clear selected media",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (activeTab == "IMAGE_TO_VIDEO") {
                            if (uploadImageUrl != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = uploadImageUrl),
                                        contentDescription = "Uploaded reference image preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                                )
                                            )
                                    )

                                    Text(
                                        text = "Uploaded Image Selected",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                    )
                                }
                            } else {
                                val borderStrokeStyle = if (isHoveredOverDropzone) {
                                    androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 6f,
                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                                    )
                                } else {
                                    androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 3f,
                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(115.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .drawBehind {
                                            drawRoundRect(
                                                color = if (isHoveredOverDropzone) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.4f),
                                                style = borderStrokeStyle,
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                                            )
                                        }
                                        .background(
                                            if (isHoveredOverDropzone) Color(0xFFFFD700).copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
                                        )
                                        .clickable {
                                            singlePhotoPickerLauncher.launch("image/*")
                                        }
                                        .testTag("upload_image_trigger_box"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isHoveredOverDropzone) Icons.Default.CloudDownload else Icons.Default.FileUpload,
                                            contentDescription = "Upload or drag image",
                                            tint = if (isHoveredOverDropzone) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (isHoveredOverDropzone) "🔥 DROP IMAGE TO UPLOAD!" else "Drag & drop image file here",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHoveredOverDropzone) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "or click to select from local library (Optional)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Interactive Drag & Drop Sandbox drawer for users inside web emulator
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B24).copy(alpha = 0.6f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.DragIndicator,
                                            contentDescription = "Drop help",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "DRAG & DROP SANDBOX",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Long-press & drag any frame below directly over the target above to upload instantly:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("drag_sandbox_row")
                                    ) {
                                        items(sampleImageDrawables.size) { index ->
                                            val drawable = sampleImageDrawables[index]
                                            val name = sampleImageNames[index]
                                            Box(
                                                modifier = Modifier
                                                    .size(75.dp, 50.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                                    .pointerInput(index) {
                                                        detectDragGesturesAfterLongPress(
                                                            onDragStart = {
                                                                isDraggingActive = true
                                                                draggedResourceId = drawable
                                                                draggedResourceName = name
                                                                dragFingerOffset = Offset.Zero
                                                            },
                                                            onDrag = { change, dragAmount ->
                                                                change.consume()
                                                                dragFingerOffset += dragAmount
                                                                // If dragged upwards/towards dropzone
                                                                isHoveredOverDropzone = dragFingerOffset.y < -50f
                                                            },
                                                            onDragEnd = {
                                                                if (isHoveredOverDropzone) {
                                                                    val resUri = android.net.Uri.parse("android.resource://${context.packageName}/$drawable")
                                                                    val savedPath = saveUriToInternalStorage(context, resUri)
                                                                    uploadImageUrl = savedPath
                                                                    Toast.makeText(context, "Frame Dragged & Dropped Successfully!", Toast.LENGTH_SHORT).show()
                                                                }
                                                                isDraggingActive = false
                                                                draggedResourceId = null
                                                                draggedResourceName = null
                                                                isHoveredOverDropzone = false
                                                            },
                                                            onDragCancel = {
                                                                isDraggingActive = false
                                                                draggedResourceId = null
                                                                draggedResourceName = null
                                                                isHoveredOverDropzone = false
                                                            }
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(id = drawable),
                                                    contentDescription = "Sandbox option $index",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.3f))
                                                )
                                                Icon(
                                                    Icons.Default.DragHandle,
                                                    contentDescription = "draggable indicator icon",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // VIDEO_TO_VIDEO Upload section
                            if (uploadVideoUrl != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .background(Color.Black)
                                ) {
                                    // Animated pulse effect representing dynamic reference stream
                                    Icon(
                                        Icons.Default.PlayCircle,
                                        contentDescription = "Video uploaded play placeholder",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(48.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                                )
                                            )
                                    )

                                    Text(
                                        text = "Uploaded Video Selected\n${uploadVideoUrl?.substringAfterLast("/")?.take(30)}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            border = BorderStroke(
                                                1.5.dp,
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                                    )
                                                )
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                        .clickable {
                                            singleVideoPickerLauncher.launch("video/*")
                                        }
                                        .testTag("upload_video_trigger_box"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Videocam,
                                            contentDescription = "Add reference video",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Select reference source video file (Optional)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Image Selection node if IMAGE-TO-VIDEO active
            AnimatedVisibility(
                visible = activeTab == "IMAGE_TO_VIDEO",
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select Starting Keyframe",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("image_keyframes_row")
                    ) {
                        items(sampleImageDrawables.size) { index ->
                            val drawable = sampleImageDrawables[index]
                            val isSelected = selectedImageIndex == index
                            Box(
                                modifier = Modifier
                                    .size(100.dp, 60.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedImageIndex = index }
                            ) {
                                Image(
                                    painter = painterResource(id = drawable),
                                    contentDescription = "Keyframe option $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Video Selection node if VIDEO-TO-VIDEO active
            AnimatedVisibility(
                visible = activeTab == "VIDEO_TO_VIDEO",
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select Base Motion Video",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("video_keyframes_row")
                    ) {
                        items(sampleVideoDrawables.size) { index ->
                            val drawable = sampleVideoDrawables[index]
                            val name = sampleVideoNames[index]
                            val isSelected = selectedVideoIndex == index
                            Box(
                                modifier = Modifier
                                    .size(100.dp, 60.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedVideoIndex = index }
                            ) {
                                Image(
                                    painter = painterResource(id = drawable),
                                    contentDescription = "Video base option $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                            )
                                        )
                                )
                                Text(
                                    text = name.substringAfter("ref_").replace("_", " ").uppercase(),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Negative prompt section
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = negativePromptState,
                    onValueChange = { negativePromptState = it },
                    placeholder = {
                        Text(
                            text = LanguageUtils.translate("negative_prompt", langCode),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("negative_prompt_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        selectionColors = androidx.compose.foundation.text.selection.TextSelectionColors(
                            handleColor = MaterialTheme.colorScheme.primary,
                            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Aspect ratio horizontal selection
            Text(
                text = LanguageUtils.translate("aspect_ratio", langCode),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp).testTag("aspect_ratio_section_title")
            )
            Text(
                text = "Select your desired spatial dimension rendering format",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val aspectRatios = listOf(
                Triple("16:9", Icons.Default.Square, "Wide Landscape (16:9)"),
                Triple("9:16", Icons.Default.VerticalDistribute, "Vertical Portrait (9:16)"),
                Triple("1:1", Icons.Default.CropSquare, "Square Post (1:1)")
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                var menuExpanded by remember { mutableStateOf(false) }
                val currentSelection = aspectRatios.find { it.first == aspectRatio } ?: aspectRatios[0]

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { menuExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("ratio_dropdown_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    currentSelection.second,
                                    contentDescription = "Selected aspect ratio representation icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = currentSelection.third,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown indicators",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .testTag("ratio_dropdown_menu")
                    ) {
                        aspectRatios.forEach { (ratio, icon, label) ->
                            val isSelected = aspectRatio == ratio
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = "$label icon",
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = {
                                    aspectRatio = ratio
                                    menuExpanded = false
                                },
                                modifier = Modifier.testTag("ratio_menu_item_$ratio")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Output qualities (HD cost 10, 4K cost 25)
            Text(
                text = LanguageUtils.translate("resolution_label", langCode),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Pair("HD", "10 CR"),
                    Pair("4K", "25 CR")
                ).forEach { (qual, cost) ->
                    val isSelected = selectedQuality == qual
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedQuality = qual }
                            .testTag("quality_card_$qual")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = qual + " Render",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (qual == "4K") "Extreme 2160p Cinematic" else "Standard 1080p Stream",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    else Color.DarkGray.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = cost,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Video Duration Configuration Panel (Combined discrete Slider + drop-down)
            Text(
                text = LanguageUtils.translate("duration_label", langCode),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp).testTag("duration_section_title")
            )
            Text(
                text = LanguageUtils.translate("duration_sub", langCode),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(5, 8, 10).forEach { sec ->
                    val isSelected = selectedDurationSec == sec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedDurationSec = sec }
                            .padding(vertical = 12.dp)
                            .testTag("duration_selector_$sec"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${sec}s",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Duration",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Video Transition Effect Configuration Panel
            Text(
                text = LanguageUtils.translate("transition_label", langCode),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp).testTag("transition_section_title")
            )
            Text(
                text = LanguageUtils.translate("transition_sub", langCode),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info label or Current selection display on the left
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    val currentIcon = when (selectedTransition) {
                        "Fade" -> Icons.Default.BlurOn
                        "Dissolve" -> Icons.Default.Grain
                        "Slide" -> Icons.Default.East
                        "Zoom" -> Icons.Default.ZoomIn
                        else -> Icons.Default.Waves
                    }
                    Icon(
                        currentIcon,
                        contentDescription = "Selected transition icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = selectedTransition,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Motion flow style",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Dropdown box on the right
                Box(
                    modifier = Modifier.weight(0.7f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    var transitionExpanded by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { transitionExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("transition_dropdown_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Select",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown indicators",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = transitionExpanded,
                        onDismissRequest = { transitionExpanded = false },
                        modifier = Modifier
                            .width(130.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .testTag("transition_dropdown_menu")
                    ) {
                        listOf(
                            Pair("Fade", Icons.Default.BlurOn),
                            Pair("Dissolve", Icons.Default.Grain),
                            Pair("Slide", Icons.Default.East),
                            Pair("Zoom", Icons.Default.ZoomIn),
                            Pair("Wipe", Icons.Default.Waves)
                        ).forEach { (effect, icon) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (selectedTransition == effect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = effect,
                                            fontWeight = if (selectedTransition == effect) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = {
                                    selectedTransition = effect
                                    transitionExpanded = false
                                },
                                modifier = Modifier.testTag("transition_menu_item_$effect")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Dynamic Active Render Progress Bar Card
            AnimatedVisibility(
                visible = activeGen != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                activeGen?.let { renderTask ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .testTag("rendering_progress_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Rendering AI Stream",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text(
                                    text = "${renderTask.progress}%",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Fluid custom progress indicator
                            LinearProgressIndicator(
                                progress = { renderTask.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = renderTask.status,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Latest Completed Generation Display Area (Fades in once completed)
            AnimatedVisibility(
                visible = recentlyCompleted != null && activeGen == null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                recentlyCompleted?.let { video ->
                    LatestOutputDisplay(
                        video = video,
                        langCode = langCode,
                        onDismiss = {
                            viewModel.recentlyCompletedVideo.value = null
                        }
                    )
                }
            }

            // QUICK PREVIEW CONTAINER BLOCK
            AnimatedVisibility(
                visible = isGeneratingPreview || generatedPreviewImage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("quick_preview_container")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LanguageUtils.translate("quick_preview_label", langCode),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (generatedPreviewImage != null && !isGeneratingPreview) {
                                IconButton(
                                    onClick = { generatedPreviewImage = null },
                                    modifier = Modifier.size(24.dp).testTag("clear_preview_button")
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear preview draft",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isGeneratingPreview) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp).testTag("preview_generating_indicator"),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = LanguageUtils.translate("quick_preview_status", langCode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            generatedPreviewImage?.let { previewImg ->
                                val imageResId = remember(previewImg) {
                                    val id = context.resources.getIdentifier(previewImg, "drawable", context.packageName)
                                    if (id == 0) R.drawable.img_video_cover_1 else id
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black)
                                ) {
                                    Image(
                                        painter = painterResource(id = imageResId),
                                        contentDescription = "Quick Static Preview Frame",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Custom low-res translucent filter layer overlays
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                                )
                                            )
                                    )

                                    // Aspect Ratio overlay indicators / tag badges
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Draft Frame ($aspectRatio)",
                                            color = GoldenFire,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Low-Resolution Static",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "Prompt: \"$prompt\"",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DUAL ACTION TRIGGERS (QUICK PREVIEW & HD RENDER)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // BUTTON 1: QUICK PREVIEW BUTTON
                val isPreviewButtonEnabled = prompt.isNotBlank() && activeGen == null && !isGeneratingPreview
                OutlinedButton(
                    onClick = {
                        val lowercasePrompt = prompt.lowercase()
                        val matchedPreviewImgName = when {
                            lowercasePrompt.contains("dragon") || lowercasePrompt.contains("monster") || lowercasePrompt.contains("creature") -> "img_dragon_hero"
                            lowercasePrompt.contains("logo") || lowercasePrompt.contains("brand") -> "img_dragon_logo"
                            lowercasePrompt.contains("avatar") || lowercasePrompt.contains("character") || lowercasePrompt.contains("human") || lowercasePrompt.contains("user") || lowercasePrompt.contains("person") -> "img_user_avatar"
                            lowercasePrompt.contains("cyber") || lowercasePrompt.contains("neon") || lowercasePrompt.contains("street") || lowercasePrompt.contains("city") -> "img_video_cover_2"
                            else -> "img_video_cover_1"
                        }
                        isGeneratingPreview = true
                        scope.launch {
                            delay(1200) // Synthesizing frame latency delay
                            isGeneratingPreview = false
                            generatedPreviewImage = matchedPreviewImgName
                        }
                    },
                    enabled = isPreviewButtonEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("quick_preview_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (isPreviewButtonEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.RemoveRedEye,
                            contentDescription = "Preview icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageUtils.translate("quick_preview_btn", langCode),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                // BUTTON 2: FULL HD CINEMATIC VIDEO ACTION BUTTON
                val isButtonEnabled = prompt.isNotBlank() && activeGen == null
                Button(
                    onClick = {
                        val keyframeImage = if (uploadVideoUrl != null) {
                            uploadVideoUrl
                        } else if (activeTab == "VIDEO_TO_VIDEO") {
                            sampleVideoNames[selectedVideoIndex]
                        } else if (uploadImageUrl != null) {
                            uploadImageUrl
                        } else if (generatedPreviewImage != null) {
                            // If quick preview frame was selected, proceed with full HD generation from this preview frame!
                            generatedPreviewImage
                        } else if (activeTab == "IMAGE_TO_VIDEO") {
                            sampleImageNames[selectedImageIndex]
                        } else null
                        viewModel.generateVideo(
                            prompt = prompt,
                            negativePrompt = negativePrompt,
                            aspectRatio = aspectRatio,
                            quality = selectedQuality,
                            imageInput = keyframeImage,
                            durationSec = selectedDurationSec,
                            transitionEffect = selectedTransition
                        )
                        // Clear fields upon launch
                        updatePrompt("")
                        updateNegativePrompt("")
                        uploadImageUrl = null
                        uploadVideoUrl = null
                        generatedPreviewImage = null
                    },
                    enabled = isButtonEnabled,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(56.dp)
                        .shadow(
                            elevation = if (isButtonEnabled) 8.dp else 0.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = MaterialTheme.colorScheme.primary,
                            spotColor = MaterialTheme.colorScheme.primary
                        )
                        .testTag("ignite_engine_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.MovieFilter,
                            contentDescription = "Generator core action",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageUtils.translate("generate_btn", langCode),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unlimited Premium notice
            Text(
                text = "Unlimited Generation Core Enabled (Private Beta)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Title and sub-title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_history_section_title"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "History Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Generation History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your previous prompts & video streams",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Responsive Video Stream Grid
            Text(
                text = "Your Generated Streams",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            ResponsiveVideoGrid(
                items = videos,
                emptyContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                RoundedCornerShape(12.dp)
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.VideoLibrary,
                                contentDescription = "No creations yet",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No generations found",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enter a prompt above to ignite your first creation!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            ) { video ->
                InteractiveVideoCard(
                    video = video,
                    viewModel = viewModel,
                    recentlyCompleted = recentlyCompleted,
                    onPlayClick = {
                        viewModel.recentlyCompletedVideo.value = video
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Responsive Placeholder Blueprints / Inspiration Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Premium Blueprint Templates",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                ) {
                    Text(
                        text = "TAP TO ACTIVATE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = "Hover or tap on these ready-to-generate inspiration blueprints",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val scope = rememberCoroutineScope()
            ResponsiveVideoGrid(
                items = blueprints
            ) { blueprint ->
                InteractivePlaceholderCard(
                    blueprint = blueprint,
                    onBlueprintSelect = { selectedPrompt ->
                        updatePrompt(selectedPrompt)
                        // Smoothly scroll back to the top of the screen so user sees their new loaded prompt
                        scope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun <T> ResponsiveVideoGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    emptyContent: @Composable (() -> Unit)? = null,
    itemContent: @Composable (T) -> Unit
) {
    if (items.isEmpty()) {
        emptyContent?.invoke()
    } else {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            val screenWidth = maxWidth
            val columns = when {
                screenWidth < 400.dp -> 2
                screenWidth < 680.dp -> 3
                else -> 4
            }
            
            val chunkedItems = remember(items, columns) { items.chunked(columns) }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                chunkedItems.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                itemContent(item)
                            }
                        }
                        // Fill remaining spaces in the row with empty Spacers to keep correct item widths
                        val emptySlots = columns - rowItems.size
                        if (emptySlots > 0) {
                            repeat(emptySlots) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveVideoCard(
    video: com.example.data.database.VideoHistoryEntity,
    viewModel: DragonViewModel,
    recentlyCompleted: com.example.data.database.VideoHistoryEntity?,
    onPlayClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.85f else 0.15f,
        animationSpec = spring()
    )
    val glowColor = MaterialTheme.colorScheme.primary
    val borderColor = if (isHovered) glowColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val borderThickness = if (isHovered) 2.dp else 1.dp
    
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 12.dp else 2.dp,
        animationSpec = spring()
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isHovered) 1.0f else 0.85f,
        animationSpec = spring()
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(elevation, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                borderThickness,
                borderColor.copy(alpha = borderAlpha),
                RoundedCornerShape(14.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                        }
                    }
                }
            }
            .clickable {
                onPlayClick()
            }
            .testTag("dashboard_history_card_${video.id}")
    ) {
        val isCustomImage = video.coverImage.startsWith("/") ||
                video.coverImage.contains("content://") ||
                video.coverImage.contains("file://")
        val cardPainter = if (isCustomImage) {
            rememberAsyncImagePainter(model = video.coverImage)
        } else {
            val context = LocalContext.current
            val imageResId = remember(video.coverImage) {
                val id = context.resources.getIdentifier(video.coverImage, "drawable", context.packageName)
                if (id == 0) R.drawable.img_video_cover_1 else id
            }
            painterResource(id = imageResId)
        }

        Image(
            painter = cardPainter,
            contentDescription = video.prompt,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Play Button scale animation on hover
        val playButtonScale by animateFloatAsState(
            targetValue = if (isHovered) 1.2f else 1.0f,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f)
        )

        IconButton(
            onClick = onPlayClick,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = playButtonScale
                    scaleY = playButtonScale
                }
                .size(38.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    CircleShape
                )
                .testTag("dashboard_history_play_btn_${video.id}")
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Quick preview in display player",
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }

        // Floating action controls (Favorite and Delete)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    viewModel.toggleFavoriteVideo(video)
                },
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        Color.Black.copy(alpha = 0.65f),
                        CircleShape
                    )
                    .testTag("dashboard_history_favorite_btn_${video.id}")
            ) {
                val favoriteIcon = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder
                val favoriteTint = if (video.isFavorite) Color.Red else Color.White
                Icon(
                    favoriteIcon,
                    contentDescription = "Toggle Favorite status",
                    tint = favoriteTint,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = {
                    viewModel.deleteVideo(video)
                    if (recentlyCompleted?.id == video.id) {
                        viewModel.recentlyCompletedVideo.value = null
                    }
                },
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        Color.Black.copy(alpha = 0.65f),
                        CircleShape
                    )
                    .testTag("dashboard_history_delete_btn_${video.id}")
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete generation record",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Ratio Badge
        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
        ) {
            Text(
                text = video.aspectRatio,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }

        // Title and description overlay at the bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .alpha(contentAlpha)
        ) {
            Text(
                text = video.prompt,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Quality: ${video.quality} • ${video.durationSec}s",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun InteractivePlaceholderCard(
    blueprint: VideoPlaceholder,
    onBlueprintSelect: (String) -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.95f else 0.25f,
        animationSpec = spring()
    )
    
    val borderThickness = if (isHovered) 2.dp else 1.dp
    
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 14.dp else 2.dp,
        animationSpec = spring()
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(elevation, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                borderThickness,
                blueprint.glowColor.copy(alpha = borderAlpha),
                RoundedCornerShape(14.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                        }
                    }
                }
            }
            .clickable {
                onBlueprintSelect(blueprint.prompt)
                Toast.makeText(context, "Loaded Prompt blueprint: ${blueprint.title}", Toast.LENGTH_SHORT).show()
            }
            .testTag("dashboard_placeholder_card_${blueprint.id}")
    ) {
        Image(
            painter = painterResource(id = blueprint.imageResId),
            contentDescription = blueprint.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Colorful radial/vignette gradient overlay based on its theme glowColor
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            blueprint.glowColor.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Custom Glowing Style Badge (Top-Left)
        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = blueprint.glowColor),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
        ) {
            Text(
                text = blueprint.styleBadge,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }

        // Preset Ratio badge (Top-Right)
        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
        ) {
            Text(
                text = blueprint.aspectRatio,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }

        // Play/Instant-Use icon in the middle that glows on hover
        val actionButtonScale by animateFloatAsState(
            targetValue = if (isHovered) 1.2f else 1.0f,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f)
        )
        val actionIcon = if (isHovered) Icons.Default.AutoAwesome else Icons.Default.Add

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = actionButtonScale
                    scaleY = actionButtonScale
                }
                .size(36.dp)
                .background(
                    if (isHovered) blueprint.glowColor else Color.Black.copy(alpha = 0.7f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                actionIcon,
                contentDescription = "Load prompt blueprint",
                tint = if (isHovered) Color.Black else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // Prompt text & meta details at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = blueprint.title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = blueprint.prompt,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun saveUriToInternalStorage(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "ref_${System.currentTimeMillis()}.jpg"
        val outputFile = java.io.File(context.filesDir, fileName)
        java.io.FileOutputStream(outputFile).use { outputStream ->
            inputStream.use { input ->
                input.copyTo(outputStream)
            }
        }
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun saveVideoLocally(context: android.content.Context, prompt: String, id: Int): String? {
    val fileName = "DragonAI_${id}_${System.currentTimeMillis()}.mp4"
    val mockMp4Bytes = byteArrayOf(
        0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70, // ftyp atom signature block
        0x6d, 0x70, 0x34, 0x32, 0x00, 0x00, 0x00, 0x00,
        0x6d, 0x70, 0x34, 0x32, 0x69, 0x73, 0x6f, 0x6d
    ) + ("Dragon AI Cinematic Video Stream for Prompt: $prompt").toByteArray(Charsets.UTF_8)

    // Try modern MediaStore Q+ insert first (does not require runtime permissions)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        try {
            val contentResolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Movies/DragonAI")
            }
            val contentUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val uri = contentResolver.insert(contentUri, contentValues)
            if (uri != null) {
                contentResolver.openOutputStream(uri).use { outputStream ->
                    outputStream?.write(mockMp4Bytes)
                }
                return "Movies/DragonAI/$fileName"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fallback path 1: External storage environment movies directory
    try {
        val moviesDirectory = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES)
        val dragonDirectory = java.io.File(moviesDirectory, "DragonAI")
        if (!dragonDirectory.exists()) {
            dragonDirectory.mkdirs()
        }
        val targetFile = java.io.File(dragonDirectory, fileName)
        java.io.FileOutputStream(targetFile).use { fos ->
            fos.write(mockMp4Bytes)
        }
        return targetFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Fallback path 2: App's files sandbox
    try {
        val targetFile = java.io.File(context.filesDir, fileName)
        java.io.FileOutputStream(targetFile).use { fos ->
            fos.write(mockMp4Bytes)
        }
        return targetFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

@Composable
fun LatestOutputDisplay(
    video: com.example.data.database.VideoHistoryEntity,
    langCode: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var playProgress by remember { mutableStateOf(0f) }
    var isLooping by remember { mutableStateOf(true) }
    var animationPhase by remember { mutableStateOf(0f) }

    val isCustomImage = video.coverImage.startsWith("/") || 
            video.coverImage.contains("content://") || 
            video.coverImage.contains("file://")
    val imagePainter = if (isCustomImage) {
        rememberAsyncImagePainter(model = video.coverImage)
    } else {
        val imageResId = remember(video.coverImage) {
            val id = context.resources.getIdentifier(video.coverImage, "drawable", context.packageName)
            if (id == 0) R.drawable.img_video_cover_1 else id
        }
        painterResource(id = imageResId)
    }

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

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .testTag("latest_video_output_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Success Star Sparkle",
                        tint = GoldenFire,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "LATEST GENERATION OUTPUT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Generation Completed Successfully",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("latest_video_dismiss_button")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss video output player",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Video Screen Display Frame with Wave canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Image(
                    painter = imagePainter,
                    contentDescription = video.prompt,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Draw waves simulation for movement inside the Display card
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val pathBrush = Brush.horizontalGradient(
                        colors = listOf(GoldenFire.copy(alpha = 0.4f), CyberCyan.copy(alpha = 0.6f))
                    )

                    if (isPlaying) {
                        for (yOffset in listOf(0.45f, 0.55f, 0.65f)) {
                            val amplitude = 18.dp.toPx()
                            val frequency = 0.012f
                            val centerY = height * yOffset

                            var priorPointX = 0f
                            var priorPointY = centerY + sin(animationPhase + 0f) * amplitude

                            for (x in 1..width.toInt() step 5) {
                                val y = centerY + sin(animationPhase + x * frequency) * amplitude
                                drawLine(
                                    brush = pathBrush,
                                    start = Offset(priorPointX, priorPointY),
                                    end = Offset(x.toFloat(), y),
                                    strokeWidth = 2.5.dp.toPx()
                                )
                                priorPointX = x.toFloat()
                                priorPointY = y
                            }
                        }
                    }
                }

                // Aspect Ratio and Quality Badges Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.End
                ) {
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = video.quality,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = video.aspectRatio,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Interactive middle Play state trigger overlay when not playing
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clickable { isPlaying = true }
                            .testTag("latest_video_middle_play_trigger"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play video",
                                tint = Color.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Video details (Prompt, Duration, Resolution)
            Text(
                text = video.prompt,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resolution: ${video.resolution}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Duration: ${video.durationSec}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback Seek Slider Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.testTag("latest_video_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle play pause",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Slider(
                    value = playProgress,
                    onValueChange = { playProgress = it },
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        thumbColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val currentSec = (playProgress * video.durationSec).toInt()
                Text(
                    text = "0:${currentSec.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Download and Share buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isLooping = !isLooping },
                        modifier = Modifier
                            .background(
                                if (isLooping) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .testTag("latest_video_loop_toggle")
                    ) {
                        Icon(
                            Icons.Default.Loop,
                            contentDescription = "Toggle looping",
                            tint = if (isLooping) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
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
                        modifier = Modifier.testTag("latest_video_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Share", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val savedPath = saveVideoLocally(context, video.prompt, video.id)
                            if (savedPath != null) {
                                Toast.makeText(
                                    context,
                                    "Successfully saved locally to:\n$savedPath",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to save video file locally.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("latest_video_download_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateLibraryDropdown(
    onSelectTemplate: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val templates = remember {
        listOf(
            "Panning Landscape Shot" to "Panning landscape shot of [Subject], majestic mountains, golden hour lighting, sweeping dynamic camera movement, high fidelity, 8k",
            "Close-up Portrait" to "Close-up portrait movement of [Subject], extremely detailed, studio lighting, shallow depth of field, slow-motion facial expression details, 4k",
            "First-Person Action" to "First-person POV action shot of running through [Location], fast kinetic flow, camera shake, immersive depth, motion blur",
            "Dolly Zoom Suspense" to "Dolly zoom suspense camera movement centered on [Subject], background compressing, dramatic lighting, high tension, epic cinematic composition",
            "Drone Flyover Aerial" to "Drone flyover aerial view of [Location], looking straight down, slow soaring camera rotation, ultra realistic textures, professional film look",
            "Sci-Fi Hologram Zoom" to "Slow camera zoom into a futuristic sci-fi hologram of [Subject], neon blue glowing digital interference, dark abstract backdrop",
            "Anime Epic Battle" to "Epic battle sequence, anime style, colorful energy beams radiating from [Subject], dynamic screen vibration, hand-drawn detailing, celestial backdrop"
        )
    }

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier
                .height(34.dp)
                .testTag("template_library_dropdown_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoStories,
                    contentDescription = "Templates Logo",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Templates",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown indicator",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(260.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .testTag("template_library_dropdown_menu")
        ) {
            Text(
                text = "SELECT PROMPT TACTIC",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            templates.forEach { (name, finalPrompt) ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = finalPrompt,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    onClick = {
                        onSelectTemplate(finalPrompt)
                        expanded = false
                    },
                    modifier = Modifier.testTag("template_item_$name")
                )
            }
        }
    }
}

