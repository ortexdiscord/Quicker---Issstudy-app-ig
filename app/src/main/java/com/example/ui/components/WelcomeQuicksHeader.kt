package com.example.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class FontVariant(
    val id: Int,
    val name: String,
    val fontFamily: FontFamily,
    val fontStyle: FontStyle = FontStyle.Normal,
    val fontWeight: FontWeight = FontWeight.Bold,
    val letterSpacing: Float = 0f,
    val accentColor: Color = Color.White
)

val quicksFontVariants = listOf(
    FontVariant(
        id = 0,
        name = "Serif Editorial",
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.5f,
        accentColor = Color(0xFFF3E8FF)
    ),
    FontVariant(
        id = 1,
        name = "Monospace Cyber",
        fontFamily = FontFamily.Monospace,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 3.5f,
        accentColor = Color(0xFF67E8F9)
    ),
    FontVariant(
        id = 2,
        name = "Cursive Script",
        fontFamily = FontFamily.Cursive,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.0f,
        accentColor = Color(0xFFFDE047)
    ),
    FontVariant(
        id = 3,
        name = "Sans Geometric",
        fontFamily = FontFamily.SansSerif,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5f,
        accentColor = Color(0xFF34D399)
    ),
    FontVariant(
        id = 4,
        name = "Display Modern",
        fontFamily = FontFamily.Default,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.Bold,
        letterSpacing = 4.0f,
        accentColor = Color(0xFF818CF8)
    )
)

@Composable
fun WelcomeQuicksHeader(
    modifier: Modifier = Modifier
) {
    var fontIndex by remember { mutableIntStateOf(0) }

    // Auto-cycle through the 5 fonts every 1.5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            fontIndex = (fontIndex + 1) % quicksFontVariants.size
        }
    }

    val currentVariant = quicksFontVariants[fontIndex]

    val infiniteTransition = rememberInfiniteTransition(label = "welcome_brand_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brand_pulse"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing brand badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            currentVariant.accentColor,
                            Color.White.copy(alpha = 0.8f)
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Q",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Welcome to" with spacious tracking
        Text(
            text = "Welcome to",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFA1A1AA),
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // "Quicks" dynamically morphing between the 5 fonts with animations
        AnimatedContent(
            targetState = currentVariant,
            transitionSpec = {
                (scaleIn(initialScale = 0.85f, animationSpec = spring(stiffness = 400f)) +
                 fadeIn(animationSpec = tween(300))) togetherWith
                (scaleOut(targetScale = 1.15f, animationSpec = tween(250)) +
                 fadeOut(animationSpec = tween(250)))
            },
            label = "quicks_font_switcher"
        ) { variant ->
            Text(
                text = "Quicks",
                fontSize = 42.sp,
                fontFamily = variant.fontFamily,
                fontStyle = variant.fontStyle,
                fontWeight = variant.fontWeight,
                color = variant.accentColor,
                letterSpacing = variant.letterSpacing.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Active Font Indicator Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1E28).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF323242), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(currentVariant.accentColor)
            )
            Text(
                text = "Font ${currentVariant.id + 1}/5 • ${currentVariant.name}",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD4D4D8)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Minimalist Deep Study & Knowledge Workspace",
            fontSize = 12.5.sp,
            color = Color(0xFF71717A),
            textAlign = TextAlign.Center
        )
    }
}
