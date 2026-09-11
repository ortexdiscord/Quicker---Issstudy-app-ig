package com.example.ui.screens.chat

import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.data.api.QuickerAiMode
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.components.QuicksRichText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ChatChannel
import com.example.data.local.ChatMessage
import com.example.ui.viewmodel.QuicksViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: QuicksViewModel,
    channels: List<ChatChannel>,
    selectedConvoId: String,
    messages: List<ChatMessage>
) {
    val activeChatConvoId by viewModel.activeChatConvoId.collectAsState()

    AnimatedContent(
        targetState = activeChatConvoId,
        transitionSpec = {
            if (targetState != null) {
                // Transitioning into Conversation Detail (slide in from right)
                (slideInHorizontally(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                    initialOffsetX = { it }
                ) + fadeIn(animationSpec = tween(250))) togetherWith
                (slideOutHorizontally(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                    targetOffsetX = { -it / 3 }
                ) + fadeOut(animationSpec = tween(200)))
            } else {
                // Transitioning back to WhatsApp Chats List (slide in from left)
                (slideInHorizontally(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                    initialOffsetX = { -it / 3 }
                ) + fadeIn(animationSpec = tween(250))) togetherWith
                (slideOutHorizontally(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                    targetOffsetX = { it }
                ) + fadeOut(animationSpec = tween(200)))
            }
        },
        label = "chat_screen_nav"
    ) { convoId ->
        if (convoId == null) {
            WhatsAppChatsListView(
                viewModel = viewModel,
                channels = channels,
                onSelectChannel = { ch ->
                    viewModel.openConversation(ch.id)
                }
            )
        } else {
            val activeChannel = channels.find { it.id == convoId }
                ?: ChatChannel(convoId, null, "Chat", "Chat", true, "default")

            ConversationDetailView(
                viewModel = viewModel,
                activeChannel = activeChannel,
                channels = channels,
                messages = messages,
                onBack = { viewModel.closeConversation() }
            )
        }
    }
}

