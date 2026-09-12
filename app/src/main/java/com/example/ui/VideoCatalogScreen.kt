package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCatalogScreen(
    keyManager: FirebaseKeyManager,
    userStore: VideoUserStore,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeKey = remember { keyManager.getActiveKey() }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }
    var currentPlayingVideo by remember { mutableStateOf<VideoItem?>(VideoRepository.sampleVideos.firstOrNull()) }
    var showKeyDetailsDialog by remember { mutableStateOf(false) }
    var showAddVideoDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    var favoriteIds by remember { mutableStateOf(userStore.getFavoriteIds()) }
    var customVideos by remember { mutableStateOf(userStore.getCustomVideos()) }

    // Combine sample videos and custom videos
    val allVideos = remember(customVideos, favoriteIds) {
        val list = mutableListOf<VideoItem>()
        list.addAll(customVideos)
        list.addAll(VideoRepository.sampleVideos)
        list.map { it.copy(isFavorite = favoriteIds.contains(it.id)) }
    }

    // Filtered videos
    val filteredVideos = remember(allVideos, selectedCategory, searchQuery) {
        allVideos.filter { video ->
            val matchesCat = when (selectedCategory) {
                "Todos" -> true
                "Favoritos" -> favoriteIds.contains(video.id)
                "Mis Videos" -> video.isCustom
                else -> video.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                video.title.contains(searchQuery, ignoreCase = true) ||
                        video.author.contains(searchQuery, ignoreCase = true) ||
                        video.description.contains(searchQuery, ignoreCase = true)
            }
            matchesCat && matchesQuery
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SentinelBackgroundDark),
        containerColor = SentinelBackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Video Sentinel",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SentinelPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SentinelSecondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = activeKey?.tier ?: "VIP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SentinelSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Key: ${activeKey?.key ?: "Activa"}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Add custom video URL
                    IconButton(
                        onClick = { showAddVideoDialog = true },
                        modifier = Modifier.testTag("add_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddLink,
                            contentDescription = "Agregar enlace de video",
                            tint = SentinelPrimary
                        )
                    }

                    // Key details & Firebase status
                    IconButton(
                        onClick = { showKeyDetailsDialog = true },
                        modifier = Modifier.testTag("key_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Estado de Key Sentinel",
                            tint = SentinelSecondary
                        )
                    }

                    // Exit / Revoke
                    IconButton(
                        onClick = { showLogoutConfirmDialog = true },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión de key",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SentinelSurfaceDark,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddVideoDialog = true },
                containerColor = SentinelPrimary,
                contentColor = SentinelOnPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_custom_video")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir video propio")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Embedded Video Player (if active)
            currentPlayingVideo?.let { video ->
                VideoPlayerView(
                    video = video,
                    onClose = { currentPlayingVideo = null },
                    onToggleFavorite = {
                        userStore.toggleFavorite(video.id)
                        favoriteIds = userStore.getFavoriteIds()
                    },
                    isFavorite = favoriteIds.contains(video.id)
                )

                // Playing Video Details Card
                Surface(
                    color = SentinelSurfaceDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = video.author,
                                fontSize = 12.sp,
                                color = SentinelPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "•", color = SentinelOutlineDark)
                            Text(
                                text = video.views,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(text = "•", color = SentinelOutlineDark)
                            Surface(
                                color = SentinelSurfaceVariantDark,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = video.category,
                                    fontSize = 10.sp,
                                    color = SentinelSecondary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = video.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                HorizontalDivider(color = SentinelOutlineDark, thickness = 1.dp)
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("video_search_input"),
                placeholder = { Text("Buscar videos o canales...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar búsqueda",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SentinelSurfaceDark,
                    unfocusedContainerColor = SentinelSurfaceDark,
                    focusedBorderColor = SentinelPrimary,
                    unfocusedBorderColor = SentinelOutlineDark
                )
            )

            // Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(VideoRepository.categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SentinelPrimary,
                            selectedLabelColor = SentinelOnPrimary,
                            containerColor = SentinelSurfaceDark,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) SentinelPrimary else SentinelOutlineDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Videos List
            if (filteredVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = SentinelOutlineDark,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron videos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Intenta con otra categoría o agrega tu propio enlace de video.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("video_feed_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredVideos, key = { it.id }) { video ->
                        val isFav = favoriteIds.contains(video.id)
                        val isCurrentlyPlaying = currentPlayingVideo?.id == video.id

                        VideoFeedCard(
                            video = video,
                            isPlaying = isCurrentlyPlaying,
                            isFavorite = isFav,
                            onPlayClick = {
                                currentPlayingVideo = video
                            },
                            onToggleFavorite = {
                                userStore.toggleFavorite(video.id)
                                favoriteIds = userStore.getFavoriteIds()
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }

    // Dialog: Key Details & Firebase Binding
    if (showKeyDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDetailsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SentinelPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seguridad Key Sentinel")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Paquete Vinculado:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FirebaseKeyManager.TARGET_PACKAGE,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentinelPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Clave Activa:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = activeKey?.key ?: "Sin clave",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentinelSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Nivel / Tier: ${activeKey?.tier ?: "Standard"}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Origen: ${activeKey?.source ?: "Firebase"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Vigencia: ${activeKey?.expiresAt ?: "Ilimitado"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = SentinelSurfaceVariantDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Vinculado a Firebase Firestore",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showKeyDetailsDialog = false }) {
                    Text("Cerrar", color = SentinelPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showKeyDetailsDialog = false
                        showLogoutConfirmDialog = true
                    }
                ) {
                    Text("Desvincular Key", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // Dialog: Add Custom Video URL
    if (showAddVideoDialog) {
        var customTitle by remember { mutableStateOf("") }
        var customUrl by remember { mutableStateOf("") }
        var customDesc by remember { mutableStateOf("") }
        var addError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddVideoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = null,
                        tint = SentinelPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Enlace de Video")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ingresa la URL directa del video (MP4, WebM o HLS stream):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("Título del Video") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it },
                        label = { Text("URL del Video (.mp4)") },
                        placeholder = { Text("https://servidor.com/video.mp4") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customDesc,
                        onValueChange = { customDesc = it },
                        label = { Text("Descripción (Opcional)") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    addError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ejemplos rápidos:",
                        fontSize = 11.sp,
                        color = SentinelSecondary
                    )
                    TextButton(
                        onClick = {
                            customTitle = "Cosmos Laundromat - First Cycle"
                            customUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
                            customDesc = "Cortometraje de animación y pruebas de color de alta fidelidad."
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Cargar demostración de prueba", fontSize = 11.sp, color = SentinelPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customTitle.isBlank() || customUrl.isBlank()) {
                            addError = "Por favor ingresa al menos título y URL."
                        } else {
                            val created = userStore.addCustomVideo(customTitle, customUrl, customDesc)
                            customVideos = userStore.getCustomVideos()
                            currentPlayingVideo = created
                            showAddVideoDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SentinelPrimary)
                ) {
                    Text("Guardar y Ver", color = SentinelOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVideoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog: Logout Confirm
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("¿Desvincular Key Sentinel?") },
            text = {
                Text("Se cerrará la sesión actual. Tendrás que ingresar nuevamente una clave válida vinculada a Firebase para reproducir videos.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        keyManager.revokeKey()
                        showLogoutConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Desvincular y Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun VideoFeedCard(
    video: VideoItem,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlayClick() }
            .testTag("video_card_${video.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) SentinelSurfaceVariantDark else SentinelSurfaceDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPlaying) 1.5.dp else 1.dp,
            color = if (isPlaying) SentinelPrimary else SentinelOutlineDark
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF0F172A))
            ) {
                if (video.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1E293B),
                                        Color(0xFF0F172A),
                                        SentinelPrimaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            tint = SentinelPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                // Play Button overlay
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(1.dp, SentinelPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                        contentDescription = "Reproducir",
                        tint = if (isPlaying) SentinelPrimary else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Duration badge (Bottom Right)
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.duration,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Resolution badge (Top Right)
                Surface(
                    color = SentinelSurfaceDark.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.resolution,
                        color = SentinelSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (video.isCustom) {
                    Surface(
                        color = SentinelTertiary.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "Personalizado",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPlaying) SentinelPrimary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = video.author,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = "•", color = SentinelOutlineDark)
                        Text(
                            text = video.views,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
