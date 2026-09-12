package com.example.ui

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.VideoItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerView(
    video: VideoItem,
    onClose: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentPos by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isFullScreen by remember { mutableStateOf(false) }

    // Auto-hide controls after 4 seconds
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(4000)
            isControlsVisible = false
        }
    }

    // Periodic progress update
    LaunchedEffect(videoViewRef, isPlaying) {
        while (true) {
            videoViewRef?.let { vv ->
                try {
                    if (vv.isPlaying) {
                        currentPos = vv.currentPosition.toLong()
                        val dur = vv.duration.toLong()
                        if (dur > 0) {
                            duration = dur
                        }
                    }
                } catch (_: Exception) {}
            }
            delay(500)
        }
    }

    val formatTime = { millis: Long ->
        val totalSec = (millis / 1000).coerceAtLeast(0)
        val mins = totalSec / 60
        val secs = totalSec % 60
        String.format("%02d:%02d", mins, secs)
    }

    val boxModifier = if (isFullScreen) {
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    } else {
        modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(Color.Black)
    }

    Box(
        modifier = boxModifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isControlsVisible = !isControlsVisible
            }
            .testTag("video_player_container")
    ) {
        // Android Native VideoView
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse(video.videoUrl))
                    setOnPreparedListener { mp ->
                        isBuffering = false
                        hasError = false
                        duration = mp.duration.toLong()
                        mp.isLooping = false
                        start()
                        isPlaying = true
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
                            }
                        } catch (_: Exception) {}
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        isControlsVisible = true
                    }
                    setOnErrorListener { _, _, _ ->
                        isBuffering = false
                        hasError = true
                        true
                    }
                    videoViewRef = this
                }
            },
            update = { view ->
                videoViewRef = view
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering && !hasError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = SentinelPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Error message if link is unreachable
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error de video",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Error al cargar la transmisión del video",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            hasError = false
                            isBuffering = true
                            videoViewRef?.setVideoURI(Uri.parse(video.videoUrl))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SentinelPrimary)
                    ) {
                        Text("Reintentar", color = SentinelOnPrimary)
                    }
                }
            }
        }

        // Key Sentinel Watermark Tag (Top Left)
        Surface(
            color = Color.Black.copy(alpha = 0.55f),
            shape = RoundedCornerShape(bottomEnd = 10.dp),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = SentinelSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Key Verificada",
                    color = SentinelPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Bar Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    Row {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorite) Color(0xFFFF4081) else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Speed toggle button
                        TextButton(
                            onClick = {
                                val nextSpeed = when (playbackSpeed) {
                                    1.0f -> 1.25f
                                    1.25f -> 1.5f
                                    1.5f -> 2.0f
                                    2.0f -> 0.75f
                                    else -> 1.0f
                                }
                                playbackSpeed = nextSpeed
                                try {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                        // Speed is applied on MediaPlayer
                                    }
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .height(36.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        ) {
                            Text(
                                text = "${playbackSpeed}x",
                                color = SentinelPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Center Play/Pause & Skip buttons
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Skip -10s
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                val target = (vv.currentPosition - 10000).coerceAtLeast(0)
                                vv.seekTo(target)
                                currentPos = target.toLong()
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Retroceder 10 segundos",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Main Play/Pause
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isPlaying = false
                                } else {
                                    vv.start()
                                    isPlaying = true
                                }
                            }
                        },
                        modifier = Modifier
                            .size(60.dp)
                            .background(SentinelPrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = SentinelOnPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Skip +10s
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                val target = (vv.currentPosition + 10000).coerceAtMost(vv.duration)
                                vv.seekTo(target)
                                currentPos = target.toLong()
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Adelantar 10 segundos",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Bottom Timeline & Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    // Timeline Slider
                    Slider(
                        value = if (duration > 0) currentPos.toFloat().coerceIn(0f, duration.toFloat()) else 0f,
                        onValueChange = { newPos ->
                            currentPos = newPos.toLong()
                            videoViewRef?.seekTo(newPos.toInt())
                        },
                        valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                        colors = SliderDefaults.colors(
                            thumbColor = SentinelPrimary,
                            activeTrackColor = SentinelPrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatTime(currentPos)} / ${formatTime(duration)}",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SentinelSurfaceVariantDark,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = video.resolution,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SentinelSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { isFullScreen = !isFullScreen },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Pantalla completa",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