// -------------------------------------------------------------
// 1. WhatsApp Chats List View (Matches the reference screenshot!)
// -------------------------------------------------------------
@Composable
fun WhatsAppChatsListView(
    viewModel: QuicksViewModel,
    channels: List<ChatChannel>,
    onSelectChannel: (ChatChannel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Unread", "Groups", "AI"
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showAiMemoryDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val quickerAiChannels = channels.filter { it.serverName == "Quicker AI" || it.id.startsWith("quicker_ai") || it.id == "quicker_ai" }

    val filteredChannels = channels.filter { channel ->
        val matchesSearch = channel.displayName.contains(searchQuery, ignoreCase = true) ||
                channel.lastMessage.contains(searchQuery, ignoreCase = true)
        val isAiChannel = channel.serverName == "Quicker AI" || channel.id == "quicker_ai" || channel.id.startsWith("quicker_ai")
        val matchesFilter = when (selectedFilter) {
            "Unread" -> channel.unreadCount > 0
            "Groups" -> !channel.isDirectMessage && !isAiChannel
            "AI" -> isAiChannel
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // WhatsApp Header Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quicks Chat",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isSearchActive = !isSearchActive },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { showNewChatDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Camera",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("New Study Group") },
                                        onClick = {
                                            showMenu = false
                                            showNewChatDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("New Chat") },
                                        onClick = {
                                            showMenu = false
                                            showNewChatDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Open Quicker AI") },
                                        onClick = {
                                            showMenu = false
                                            viewModel.openConversation("quicker_ai")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Search input field if toggled
                    AnimatedVisibility(visible = isSearchActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search chats or messages...", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            )
                        }
                    }

                    // Filter Chips: All, Unread, Groups, AI
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filterOptions = listOf("All", "Unread", "Groups", "AI")
                        items(filterOptions) { filter ->
                            val isSelected = selectedFilter == filter
                            val bgCol by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                label = "filter_bg"
                            )
                            val textCol by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                label = "filter_text"
                            )

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = bgCol,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedFilter = filter }
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textCol,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // WhatsApp Conversation Rows
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 76.dp)
            ) {
                // Quicker AI Sessions Hub Header
                if (searchQuery.isBlank() && (selectedFilter == "All" || selectedFilter == "AI")) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Quicker AI Sessions (${quickerAiChannels.size})",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "🧠 Memory",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B5CF6),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { showAiMemoryDialog = true }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = "+ New AI Chat",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { showNewChatDialog = true }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Horizontal list of Quicker AI sessions
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Action Card: + New Session
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                            .clickable { showNewChatDialog = true },
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(8.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("New AI Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

                                // Each Quicker AI Chat Channel
                                items(quickerAiChannels, key = { it.id }) { aiChannel ->
                                    Surface(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .clickable { onSelectChannel(aiChannel) },
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(8.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF8B5CF6)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = aiChannel.displayName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = aiChannel.lastMessage.take(40),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 13.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                    }
                }

                items(filteredChannels, key = { it.id }) { channel ->
                    WhatsAppChatListItem(
                        channel = channel,
                        onClick = { onSelectChannel(channel) },
                        onDelete = { viewModel.deleteConversation(channel.id) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }

                // Friendly card to add peer contacts if user only has Quicker AI
                val hasOtherPeople = channels.any { it.id != "quicker_ai" }
                if (!hasOtherPeople && searchQuery.isBlank() && selectedFilter == "All") {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color(0xFF25D366)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Connect with Study Buddies",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add your classmates or create a study group to share notes, flashcards, and collaborate.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showNewChatDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Person / Group", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                if (filteredChannels.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No conversations found",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // WhatsApp Floating Action Button (+) with green/primary accent
        FloatingActionButton(
            onClick = { showNewChatDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 86.dp)
                .testTag("new_chat_fab"),
            containerColor = Color(0xFF25D366), // WhatsApp Green Accent
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Chat", modifier = Modifier.size(24.dp))
        }
    }

    // New Chat / Group / Quicker AI Creation Dialog
    if (showNewChatDialog) {
        var chatMode by remember { mutableStateOf("AI") } // "AI", "DIRECT", "GROUP"
        var chatName by remember { mutableStateOf("") }
        var starterMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = {
                Text(
                    when (chatMode) {
                        "AI" -> "New Quicker AI Study Session"
                        "GROUP" -> "Create Study Group"
                        else -> "Add Person to Chat"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Segment 1: Quicker AI
                        Button(
                            onClick = { chatMode = "AI" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (chatMode == "AI") Color(0xFF8B5CF6) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (chatMode == "AI") Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Quicker AI", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        // Segment 2: Direct Contact
                        Button(
                            onClick = { chatMode = "DIRECT" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (chatMode == "DIRECT") Color(0xFF25D366) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (chatMode == "DIRECT") Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Direct", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        // Segment 3: Study Group
                        Button(
                            onClick = { chatMode = "GROUP" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (chatMode == "GROUP") Color(0xFF3B82F6) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (chatMode == "GROUP") Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Group", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (chatMode == "AI") {
                        Text(
                            text = "Launch a dedicated study chat with Quicker AI for a subject or exam. AI context and memory are preserved.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = chatName,
                        onValueChange = { chatName = it },
                        label = {
                            Text(
                                when (chatMode) {
                                    "AI" -> "Study Topic / Subject (e.g. Physics, Calculus)"
                                    "GROUP" -> "Group Name"
                                    else -> "Contact / Friend Name"
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                when (chatMode) {
                                    "AI" -> "e.g. Organic Chemistry Final Prep"
                                    "GROUP" -> "e.g. AP Chemistry Cohort"
                                    else -> "e.g. Jordan Miller"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = starterMessage,
                        onValueChange = { starterMessage = it },
                        label = { Text(if (chatMode == "AI") "Opening Question / Prompt (Optional)" else "Initial Message (Optional)") },
                        placeholder = {
                            Text(
                                when (chatMode) {
                                    "AI" -> "e.g. Can you explain the main concepts and quiz me?"
                                    "GROUP" -> "Welcome everyone to the study group!"
                                    else -> "Hey! Let's lock in and study."
                                }
                            )
                        },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = chatName.trim().ifBlank { if (chatMode == "AI") "Study Session" else "New Chat" }
                        when (chatMode) {
                            "AI" -> {
                                viewModel.createQuickerAiChat(
                                    topic = trimmedName,
                                    initialPrompt = starterMessage.trim().takeIf { it.isNotBlank() }
                                )
                            }
                            "GROUP" -> {
                                viewModel.createNewChat(
                                    name = trimmedName,
                                    isDirect = false,
                                    serverName = "Study Squad",
                                    initialMessage = starterMessage.trim().takeIf { it.isNotBlank() }
                                )
                            }
                            else -> {
                                viewModel.createNewChat(
                                    name = trimmedName,
                                    isDirect = true,
                                    serverName = null,
                                    initialMessage = starterMessage.trim().takeIf { it.isNotBlank() }
                                )
                            }
                        }
                        showNewChatDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (chatMode) {
                            "AI" -> Color(0xFF8B5CF6)
                            "GROUP" -> Color(0xFF3B82F6)
                            else -> Color(0xFF25D366)
                        }
                    )
                ) {
                    Text(if (chatMode == "AI") "Launch AI Chat" else "Start Chat", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAiMemoryDialog) {
        AiMemoryDialog(
            viewModel = viewModel,
            onDismiss = { showAiMemoryDialog = false }
        )
    }
}

// -------------------------------------------------------------
// WhatsApp Chat List Row Item (Exact structure as screenshot)
// -------------------------------------------------------------
@Composable
fun WhatsAppChatListItem(
    channel: ChatChannel,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("M/d/yy", Locale.getDefault()) }

    val formattedTime = remember(channel.lastMessageTime) {
        val now = System.currentTimeMillis()
        val diff = now - channel.lastMessageTime
        if (diff < 24 * 3600 * 1000) {
            dateFormat.format(Date(channel.lastMessageTime))
        } else {
            dayFormat.format(Date(channel.lastMessageTime))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Contact / Group Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    if (channel.id == "quicker_ai") MaterialTheme.colorScheme.primary
                    else if (!channel.isDirectMessage) Color(0xFF3B82F6)
                    else when (channel.displayName.hashCode() % 4) {
                        0 -> Color(0xFF10B981)
                        1 -> Color(0xFF8B5CF6)
                        2 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEC4899)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (channel.id == "quicker_ai") {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else if (!channel.isDirectMessage) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Text(
                    text = channel.displayName.trim().take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Name and Last Message Snippet
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (channel.callType == "voice") {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Voice Call",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else if (channel.callType == "video") {
                    Icon(
                        Icons.Default.Videocam,
                        contentDescription = "Video Call",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = if (channel.lastMessage.isNotBlank()) channel.lastMessage else "Tap to chat",
                    fontSize = 13.5.sp,
                    color = if (channel.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (channel.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Time and Unread Badge
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formattedTime,
                fontSize = 11.5.sp,
                color = if (channel.unreadCount > 0) Color(0xFF25D366) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (channel.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (channel.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.unreadCount.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Delete action for peer/custom channels
        if (channel.id != "quicker_ai" && onDelete != null) {
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Chat",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Conversation?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove \"${channel.displayName}\"? This conversation will be removed from your chat list.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 2. Conversation Detail View ("click on messages and type")
// -------------------------------------------------------------
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ConversationDetailView(
    viewModel: QuicksViewModel,
    activeChannel: ChatChannel,
    channels: List<ChatChannel>,
    messages: List<ChatMessage>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val currentAiMode by viewModel.quickerAiMode.collectAsState()
    val isVoiceListening by viewModel.voiceHelper.isListening.collectAsState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val isQuickerAi = activeChannel.serverName == "Quicker AI" || activeChannel.id == "quicker_ai" || activeChannel.id.startsWith("quicker_ai")
    var showAiMemoryDialog by remember { mutableStateOf(false) }
    var showSwitchAiDialog by remember { mutableStateOf(false) }
    var showAiMoreMenu by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var speechNotice by remember { mutableStateOf<String?>(null) }

    val quickerAiSessions = channels.filter { it.serverName == "Quicker AI" || it.id == "quicker_ai" || it.id.startsWith("quicker_ai") }

    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var isHintModeActive by remember { mutableStateOf(false) }

    // Message options modal state
    var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }
    var showForwardDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri?.toString() }
    )

    // Document / PDF Picker (Stores locally and extracts source text for AI)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.sendSourceAttachment(activeChannel.id, uri, inputText)
                inputText = ""
            }
        }
    )

    // Audio recording permission launcher
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                speechNotice = "Listening... Speak your question now"
                viewModel.voiceHelper.startSpeechToText(
                    onText = { text ->
                        speechNotice = null
                        inputText = if (inputText.isBlank()) text else "$inputText $text"
                    },
                    onError = { err ->
                        speechNotice = err
                    }
                )
            } else {
                speechNotice = "Microphone permission is required for voice input"
            }
        }
    )

    val imeVisible = WindowInsets.isImeVisible

    // Keyboard Auto-Adjustment & Auto-scroll when messages arrive or keyboard opens
    LaunchedEffect(messages.size, imeVisible) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
        if (imeVisible) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // WhatsApp Style Top App Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Takes you back to WhatsApp Chats List!)
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Chats",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isQuickerAi) Color(0xFF8B5CF6)
                            else if (!activeChannel.isDirectMessage) Color(0xFF3B82F6)
                            else Color(0xFF10B981)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isQuickerAi) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    } else if (!activeChannel.isDirectMessage) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = activeChannel.displayName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Contact Name & Status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeChannel.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val remainingQuota = viewModel.aiService.getRemainingFreeQuota()
                    val hasKey = viewModel.aiService.hasCustomKey()
                    val statusText = if (isQuickerAi) {
                        if (hasKey) "Quicker AI • Context & Memory Active" else "Quicker AI • $remainingQuota/5 free daily queries"
                    } else if (activeChannel.isDirectMessage) "Online • Study Buddy"
                    else "Study Group • ${messages.size} messages"

                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = if (isQuickerAi) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isQuickerAi) {
                    // Memory Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAiMemoryDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🧠", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Memory", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // More Options Dropdown
                    Box {
                        IconButton(onClick = { showAiMoreMenu = true }, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "AI Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        DropdownMenu(
                            expanded = showAiMoreMenu,
                            onDismissRequest = { showAiMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Switch AI Session (${quickerAiSessions.size})") },
                                onClick = {
                                    showAiMoreMenu = false
                                    showSwitchAiDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("+ New Quicker AI Session") },
                                onClick = {
                                    showAiMoreMenu = false
                                    viewModel.createQuickerAiChat("Practice Session")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🧠 Manage AI Memory") },
                                onClick = {
                                    showAiMoreMenu = false
                                    showAiMemoryDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🎯 Practice Quiz") },
                                onClick = {
                                    showAiMoreMenu = false
                                    viewModel.requestAiQuiz("Current Topic", activeChannel.id)
                                }
                            )
                        }
                    }
                } else {
                    // Call icons for user chats
                    IconButton(onClick = {}, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = {}, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Quicker AI Mode Switcher (Smart vs Expert)
        if (isQuickerAi) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF101018).copy(alpha = 0.85f))
                            .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isSmart = currentAiMode == QuickerAiMode.SMART
                        val smartBg by animateColorAsState(
                            targetValue = if (isSmart) MaterialTheme.colorScheme.primary else Color.Transparent,
                            label = "smart_bg"
                        )
                        val smartTextColor by animateColorAsState(
                            targetValue = if (isSmart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "smart_txt"
                        )

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { viewModel.setQuickerAiMode(QuickerAiMode.SMART) },
                            color = smartBg,
                            shape = RoundedCornerShape(9.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚡", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Smart",
                                    fontWeight = if (isSmart) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = smartTextColor
                                )
                            }
                        }

                        val isExpert = currentAiMode == QuickerAiMode.EXPERT
                        val expertBg by animateColorAsState(
                            targetValue = if (isExpert) Color(0xFF8B5CF6) else Color.Transparent,
                            label = "expert_bg"
                        )
                        val expertTextColor by animateColorAsState(
                            targetValue = if (isExpert) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "expert_txt"
                        )

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { viewModel.setQuickerAiMode(QuickerAiMode.EXPERT) },
                            color = expertBg,
                            shape = RoundedCornerShape(9.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎓", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Expert",
                                    fontWeight = if (isExpert) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = expertTextColor
                                )
                            }
                        }

                        val isMaster = currentAiMode == QuickerAiMode.MASTER
                        val masterBg by animateColorAsState(
                            targetValue = if (isMaster) Color(0xFFF59E0B) else Color.Transparent,
                            label = "master_bg"
                        )
                        val masterTextColor by animateColorAsState(
                            targetValue = if (isMaster) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "master_txt"
                        )

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { viewModel.setQuickerAiMode(QuickerAiMode.MASTER) },
                            color = masterBg,
                            shape = RoundedCornerShape(9.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👑", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Master",
                                    fontWeight = if (isMaster) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = masterTextColor
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when (currentAiMode) {
                                QuickerAiMode.SMART -> "⚡ Smart: Quick answers & intuition"
                                QuickerAiMode.EXPERT -> "🎓 Expert: Formulas & hard processes"
                                QuickerAiMode.MASTER -> "👑 Master: Hardest of the toughest tasks"
                            },
                            fontSize = 11.sp,
                            color = when (currentAiMode) {
                                QuickerAiMode.SMART -> MaterialTheme.colorScheme.primary
                                QuickerAiMode.EXPERT -> Color(0xFF8B5CF6)
                                QuickerAiMode.MASTER -> Color(0xFFF59E0B)
                            },
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Encrypted",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Encrypted • Protected",
                                fontSize = 10.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // WhatsApp Messages List ("where you can click on messages")
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(messages, key = { it.id }) { msg ->
                    WhatsAppMessageBubble(
                        message = msg,
                        onClick = {
                            selectedMessageForOptions = msg
                        },
                        onAnswerQuiz = { optionIndex ->
                            viewModel.answerQuiz(msg, optionIndex)
                        }
                    )
                }

                if (isAiLoading && isQuickerAi) {
                    item {
                        TypingIndicatorBubble()
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }

        // Replying Preview Banner
        AnimatedVisibility(visible = replyingToMessage != null) {
            if (replyingToMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Replying to ${replyingToMessage!!.senderName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = replyingToMessage!!.text.take(60),
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { replyingToMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Selected Image Preview
        if (selectedImageUri != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = Uri.parse(selectedImageUri),
                        contentDescription = "Selected Local Image",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Local photo attached",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedImageUri = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove Image", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Quicker AI Action Tools (when chatting with any Quicker AI session)
        if (isQuickerAi) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                            .clickable { showAiMemoryDialog = true }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🧠", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Memory", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showSwitchAiDialog = true }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF8B5CF6))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch Chat (${quickerAiSessions.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                item {
                    // Hint Mode Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isHintModeActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { isHintModeActive = !isHintModeActive }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isHintModeActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHintModeActive) "Hint Mode: ON" else "Hints",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHintModeActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.requestAiQuiz("Physics Mechanics", activeChannel.id) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🎯 Quiz Me", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    viewModel.requestAiSummary(inputText, activeChannel.id)
                                    inputText = ""
                                } else {
                                    viewModel.requestAiSummary("Wave Particle Duality", activeChannel.id)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("📝 Summarize", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                val textToTranslate = if (inputText.isNotBlank()) inputText else "Conservation of Energy"
                                viewModel.requestAiTranslation(textToTranslate, "Spanish", activeChannel.id)
                                inputText = ""
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🌐 Translate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Listening or Speech Status Banner
        if (isVoiceListening || speechNotice != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isVoiceListening) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isVoiceListening) Icons.Default.Mic else Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if (isVoiceListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = speechNotice ?: "Listening to your voice... Speak your question",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isVoiceListening) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (isVoiceListening) {
                        IconButton(
                            onClick = {
                                viewModel.voiceHelper.stopListening()
                                speechNotice = null
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop Listening", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(
                            onClick = { speechNotice = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // WhatsApp Style Message Input Bar with Keyboard Auto-Adjustment
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachment Button with Dropdown (PDF / Files Source + Device Photos)
                Box {
                    IconButton(
                        onClick = { showAttachmentMenu = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Attach Document or Photo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showAttachmentMenu,
                        onDismissRequest = { showAttachmentMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Attach PDF / Doc (AI Source)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Extracts content as knowledge source", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            onClick = {
                                showAttachmentMenu = false
                                filePickerLauncher.launch(arrayOf("application/pdf", "text/plain", "text/*", "*/*"))
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Attach Image (Device Only)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Stored on device, not cloud", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            onClick = {
                                showAttachmentMenu = false
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Text Input Field with Auto-Focus and Keyboard Alignment
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (activeChannel.id == "quicker_ai") "Ask Quicker AI (${currentAiMode.name.lowercase()})..." else "Encrypted message...",
                            fontSize = 13.5.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .testTag("chat_message_input"),
                    maxLines = 4,
                    shape = RoundedCornerShape(22.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() || selectedImageUri != null) {
                                viewModel.sendChatMessage(
                                    convoId = activeChannel.id,
                                    text = inputText,
                                    imageUri = selectedImageUri,
                                    replyToText = replyingToMessage?.text,
                                    isHintRequested = isHintModeActive
                                )
                                inputText = ""
                                selectedImageUri = null
                                replyingToMessage = null
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Real Microphone Speech-to-Text Button
                val micBgColor by animateColorAsState(
                    targetValue = if (isVoiceListening) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant,
                    label = "mic_bg"
                )
                IconButton(
                    onClick = {
                        val hasMicPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasMicPermission) {
                            if (isVoiceListening) {
                                viewModel.voiceHelper.stopListening()
                                speechNotice = null
                            } else {
                                speechNotice = "Listening... Speak now"
                                viewModel.voiceHelper.startSpeechToText(
                                    onText = { text ->
                                        speechNotice = null
                                        inputText = if (inputText.isBlank()) text else "$inputText $text"
                                    },
                                    onError = { err ->
                                        speechNotice = err
                                    }
                                )
                            }
                        } else {
                            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(micBgColor)
                ) {
                    Icon(
                        imageVector = if (isVoiceListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isVoiceListening) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Send Button with animated morphing
                val isTyping = inputText.isNotBlank() || selectedImageUri != null
                val sendButtonBg by animateColorAsState(
                    targetValue = if (isTyping) Color(0xFF25D366) else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    label = "send_bg"
                )

                IconButton(
                    onClick = {
                        if (isTyping) {
                            viewModel.sendChatMessage(
                                convoId = activeChannel.id,
                                text = inputText,
                                imageUri = selectedImageUri,
                                replyToText = replyingToMessage?.text,
                                isHintRequested = isHintModeActive
                            )
                            inputText = ""
                            selectedImageUri = null
                            replyingToMessage = null
                        } else {
                            // Prompt voice input directly if empty
                            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(sendButtonBg)
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }

    // Message Click Popup Dialog (Reply, Copy, Forward, Save to Notes)
    if (selectedMessageForOptions != null) {
        MessageOptionsDialog(
            message = selectedMessageForOptions!!,
            onDismiss = { selectedMessageForOptions = null },
            onReply = {
                replyingToMessage = selectedMessageForOptions
                selectedMessageForOptions = null
            },
            onCopy = {
                viewModel.copyToClipboard(context, selectedMessageForOptions!!.text)
                selectedMessageForOptions = null
            },
            onForward = {
                showForwardDialog = true
            },
            onSaveToNotes = {
                viewModel.addNote(
                    title = "Saved Note from ${selectedMessageForOptions!!.senderName}",
                    content = selectedMessageForOptions!!.text,
                    subject = "Study Chat",
                    preset = "Lecture",
                    imagePath1 = selectedMessageForOptions!!.imageUri
                )
                selectedMessageForOptions = null
            }
        )
    }

    if (showForwardDialog && selectedMessageForOptions != null) {
        ForwardMessageDialog(
            message = selectedMessageForOptions!!,
            channels = channels,
            onDismiss = {
                showForwardDialog = false
                selectedMessageForOptions = null
            },
            onForwardTo = { targetId ->
                viewModel.sendChatMessage(
                    convoId = targetId,
                    text = "Forwarded message: ${selectedMessageForOptions!!.text}",
                    imageUri = selectedMessageForOptions!!.imageUri
                )
                showForwardDialog = false
                selectedMessageForOptions = null
            }
        )
    }

    if (showAiMemoryDialog) {
        AiMemoryDialog(
            viewModel = viewModel,
            onDismiss = { showAiMemoryDialog = false }
        )
    }

    if (showSwitchAiDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchAiDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quicker AI Sessions", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Switch between your study sessions. AI context & memory stay synchronized across sessions.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(quickerAiSessions, key = { it.id }) { aiChan ->
                            val isCurrent = aiChan.id == activeChannel.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) Color(0xFF8B5CF6).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showSwitchAiDialog = false
                                        viewModel.openConversation(aiChan.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrent) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(aiChan.displayName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(aiChan.lastMessage.take(40), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }
                                    if (isCurrent) {
                                        Text("Active", fontSize = 11.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSwitchAiDialog = false
                        viewModel.createQuickerAiChat("New Study Topic")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ New Session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchAiDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// Message Bubble (User on right, Peer on left, with delivery ticks)
// -------------------------------------------------------------
@Composable
fun WhatsAppMessageBubble(
    message: ChatMessage,
    onClick: () -> Unit,
    onAnswerQuiz: (Int) -> Unit
) {
    val isUser = message.isFromUser
    val alignment = if (isUser) Alignment.End else Alignment.Start

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    onClick = onClick
                )
                .border(
                    1.dp,
                    if (isUser) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Reply context banner if any
                if (!message.replyToText.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Replying: ${message.replyToText}",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Peer sender name for group chats
                if (!isUser && message.senderName != "Quicker AI") {
                    Text(
                        text = message.senderName,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // AI Mode Badge (Smart, Expert or Master)
                if (message.aiModeUsed != null) {
                    val badgeColor = when (message.aiModeUsed) {
                        "Master" -> Color(0xFFF59E0B)
                        "Expert" -> Color(0xFF8B5CF6)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val badgeLabel = when (message.aiModeUsed) {
                        "Master" -> "👑 Master Mode"
                        "Expert" -> "🎓 Expert Mode"
                        else -> "⚡ Smart Mode"
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) else badgeColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else badgeColor
                            )
                        }
                    }
                }

                // Attached Document / PDF Source
                if (message.fileName != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (message.fileType == "pdf") Icons.Default.PictureAsPdf else Icons.Default.Description,
                                contentDescription = null,
                                tint = if (message.fileType == "pdf") Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.fileName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "AI Source • Local Device Only",
                                    fontSize = 9.5.sp,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Attached image if present
                if (!message.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(message.imageUri),
                        contentDescription = "Message Attachment",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .padding(bottom = 6.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Message Text with rich markdown, bold, underline, citations support
                QuicksRichText(
                    text = message.text,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )

                // Interactive Quiz Card (if AI quiz question)
                if (message.messageType == "QUIZ" && message.quizOptions != null) {
                    val options = message.quizOptions.split(",,,,")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message.quizQuestion ?: "Quiz Question",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    options.forEachIndexed { index, option ->
                        val isSelected = message.quizSelectedOption == index
                        val isCorrect = message.quizCorrectIndex == index
                        val hasAnswered = message.quizSelectedOption != -1

                        val optColor = when {
                            !hasAnswered -> MaterialTheme.colorScheme.surface
                            isCorrect -> Color(0xFF10B981) // Green
                            isSelected && !isCorrect -> Color(0xFFEF4444) // Red
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !hasAnswered) { onAnswerQuiz(index) },
                            color = optColor
                        ) {
                            Text(
                                text = "${('A' + index)}. $option",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected || (hasAnswered && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                color = if (hasAnswered && (isCorrect || isSelected)) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time, Encryption indicator and WhatsApp Delivery Checks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEncrypted) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Encrypted Message",
                            tint = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else Color(0xFF10B981),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }

                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Animated Typing Indicator
// -------------------------------------------------------------
@Composable
fun TypingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Quicker AI is thinking", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(6.dp))
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha1)))
        Spacer(modifier = Modifier.width(3.dp))
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha2)))
        Spacer(modifier = Modifier.width(3.dp))
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha3)))
    }
}

// -------------------------------------------------------------
// Message Options Modal Dialog (Triggered on click of any message)
// -------------------------------------------------------------
@Composable
fun MessageOptionsDialog(
    message: ChatMessage,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onForward: () -> Unit,
    onSaveToNotes: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Message Options",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "\"${message.text.take(80)}\"",
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reply Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReply() }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Reply, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Reply to Message", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                // Copy Text Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCopy() }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Copy Text", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                // Forward Message Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onForward() }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Forward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Forward Message", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                // Save to Study Notes Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSaveToNotes() }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Save to Study Notes", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// -------------------------------------------------------------
// Forward Message Dialog
// -------------------------------------------------------------
@Composable
fun ForwardMessageDialog(
    message: ChatMessage,
    channels: List<ChatChannel>,
    onDismiss: () -> Unit,
    onForwardTo: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Forward Message To...", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                items(channels) { channel ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onForwardTo(channel.id) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = channel.displayName.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = channel.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.Forward, contentDescription = "Forward", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// Quicker AI Memory & Context Modal
// -------------------------------------------------------------
@Composable
fun AiMemoryDialog(
    viewModel: QuicksViewModel,
    onDismiss: () -> Unit
) {
    val memories by viewModel.aiMemoryManager.memories.collectAsState()
    var newFact by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quicker AI Memory & Context", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quicker AI uses this persistent memory alongside the recent conversation history to provide personalized answers across all your study sessions.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Add new fact row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newFact,
                        onValueChange = { newFact = it },
                        placeholder = { Text("e.g. Taking AP Physics next week", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (newFact.isNotBlank()) {
                                viewModel.aiMemoryManager.addMemory("Study Context", newFact.trim())
                                newFact = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ACTIVE MEMORIES (${memories.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (memories.isEmpty()) {
                    Text(
                        text = "No custom memories yet. Add facts like your target grade, preferred explanation style, or subjects you're learning.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(memories, key = { it.id }) { mem ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "• ${mem.key}: ${mem.detail}",
                                        fontSize = 12.5.sp,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { viewModel.aiMemoryManager.deleteMemory(mem.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Memory",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
            ) {
                Text("Done")
            }
        }
    )
}

