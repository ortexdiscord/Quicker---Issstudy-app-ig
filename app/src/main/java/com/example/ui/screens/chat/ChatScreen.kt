package com.example.ui.screens.chat

import android.net.Uri
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    var showMenu by remember { mutableStateOf(false) }

    val filteredChannels = channels.filter { channel ->
        val matchesSearch = channel.displayName.contains(searchQuery, ignoreCase = true) ||
                channel.lastMessage.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Unread" -> channel.unreadCount > 0
            "Groups" -> !channel.isDirectMessage
            "AI" -> channel.id == "quicker_ai"
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
                modifier = Modifier.fillMaxWidth(),
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
                items(filteredChannels, key = { it.id }) { channel ->
                    WhatsAppChatListItem(
                        channel = channel,
                        onClick = { onSelectChannel(channel) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
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

    // New Chat / Group Creation Dialog
    if (showNewChatDialog) {
        var chatName by remember { mutableStateOf("") }
        var isGroup by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text(if (isGroup) "Create Study Group" else "Start New Chat", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isGroup = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isGroup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (!isGroup) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Direct Chat", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { isGroup = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGroup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isGroup) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Study Group", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = chatName,
                        onValueChange = { chatName = it },
                        label = { Text(if (isGroup) "Group Name" else "Peer Name") },
                        placeholder = { Text(if (isGroup) "e.g. Physics 301 Cohort" else "e.g. Elena Rostova") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chatName.isNotBlank()) {
                            viewModel.createNewChat(
                                name = chatName.trim(),
                                isDirect = !isGroup,
                                serverName = if (isGroup) "Study Squad" else null
                            )
                            showNewChatDialog = false
                        }
                    }
                ) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// WhatsApp Chat List Row Item (Exact structure as screenshot)
// -------------------------------------------------------------
@Composable
fun WhatsAppChatListItem(
    channel: ChatChannel,
    onClick: () -> Unit
) {
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
    }
}

// -------------------------------------------------------------
// 2. Conversation Detail View ("click on messages and type")
// -------------------------------------------------------------
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

    // Scroll to bottom when messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
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
            modifier = Modifier.fillMaxWidth(),
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
                            if (activeChannel.id == "quicker_ai") MaterialTheme.colorScheme.primary
                            else if (!activeChannel.isDirectMessage) Color(0xFF3B82F6)
                            else Color(0xFF10B981)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (activeChannel.id == "quicker_ai") {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
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
                    Text(
                        text = if (activeChannel.id == "quicker_ai") "Quicker AI • Ready to help"
                        else if (activeChannel.isDirectMessage) "Online • WhatsApp Sync"
                        else "Study Cohort • ${messages.size} messages",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Call icons
                IconButton(onClick = {}, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = {}, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
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

                if (isAiLoading && activeChannel.id == "quicker_ai") {
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

        // Quicker AI Action Tools (when chatting with Quicker AI)
        if (activeChannel.id == "quicker_ai") {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
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
                            .clickable { viewModel.requestAiQuiz("Physics Mechanics") }
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
                                    viewModel.requestAiSummary(inputText)
                                    inputText = ""
                                } else {
                                    viewModel.requestAiSummary("Wave Particle Duality")
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
                                viewModel.requestAiTranslation(textToTranslate, "Spanish")
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

        // WhatsApp Style Message Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Attachment Picker
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach Photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Text Input Field ("where you can click on messages and type")
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (activeChannel.id == "quicker_ai") "Ask Quicker AI or send notes..." else "Message...",
                            fontSize = 13.5.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    maxLines = 4,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button with animated morphing between Mic and Send Plane
                val isTyping = inputText.isNotBlank() || selectedImageUri != null
                val sendButtonBg by animateColorAsState(
                    targetValue = if (isTyping) Color(0xFF25D366) else MaterialTheme.colorScheme.primary,
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
                            // Simulated voice note recording
                            viewModel.sendChatMessage(
                                convoId = activeChannel.id,
                                text = "🎤 Voice Note (0:14) - Study Discussion"
                            )
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(sendButtonBg)
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = if (isTyping) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = if (isTyping) "Send" else "Record Voice Note",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
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

                // Message Text
                Text(
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

                // Time and WhatsApp Delivery Checks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
