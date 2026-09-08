package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.QuicksBottomPill
import com.example.ui.navigation.QuicksTopBar
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.lockin.LockInScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.recap.RecapScreen
import com.example.ui.screens.settings.SettingsDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.QuicksNavTab
import com.example.ui.viewmodel.QuicksViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: QuicksViewModel = viewModel()
            val themeMode by viewModel.sessionManager.themeMode.collectAsState()
            val userProfile by viewModel.sessionManager.userProfile.collectAsState()

            MyApplicationTheme(themeMode = themeMode) {
                if (!userProfile.isLoggedIn) {
                    AuthScreen(
                        codeVerificationManager = viewModel.codeVerificationManager,
                        firebaseManager = viewModel.firebaseManager,
                        onLoginSuccess = { name, username, email ->
                            viewModel.sessionManager.saveProfile(name, username, email)
                        }
                    )
                } else {
                    QuicksMainContainer(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun QuicksMainContainer(viewModel: QuicksViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isLockInImmersive by viewModel.isLockInImmersive.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val userProfile by viewModel.sessionManager.userProfile.collectAsState()

    // Data from Room
    val tasks by viewModel.tasks.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val isGoogleSynced by viewModel.isGoogleCalendarSynced.collectAsState()
    val isOutlookSynced by viewModel.isOutlookCalendarSynced.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val recaps by viewModel.recaps.collectAsState()
    val isGeneratingRecap by viewModel.isGeneratingRecap.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val selectedConvoId by viewModel.selectedConversationId.collectAsState()
    val activeMessages by viewModel.activeMessages.collectAsState()

    val hideBars = currentTab == QuicksNavTab.LOCK_IN && isLockInImmersive

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (!hideBars) {
                    QuicksTopBar(
                        userName = userProfile.name,
                        onProfileClick = { viewModel.openSettings() }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = if (hideBars) androidx.compose.ui.unit.Dp.Unspecified else innerPadding.calculateTopPadding(),
                        bottom = if (hideBars) androidx.compose.ui.unit.Dp.Unspecified else innerPadding.calculateBottomPadding()
                    )
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        (slideInHorizontally(
                            animationSpec = tween(260),
                            initialOffsetX = { fullWidth -> direction * fullWidth / 4 }
                        ) + fadeIn(animationSpec = tween(240))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { fullWidth -> -direction * fullWidth / 4 }
                        ) + fadeOut(animationSpec = tween(200)))
                    },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        QuicksNavTab.HOME -> HomeScreen(
                            viewModel = viewModel,
                            tasks = tasks,
                            onNavigateTab = { viewModel.selectTab(it) }
                        )
                        QuicksNavTab.RECAP -> RecapScreen(
                            viewModel = viewModel,
                            recaps = recaps,
                            tasks = tasks,
                            notes = notes,
                            isGenerating = isGeneratingRecap
                        )
                        QuicksNavTab.CALENDAR -> CalendarScreen(
                            viewModel = viewModel,
                            events = calendarEvents,
                            isGoogleSynced = isGoogleSynced,
                            isOutlookSynced = isOutlookSynced
                        )
                        QuicksNavTab.LOCK_IN -> LockInScreen(
                            viewModel = viewModel,
                            isImmersive = isLockInImmersive
                        )
                        QuicksNavTab.NOTES -> NotesScreen(
                            viewModel = viewModel,
                            notes = notes
                        )
                        QuicksNavTab.CHAT -> ChatScreen(
                            viewModel = viewModel,
                            channels = channels,
                            selectedConvoId = selectedConvoId,
                            messages = activeMessages
                        )
                    }
                }
            }
        }

        // Floating Bottom Pill Navigation Bar
        if (!hideBars) {
            QuicksBottomPill(
                currentTab = currentTab,
                onTabSelect = { viewModel.selectTab(it) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Settings / Profile Modal
        if (isSettingsOpen) {
            SettingsDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeSettings() }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Welcome to Quicks, $name!", modifier = modifier)
}

