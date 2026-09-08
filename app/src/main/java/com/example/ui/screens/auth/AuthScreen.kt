package com.example.ui.screens.auth

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.CodeVerificationManager
import com.example.data.firebase.FirebaseManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AuthScreen(
    codeVerificationManager: CodeVerificationManager? = null,
    firebaseManager: FirebaseManager? = null,
    onLoginSuccess: (name: String, username: String, email: String) -> Unit
) {
    val context = LocalContext.current
    val manager = remember { codeVerificationManager ?: CodeVerificationManager(context) }
    val scope = rememberCoroutineScope()

    // Request POST_NOTIFICATIONS permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Proceed */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Dynamic background ambient animation
    val infiniteTransition = rememberInfiniteTransition(label = "auth_gradient")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_shift"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF09090C),
            Color(0xFF181824),
            Color(0xFF0F101A),
            Color(0xFF070709)
        ),
        start = androidx.compose.ui.geometry.Offset(animOffset, 0f),
        end = androidx.compose.ui.geometry.Offset(animOffset + 600f, 1200f)
    )

    var step by remember { mutableIntStateOf(1) } // 1: Email, 2: Code Verification, 3: Profile Setup, 4: Welcome Banner
    var email by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isSendingCode by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(60) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastSentCodeHint by remember { mutableStateOf<String?>(null) }
    var showNotificationBanner by remember { mutableStateOf(false) }

    // Shake animation for validation errors
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        scope.launch {
            shakeOffset.snapTo(0f)
            repeat(3) {
                shakeOffset.animateTo(20f, tween(50))
                shakeOffset.animateTo(-20f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    LaunchedEffect(step) {
        if (step == 2) {
            countdown = 60
            while (countdown > 0) {
                delay(1000)
                countdown -= 1
            }
        } else if (step == 4) {
            delay(1600)
            val finalName = name.trim().ifBlank { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
            val finalUsername = username.trim().ifBlank { email.substringBefore("@").lowercase() }
            onLoginSuccess(finalName, finalUsername, email.trim().lowercase())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Header Brand
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Q",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Quicks",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "Minimalist Deep Study & Knowledge Workspace",
                fontSize = 13.sp,
                color = Color(0xFFA1A1AA),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Notification Banner if code dispatched
            AnimatedVisibility(
                visible = showNotificationBanner && step == 2,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    color = Color(0xFF064E3B).copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "System Security Notification Sent",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Check your Android notification shade for your code.",
                                color = Color(0xFFA7F3D0),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Step Card with AnimatedContent
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFF272732), RoundedCornerShape(24.dp)),
                color = Color(0xFF121218).copy(alpha = 0.96f),
                tonalElevation = 8.dp
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it }) + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(260), targetOffsetX = { -it / 3 }) + fadeOut(tween(200)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it }) + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(260), targetOffsetX = { it / 3 }) + fadeOut(tween(200)))
                        }
                    },
                    label = "auth_step_transition"
                ) { currentStep ->
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (currentStep) {
                            1 -> {
                                Text(
                                    text = "Sign In with Email",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Enter your real email address. We will generate and dispatch a secure 6-digit verification code.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA1A1AA),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
                                )

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it
                                        errorMessage = null
                                    },
                                    label = { Text("Email Address") },
                                    placeholder = { Text("student@university.edu", color = Color(0xFF52525B)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = "Email",
                                            tint = Color.White
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (!isSendingCode) {
                                                handleSendCode(
                                                    email = email,
                                                    manager = manager,
                                                    onSending = { isSendingCode = true },
                                                    onSent = { code ->
                                                        isSendingCode = false
                                                        lastSentCodeHint = code
                                                        showNotificationBanner = true
                                                        step = 2
                                                    },
                                                    onError = { err ->
                                                        isSendingCode = false
                                                        errorMessage = err
                                                        triggerShake()
                                                    }
                                                )
                                            }
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_email_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color(0xFF3F3F46),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color.White,
                                        unfocusedLabelColor = Color(0xFFA1A1AA),
                                        cursorColor = Color.White
                                    )
                                )

                                if (errorMessage != null) {
                                    Text(
                                        text = errorMessage!!,
                                        color = Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(22.dp))

                                Button(
                                    onClick = {
                                        handleSendCode(
                                            email = email,
                                            manager = manager,
                                            onSending = { isSendingCode = true },
                                            onSent = { code ->
                                                isSendingCode = false
                                                lastSentCodeHint = code
                                                showNotificationBanner = true
                                                step = 2
                                            },
                                            onError = { err ->
                                                isSendingCode = false
                                                errorMessage = err
                                                triggerShake()
                                            }
                                        )
                                    },
                                    enabled = !isSendingCode,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("send_code_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    if (isSendingCode) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = Color.Black,
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "Send Security Code",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFF71717A),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Protected by Cryptographic One-Time Passcode",
                                        fontSize = 11.sp,
                                        color = Color(0xFF71717A)
                                    )
                                }
                            }

                            2 -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            step = 1
                                            errorMessage = null
                                            codeInput = ""
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color(0xFFA1A1AA)
                                        )
                                    }
                                    Text(
                                        text = "Enter Verification Code",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Text(
                                    text = "A single-use 6-digit security code was dispatched for ${email.trim()}.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA1A1AA),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                                )

                                // Visual 6-Box Code Input Display
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    for (i in 0 until 6) {
                                        val char = codeInput.getOrNull(i)?.toString() ?: ""
                                        val isCurrent = codeInput.length == i
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF1E1E26))
                                                .border(
                                                    width = if (isCurrent) 2.dp else 1.dp,
                                                    color = if (isCurrent) Color.White else Color(0xFF3F3F46),
                                                    shape = RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = char,
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = codeInput,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() }
                                        if (filtered.length <= 6) {
                                            codeInput = filtered
                                            errorMessage = null
                                            if (filtered.length == 6) {
                                                // Auto verify upon 6 digits entered
                                                handleVerifyCode(
                                                    code = filtered,
                                                    email = email,
                                                    manager = manager,
                                                    onSuccess = { step = 3 },
                                                    onError = { err ->
                                                        errorMessage = err
                                                        triggerShake()
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    label = { Text("Type 6 Digits") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Code",
                                            tint = Color.White
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.NumberPassword,
                                        imeAction = ImeAction.Done
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("verification_code_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color(0xFF3F3F46),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color.White,
                                        unfocusedLabelColor = Color(0xFFA1A1AA),
                                        cursorColor = Color.White
                                    )
                                )

                                if (errorMessage != null) {
                                    Text(
                                        text = errorMessage!!,
                                        color = Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (countdown > 0) "Code expires in ${countdown}s" else "Code expired",
                                        color = if (countdown > 0) Color(0xFFA1A1AA) else Color(0xFFEF4444),
                                        fontSize = 12.sp
                                    )

                                    TextButton(
                                        onClick = {
                                            handleSendCode(
                                                email = email,
                                                manager = manager,
                                                onSending = { isSendingCode = true },
                                                onSent = { code ->
                                                    isSendingCode = false
                                                    lastSentCodeHint = code
                                                    countdown = 60
                                                    errorMessage = null
                                                    codeInput = ""
                                                    showNotificationBanner = true
                                                },
                                                onError = { err ->
                                                    isSendingCode = false
                                                    errorMessage = err
                                                    triggerShake()
                                                }
                                            )
                                        },
                                        enabled = !isSendingCode && countdown == 0
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (countdown == 0) Color.White else Color(0xFF52525B)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Resend",
                                            color = if (countdown == 0) Color.White else Color(0xFF52525B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        handleVerifyCode(
                                            code = codeInput,
                                            email = email,
                                            manager = manager,
                                            onSuccess = { step = 3 },
                                            onError = { err ->
                                                errorMessage = err
                                                triggerShake()
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("verify_code_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Verify & Continue", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }

                            3 -> {
                                Text(
                                    text = "Set Up Your Profile",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "How should your study cohort and Quicks identify you?",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA1A1AA),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                                )

                                // Dynamic Avatar Preview based on Name
                                val initials = name.trim().split(" ")
                                    .filter { it.isNotBlank() }
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .joinToString("")
                                    .ifBlank { "?" }

                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF272738))
                                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                        errorMessage = null
                                    },
                                    label = { Text("Your Full Name") },
                                    placeholder = { Text("e.g. Jordan Lee", color = Color(0xFF52525B)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "Name",
                                            tint = Color.White
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("full_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color(0xFF3F3F46),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color.White,
                                        unfocusedLabelColor = Color(0xFFA1A1AA),
                                        cursorColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = username,
                                    onValueChange = {
                                        username = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' }
                                    },
                                    label = { Text("Username / Handle") },
                                    placeholder = { Text("jordan_study", color = Color(0xFF52525B)) },
                                    prefix = { Text("@", color = Color(0xFFA1A1AA)) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("username_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color(0xFF3F3F46),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color.White,
                                        unfocusedLabelColor = Color(0xFFA1A1AA),
                                        cursorColor = Color.White
                                    )
                                )

                                if (errorMessage != null) {
                                    Text(
                                        text = errorMessage!!,
                                        color = Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(22.dp))

                                Button(
                                    onClick = {
                                        if (name.isBlank()) {
                                            errorMessage = "Please enter your name to personalize your workspace"
                                            triggerShake()
                                        } else {
                                            step = 4
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("start_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("Launch Study Workspace", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            4 -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Success",
                                            tint = Color.White,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Text(
                                        text = "Verification Complete",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "Welcome to Quicks, ${name.ifBlank { "Scholar" }}.",
                                        fontSize = 14.sp,
                                        color = Color(0xFFA1A1AA),
                                        modifier = Modifier.padding(top = 6.dp)
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun handleSendCode(
    email: String,
    manager: CodeVerificationManager,
    onSending: () -> Unit,
    onSent: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (email.isBlank() || !email.contains("@")) {
        onError("Please enter a valid email address.")
        return
    }
    onSending()
    when (val result = manager.sendVerificationCode(email)) {
        is CodeVerificationManager.VerificationResult.Sent -> {
            onSent(result.code)
        }
        is CodeVerificationManager.VerificationResult.Error -> {
            onError(result.message)
        }
        else -> {}
    }
}

private fun handleVerifyCode(
    code: String,
    email: String,
    manager: CodeVerificationManager,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (code.length != 6) {
        onError("Please enter all 6 digits of your verification code.")
        return
    }
    when (val result = manager.verifyCode(code, email)) {
        is CodeVerificationManager.VerificationResult.Success -> {
            onSuccess()
        }
        is CodeVerificationManager.VerificationResult.Error -> {
            onError(result.message)
        }
        else -> {}
    }
}
