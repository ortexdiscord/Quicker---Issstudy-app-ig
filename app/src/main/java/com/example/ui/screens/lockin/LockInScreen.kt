package com.example.ui.screens.lockin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speech.VoiceAction
import com.example.ui.theme.MonoOledDark
import com.example.ui.theme.MonoWhite
import com.example.ui.viewmodel.QuicksViewModel
import java.util.Locale

@Composable
fun LockInScreen(
    viewModel: QuicksViewModel,
    isImmersive: Boolean
) {
    val context = LocalContext.current
    val totalSeconds by viewModel.lockInTotalSeconds.collectAsState()
    val remainingSeconds by viewModel.lockInRemainingSeconds.collectAsState()
    val isRunning by viewModel.isLockInRunning.collectAsState()

    val audioEngine = viewModel.audioEngine
    val isMusicPlaying by audioEngine.isPlaying.collectAsState()
    val isBuffering by audioEngine.isBuffering.collectAsState()
    val currentTrackIndex by audioEngine.currentTrackIndex.collectAsState()
    val volume by audioEngine.volume.collectAsState()
    val currentTrack = audioEngine.getCurrentTrack()

    val isListening by viewModel.voiceHelper.isListening.collectAsState()
    val lastHeardText by viewModel.voiceHelper.lastHeardText.collectAsState()

    var voiceFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f

    // Pulse animation when focus session is running
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_focus")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090C))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar of Lock In
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) Color(0xFF10B981) else Color(0xFF71717A))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "LOCK IN ACTIVE" else "FOCUS STANDBY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                if (isImmersive) {
                    IconButton(
                        onClick = { viewModel.exitLockInImmersive() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F1F24))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Immersive", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Timer Display
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1C1C22),
                    strokeWidth = 10.dp,
                )
                // Active Progress
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    strokeWidth = 10.dp,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )

                    Text(
                        text = if (isRunning) "Deep Focus" else "Paused",
                        fontSize = 13.sp,
                        color = Color(0xFFA1A1AA),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timer Primary Controls (Play/Pause, Reset, +5m)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setLockInDuration(25) },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF18181F))
                ) {
                    Icon(Icons.Default.Replay, contentDescription = "Reset 25m", tint = Color.White)
                }

                Button(
                    onClick = { viewModel.toggleLockInTimer() },
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 12.dp)
                        .testTag("lock_in_toggle_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Pause Lock In" else "Lock In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.addLockInMinutes(5) },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF18181F))
                ) {
                    Text("+5m", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            }

            // Duration Presets Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(15, 25, 45, 60).forEach { mins ->
                    val isSel = (totalSeconds / 60) == mins
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) Color.White else Color(0xFF18181E))
                            .clickable {
                                viewModel.setLockInDuration(mins)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${mins}m",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.Black else Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            // --- Lofi Music Player (Shuffles 10 Tracks) ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF27272F), RoundedCornerShape(18.dp)),
                color = Color(0xFF121217),
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1E28)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isMusicPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                    contentDescription = "Lofi",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${currentTrack.id}/${audioEngine.tracks.size} • ${currentTrack.title}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isBuffering) "Connecting live stream..." else "${currentTrack.artist} • ${currentTrack.genre}",
                                    fontSize = 11.sp,
                                    color = if (isBuffering) Color(0xFF60A5FA) else Color(0xFFA1A1AA)
                                )
                            }
                        }

                        // Shuffle Button
                        IconButton(
                            onClick = { audioEngine.shuffleTrack() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { audioEngine.previousTrack() }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
                        }

                        Button(
                            onClick = { audioEngine.togglePlayPause() },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMusicPlaying) Color.White else Color(0xFF282834),
                                contentColor = if (isMusicPlaying) Color.Black else Color.White
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle Music",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(onClick = { audioEngine.nextTrack() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                        }
                    }

                    // Volume Slider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Volume", tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = volume,
                            onValueChange = { audioEngine.setVolume(it) },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color(0xFF272732)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- Voice Commands Section ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF27272F), RoundedCornerShape(16.dp)),
                color = Color(0xFF101015)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Commands", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Voice Commands",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.voiceHelper.startListening(
                                    onResult = { action ->
                                        viewModel.handleVoiceAction(action)
                                        voiceFeedbackMessage = "Command executed: $action"
                                    },
                                    onError = { err ->
                                        voiceFeedbackMessage = err
                                    }
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) Color(0xFFEF4444) else Color(0xFF252530),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (isListening) "Listening..." else "Speak Command",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (lastHeardText.isNotBlank()) {
                        Text(
                            text = "Heard: \"$lastHeardText\"",
                            fontSize = 11.sp,
                            color = Color(0xFFA1A1AA),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (voiceFeedbackMessage != null) {
                        Text(
                            text = voiceFeedbackMessage!!,
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Voice Command Shortcut Chips
                    Text(
                        text = "Or tap a quick command shortcut:",
                        fontSize = 11.sp,
                        color = Color(0xFFA1A1AA),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Pause" to VoiceAction.PauseSession,
                            "Resume" to VoiceAction.ResumeSession,
                            "Stop Music" to VoiceAction.StopMusic,
                            "+5 min" to VoiceAction.AddMinutes(5),
                            "45 min" to VoiceAction.SetTimer(45)
                        ).forEach { (label, action) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1C1C24))
                                    .clickable {
                                        viewModel.handleVoiceAction(action)
                                        voiceFeedbackMessage = "Executed: $label"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
