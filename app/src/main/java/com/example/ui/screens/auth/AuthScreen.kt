package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
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
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.rememberLiquidAuroraBrush
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
    val fbManager = remember { firebaseManager ?: FirebaseManager(context) }
    val scope = rememberCoroutineScope()

    val auroraBrush = rememberLiquidAuroraBrush()

    // Step 0: Welcome to Quicks. [Enter]
    // Step 1: Sign Up / Log In with Gmail
    // Step 2: Code Verification via Gmail
    // Step 3: Profile Setup & Encrypted Account Saving
    // Step 4: Welcome & Workspace Launch
    var step by remember { mutableIntStateOf(0) }
    var isSignUpMode by remember { mutableStateOf(true) }

    var email by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isSendingCode by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(60) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSavingEncryptedAccount by remember { mutableStateOf(false) }

    // Shake animation for errors
    val shakeOffset = remember { Animatable(0f) }
    fun triggerShake() {
        scope.launch {
            shakeOffset.snapTo(0f)
            repeat(3) {
                shakeOffset.animateTo(16f, tween(45))
                shakeOffset.animateTo(-16f, tween(45))
            }
            shakeOffset.animateTo(0f, tween(45))
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
            delay(1400)
            val finalName = name.trim().ifBlank { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
            val finalUsername = username.trim().ifBlank { email.substringBefore("@").lowercase() }
            onLoginSuccess(finalName, finalUsername, email.trim().lowercase())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .background(auroraBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        if (step == 0) {
            // ==========================================
            // STEP 0: WELCOME TO QUICKS. ENTER GATE
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Subtle glowing orb behind header
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x40FFFFFF), Color.Transparent)
                            )
                        )
                        .border(1.dp, Color(0x30FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 44.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Welcome to Quicks.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Next-generation intelligent study engine\nPowered by Quicker AI & Study Cast Studio",
                    fontSize = 14.sp,
                    color = Color(0xFFA1A1AA),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Enter Button with Liquid Glass styling
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("welcome_enter_button"),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color(0xFF161622),
                    backgroundAlpha = 0.92f,
                    borderTopColor = Color(0x99FFFFFF),
                    borderBottomColor = Color(0x22FFFFFF),
                    onClick = { step = 1 }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Enter",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Enter",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // ==========================================
            // STEPS 1-4: SIGN IN / SIGN UP WORKFLOW
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Brand with 5 switching fonts
                com.example.ui.components.WelcomeQuicksHeader(
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Liquid Glass Auth Card
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0xFF0D0D14),
                    backgroundAlpha = 0.94f,
                    borderTopColor = Color(0x80FFFFFF),
                    borderBottomColor = Color(0x20FFFFFF)
                ) {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(tween(300)) { it } + fadeIn(tween(250))) togetherWith
                                    (slideOutHorizontally(tween(260)) { -it / 3 } + fadeOut(tween(200)))
                            } else {
                                (slideInHorizontally(tween(300)) { -it } + fadeIn(tween(250))) togetherWith
                                    (slideOutHorizontally(tween(260)) { it / 3 } + fadeOut(tween(200)))
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
                                    // Mode Switcher: Sign Up vs Log In
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF161622))
                                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                                            .padding(3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val signUpBg by animateColorAsState(if (isSignUpMode) Color.White else Color.Transparent, label = "su_bg")
                                        val signUpColor by animateColorAsState(if (isSignUpMode) Color.Black else Color(0xFFA1A1AA), label = "su_col")
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(11.dp))
                                                .clickable { isSignUpMode = true },
                                            color = signUpBg
                                        ) {
                                            Text(
                                                text = "Create Account",
                                                textAlign = TextAlign.Center,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = signUpColor,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }

                                        val logInBg by animateColorAsState(if (!isSignUpMode) Color.White else Color.Transparent, label = "li_bg")
                                        val logInColor by animateColorAsState(if (!isSignUpMode) Color.Black else Color(0xFFA1A1AA), label = "li_col")
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(11.dp))
                                                .clickable { isSignUpMode = false },
                                            color = logInBg
                                        ) {
                                            Text(
                                                text = "Sign In",
                                                textAlign = TextAlign.Center,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = logInColor,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Text(
                                        text = if (isSignUpMode) "Sign Up with Gmail" else "Welcome Back",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "We will send your single-use verification code directly to your Gmail inbox.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFA1A1AA),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                                    )

                                    // Direct "Sign up with Gmail" quick preset button
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0x18FFFFFF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x35FFFFFF)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (email.isBlank()) {
                                                    email = "user@gmail.com"
                                                } else if (!email.contains("@")) {
                                                    email = "$email@gmail.com"
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 11.dp, horizontal = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("🇬", fontSize = 16.sp) // Google indicator
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Continue with Gmail",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0x22FFFFFF)))
                                        Text(
                                            text = "or enter address",
                                            fontSize = 11.sp,
                                            color = Color(0xFF71717A),
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0x22FFFFFF)))
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = {
                                            email = it
                                            errorMessage = null
                                        },
                                        label = { Text("Gmail or Email") },
                                        placeholder = { Text("username@gmail.com", color = Color(0xFF52525B)) },
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
                                                    handleSendCodeToGmail(
                                                        email = email,
                                                        manager = manager,
                                                        onSending = { isSendingCode = true },
                                                        onSent = {
                                                            isSendingCode = false
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

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            handleSendCodeToGmail(
                                                email = email,
                                                manager = manager,
                                                onSending = { isSendingCode = true },
                                                onSent = {
                                                    isSendingCode = false
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
                                                Icon(Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Send Code to Gmail",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Codes are dispatched to your Gmail inbox (not in messages).",
                                        fontSize = 11.sp,
                                        color = Color(0xFF71717A),
                                        textAlign = TextAlign.Center
                                    )
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
                                            text = "Check Your Gmail",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Text(
                                        text = "A single-use 6-digit verification code was sent to ${email.trim()}.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFA1A1AA),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
                                    )

                                    // Quick Open Gmail Button
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x18FFFFFF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x35FFFFFF)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { manager.openMailInbox() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Email,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Open Gmail Inbox",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // 6 Digit Cells Display
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

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = codeInput,
                                        onValueChange = { input ->
                                            val filtered = input.filter { it.isDigit() }
                                            if (filtered.length <= 6) {
                                                codeInput = filtered
                                                errorMessage = null
                                                if (filtered.length == 6) {
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

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (countdown > 0) "Expires in ${countdown}s" else "Expired",
                                            color = if (countdown > 0) Color(0xFFA1A1AA) else Color(0xFFEF4444),
                                            fontSize = 12.sp
                                        )

                                        TextButton(
                                            onClick = {
                                                handleSendCodeToGmail(
                                                    email = email,
                                                    manager = manager,
                                                    onSending = { isSendingCode = true },
                                                    onSent = {
                                                        isSendingCode = false
                                                        countdown = 60
                                                        errorMessage = null
                                                        codeInput = ""
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
                                            Text("Resend to Gmail", color = if (countdown == 0) Color.White else Color(0xFF52525B))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

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
                                        text = "Profile & Account Security",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Your account data will be cryptographically encrypted and synchronized securely.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFA1A1AA),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                                    )

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
                                            .background(Color(0xFF20202E))
                                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
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
                                        label = { Text("Username") },
                                        placeholder = { Text("jordan_quicks", color = Color(0xFF52525B)) },
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

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Encrypted Account Badge
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Encrypted Account Storage Active",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF34D399)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Button(
                                        onClick = {
                                            if (name.isBlank()) {
                                                errorMessage = "Please enter your name"
                                                triggerShake()
                                            } else {
                                                scope.launch {
                                                    isSavingEncryptedAccount = true
                                                    fbManager.saveEncryptedUserAccount(
                                                        email = email,
                                                        name = name,
                                                        username = username.ifBlank { email.substringBefore("@") }
                                                    )
                                                    isSavingEncryptedAccount = false
                                                    step = 4
                                                }
                                            }
                                        },
                                        enabled = !isSavingEncryptedAccount,
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
                                        if (isSavingEncryptedAccount) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text("Launch Quicks", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                            }
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
}

private fun handleSendCodeToGmail(
    email: String,
    manager: CodeVerificationManager,
    onSending: () -> Unit,
    onSent: () -> Unit,
    onError: (String) -> Unit
) {
    if (email.isBlank() || !email.contains("@")) {
        onError("Please enter a valid Gmail address.")
        return
    }
    onSending()
    when (val result = manager.sendVerificationCode(email)) {
        is CodeVerificationManager.VerificationResult.Sent -> {
            onSent()
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
