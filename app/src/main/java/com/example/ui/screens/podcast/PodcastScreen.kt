package com.example.ui.screens.podcast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.podcast.PodcastManager
import com.example.podcast.PodcastSpeaker
import com.example.podcast.PodcastTurn
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.rememberLiquidAuroraBrush
import com.example.ui.viewmodel.QuicksViewModel

@Composable
fun PodcastStudioScreen(
    viewModel: QuicksViewModel,
    podcastManager: PodcastManager
) {
    val episode by podcastManager.currentEpisode.collectAsState()
    val isPlaying by podcastManager.isPlaying.collectAsState()
    val currentTurnIndex by podcastManager.currentTurnIndex.collectAsState()
    val currentSpeaker by podcastManager.currentSpeaker.collectAsState()
    val speed by podcastManager.playbackSpeed.collectAsState()
    val isGenerating by podcastManager.isGeneratingScript.collectAsState()

    val notes by viewModel.notes.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var customTopicInput by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Auto-scroll transcript to active turn
    LaunchedEffect(currentTurnIndex) {
        if (currentTurnIndex >= 0) {
            listState.animateScrollToItem(currentTurnIndex.coerceAtLeast(0))
        }
    }

    val auroraBrush = rememberLiquidAuroraBrush()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .background(auroraBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Study Cast Studio",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "2-Host Audio",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Natural 2-person dialogue • Free native synthesis engine",
                        fontSize = 11.5.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }

                // New Episode Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x22FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40FFFFFF)),
                    modifier = Modifier.clickable { showCreateDialog = !showCreateDialog }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New Episode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Create New Episode Accordion / Panel
            AnimatedVisibility(
                visible = showCreateDialog,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    backgroundColor = Color(0xFF0F0F16),
                    borderTopColor = Color(0x80FFFFFF)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Generate 2-Person Podcast",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customTopicInput,
                            onValueChange = { customTopicInput = it },
                            placeholder = { Text("Enter topic (e.g. Quantum Physics, Memory)", fontSize = 12.sp, color = Color(0xFF71717A)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Presets
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val presets = listOf("Quantum Physics", "Neuroplasticity", "Calculus Limits", "Algorithms Big-O")
                            items(presets) { preset ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x18FFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x28FFFFFF)),
                                    modifier = Modifier.clickable {
                                        customTopicInput = preset
                                        podcastManager.createEpisodeFromTopic(preset)
                                        showCreateDialog = false
                                    }
                                ) {
                                    Text(
                                        text = preset,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFE4E4E7),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (notes.isNotEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6)),
                                        modifier = Modifier.clickable {
                                            val firstNote = notes.first()
                                            podcastManager.createEpisodeFromTopic(firstNote.title, firstNote.content)
                                            showCreateDialog = false
                                        }
                                    ) {
                                        Text(
                                            text = "🎙️ From Latest Note",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC4B5FD),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                modifier = Modifier.clickable {
                                    if (customTopicInput.isNotBlank()) {
                                        podcastManager.createEpisodeFromTopic(customTopicInput)
                                        showCreateDialog = false
                                    }
                                }
                            ) {
                                Text(
                                    text = "Start Dialogue",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Central Podcast Player Widget (Liquid Glass)
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color(0xFF0C0C12),
                borderTopColor = Color(0x99FFFFFF),
                borderBottomColor = Color(0x20FFFFFF)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = episode?.title ?: "Select or create an episode",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = episode?.topic ?: "Study Cast",
                        fontSize = 11.5.sp,
                        color = Color(0xFFA1A1AA),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Dual Host Avatars with Talking Aura Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HostAvatarPill(
                            speaker = PodcastSpeaker.LEO,
                            isSpeaking = isPlaying && currentSpeaker == PodcastSpeaker.LEO
                        )
                        PodcastWaveformVisualizer(isPlaying = isPlaying)
                        HostAvatarPill(
                            speaker = PodcastSpeaker.MAYA,
                            isSpeaking = isPlaying && currentSpeaker == PodcastSpeaker.MAYA
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Playback Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { podcastManager.skipBackward() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.FastRewind,
                                contentDescription = "Previous Turn",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Big Play / Pause Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .size(54.dp)
                                .clickable { podcastManager.togglePlayPause() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.Black,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = { podcastManager.skipForward() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.FastForward,
                                contentDescription = "Next Turn",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Speed Multiplier Chips
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color(0xFF71717A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val speeds = listOf(0.8f, 1.0f, 1.25f, 1.5f)
                        speeds.forEach { s ->
                            val isSelected = speed == s
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color.White else Color(0x18FFFFFF),
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .clickable { podcastManager.setSpeed(s) }
                            ) {
                                Text(
                                    text = "${s}x",
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else Color(0xFFA1A1AA),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Live Dialogue Transcript Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Live Dialogue Transcript",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${episode?.turns?.size ?: 0} turns",
                    fontSize = 11.sp,
                    color = Color(0xFF71717A)
                )
            }

            // Transcript Scrollable List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                val turns = episode?.turns ?: emptyList()
                itemsIndexed(turns) { index, turn ->
                    val isActive = index == currentTurnIndex && isPlaying
                    TranscriptTurnCard(
                        turn = turn,
                        isActive = isActive,
                        onClick = { podcastManager.playTurn(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun HostAvatarPill(
    speaker: PodcastSpeaker,
    isSpeaking: Boolean
) {
    val auraColor by animateColorAsState(
        targetValue = if (isSpeaking) {
            if (speaker == PodcastSpeaker.LEO) Color(0xFF38BDF8) else Color(0xFFC084FC)
        } else Color.Transparent,
        animationSpec = tween(300),
        label = "aura"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF14141E))
                .border(2.dp, if (isSpeaking) auraColor else Color(0x33FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = speaker.avatarEmoji,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = speaker.displayName,
            fontSize = 11.sp,
            fontWeight = if (isSpeaking) FontWeight.Bold else FontWeight.Medium,
            color = if (isSpeaking) Color.White else Color(0xFFA1A1AA)
        )
        Text(
            text = if (isSpeaking) "Speaking..." else "Ready",
            fontSize = 9.sp,
            color = if (isSpeaking) auraColor else Color(0xFF71717A)
        )
    }
}

@Composable
fun PodcastWaveformVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        val barCounts = 7
        for (i in 0 until barCounts) {
            val animHeight by infiniteTransition.animateFloat(
                initialValue = 6f,
                targetValue = if (isPlaying) (12f + (i % 4) * 8f) else 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(350 + i * 80, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(animHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFF6366F1).copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun TranscriptTurnCard(
    turn: PodcastTurn,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val isLeo = turn.speaker == PodcastSpeaker.LEO
    val accentColor = if (isLeo) Color(0xFF38BDF8) else Color(0xFFC084FC)

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (isActive) Color(0xFF161626) else Color(0xFF0D0D14),
        backgroundAlpha = if (isActive) 0.94f else 0.82f,
        borderTopColor = if (isActive) accentColor.copy(alpha = 0.85f) else Color(0x35FFFFFF),
        borderBottomColor = if (isActive) accentColor.copy(alpha = 0.35f) else Color(0x10FFFFFF),
        borderWidth = if (isActive) 1.5.dp else 1.dp,
        showShimmer = isActive,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = turn.speaker.avatarEmoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = turn.speaker.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    if (turn.emotionNote.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${turn.emotionNote}",
                            fontSize = 10.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
                if (isActive) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = "Playing",
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "NOW PLAYING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = turn.text,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                color = if (isActive) Color.White else Color(0xFFD4D4D8)
            )
        }
    }
}
