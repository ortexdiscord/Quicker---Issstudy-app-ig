package com.example.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LofiTrack(
    val id: Int,
    val title: String,
    val artist: String,
    val genre: String,
    val streamUrl: String
)

class LofiAudioEngine {

    val tracks: List<LofiTrack> = listOf(
        LofiTrack(
            id = 1,
            title = "Tokyo Rain Sessions",
            artist = "Lofi Cafe Stream",
            genre = "Chill Lo-Fi & Rain",
            streamUrl = "https://radio.loficafe.net/listen/chilling/radio.mp3"
        ),
        LofiTrack(
            id = 2,
            title = "Midnight Study Beats",
            artist = "Lo-Fi Radio Online",
            genre = "Warm Vinyl & Rhodes",
            streamUrl = "https://live.lofiradio.ru/lofi_mp3_128"
        ),
        LofiTrack(
            id = 3,
            title = "Chillhop Instrumental",
            artist = "I Love Chillhop",
            genre = "Smooth Study Beats",
            streamUrl = "https://streams.ilovemusic.de/iloveradio17.mp3"
        ),
        LofiTrack(
            id = 4,
            title = "Groove Salad Ambient",
            artist = "SomaFM Ambient Focus",
            genre = "Deep Ambient Study",
            streamUrl = "https://ice1.somafm.com/groovesalad-128-mp3"
        ),
        LofiTrack(
            id = 5,
            title = "Library Tape Memories",
            artist = "Zenith Collective",
            genre = "Mellow Piano Lo-Fi",
            streamUrl = "https://stream.zeno.fm/f3wvbbqmdg8uv"
        ),
        LofiTrack(
            id = 6,
            title = "Night Owl Acoustic",
            artist = "Broke For Free (CC-BY)",
            genre = "Acoustic Chill Beats",
            streamUrl = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/WFMU/Broke_For_Free/Directionless_EP/Broke_For_Free_-_01_-_Night_Owl.mp3"
        ),
        LofiTrack(
            id = 7,
            title = "Drone Zone Deep Space",
            artist = "SomaFM Atmospheric",
            genre = "Zero-Distraction Flow",
            streamUrl = "https://ice1.somafm.com/dronezone-128-mp3"
        ),
        LofiTrack(
            id = 8,
            title = "Secret Agent Lounge",
            artist = "SomaFM Downtempo",
            genre = "Vintage Spy Lo-Fi",
            streamUrl = "https://ice1.somafm.com/secretagent-128-mp3"
        ),
        LofiTrack(
            id = 9,
            title = "Lush Neo-Soul Chill",
            artist = "SomaFM Focus Wave",
            genre = "Sensuous Vocals & Beats",
            streamUrl = "https://ice1.somafm.com/lush-128-mp3"
        ),
        LofiTrack(
            id = 10,
            title = "DefCon Hacker Radio",
            artist = "SomaFM Cyber Focus",
            genre = "Chill Synth & Coding",
            streamUrl = "https://ice1.somafm.com/defcon-128-mp3"
        ),
        LofiTrack(
            id = 11,
            title = "Suburbs of Goa Chill",
            artist = "SomaFM World Downtempo",
            genre = "Meditative Rhythms",
            streamUrl = "https://ice1.somafm.com/suburbsofgoa-128-mp3"
        ),
        LofiTrack(
            id = 12,
            title = "Fluid Beats Lo-Fi",
            artist = "Future Wave Audio",
            genre = "Glitch & Chillhop",
            streamUrl = "https://ice1.somafm.com/fluid-128-mp3"
        )
    )

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering

    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun getCurrentTrack(): LofiTrack = tracks[_currentTrackIndex.value]

    fun play() {
        if (_isPlaying.value && mediaPlayer?.isPlaying == true) return
        _isPlaying.value = true
        startStream(getCurrentTrack().streamUrl)
    }

    fun pause() {
        _isPlaying.value = false
        _isBuffering.value = false
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e("LofiAudioEngine", "Error pausing player", e)
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) pause() else play()
    }

    fun nextTrack() {
        _currentTrackIndex.value = (_currentTrackIndex.value + 1) % tracks.size
        if (_isPlaying.value) {
            startStream(getCurrentTrack().streamUrl)
        }
    }

    fun previousTrack() {
        _currentTrackIndex.value = if (_currentTrackIndex.value - 1 < 0) tracks.size - 1 else _currentTrackIndex.value - 1
        if (_isPlaying.value) {
            startStream(getCurrentTrack().streamUrl)
        }
    }

    fun shuffleTrack() {
        val next = (tracks.indices).filter { it != _currentTrackIndex.value }.randomOrNull() ?: 0
        _currentTrackIndex.value = next
        if (_isPlaying.value) {
            startStream(getCurrentTrack().streamUrl)
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (e: Exception) {
            Log.e("LofiAudioEngine", "Error setting volume", e)
        }
    }

    private fun startStream(url: String) {
        scope.launch {
            try {
                _isBuffering.value = true
                mediaPlayer?.release()
                mediaPlayer = null

                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(url)
                    val vol = _volume.value
                    setVolume(vol, vol)
                    setOnPreparedListener { mp ->
                        _isBuffering.value = false
                        if (_isPlaying.value) {
                            mp.start()
                        }
                    }
                    setOnCompletionListener {
                        nextTrack()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.w("LofiAudioEngine", "MediaPlayer error: $what, $extra")
                        _isBuffering.value = false
                        nextTrack()
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = player
            } catch (e: Exception) {
                Log.e("LofiAudioEngine", "Failed to start stream", e)
                _isBuffering.value = false
            }
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
            _isBuffering.value = false
        } catch (e: Exception) {
            Log.e("LofiAudioEngine", "Error releasing MediaPlayer", e)
        }
    }
}
