package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderBottom
import com.example.ui.theme.GlassBorderTop
import com.example.ui.theme.GlassSpecularWhite
import com.example.ui.theme.GlassTranslucentElevated
import com.example.ui.theme.GlassTranslucentSurface
import com.example.ui.theme.LiquidGlowCyan
import com.example.ui.theme.LiquidGlowViolet

/**
 * Creates a liquid glass border brush with a bright specular highlight along the top
 * fading to a soft translucent rim along the bottom.
 */
fun liquidGlassBorder(
    topColor: Color = GlassBorderTop,
    bottomColor: Color = GlassBorderBottom
): Brush {
    return Brush.verticalGradient(
        colors = listOf(topColor, bottomColor)
    )
}

/**
 * Creates a translucent frosted liquid glass background brush with subtle top-down refraction.
 */
fun liquidGlassBackground(
    tint: Color = Color(0xFF14141A),
    alpha: Float = 0.85f,
    specularAlpha: Float = 0.12f
): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            tint.copy(alpha = alpha).compositeOver(Color.White.copy(alpha = specularAlpha)),
            tint.copy(alpha = alpha)
        )
    )
}

/**
 * Helper to composite a translucent color over an opaque base.
 */
private fun Color.compositeOver(background: Color): Color {
    val a = this.alpha
    return Color(
        red = this.red * a + background.red * (1f - a),
        green = this.green * a + background.green * (1f - a),
        blue = this.blue * a + background.blue * (1f - a),
        alpha = 1f
    )
}

/**
 * Liquid Glass Card container with specular reflection, liquid frosted glass styling,
 * rounded corners, subtle refraction border, and animated refractive sheen.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color(0xFF0D0D14),
    backgroundAlpha: Float = 0.85f,
    borderTopColor: Color = Color(0x80FFFFFF),
    borderBottomColor: Color = Color(0x22FFFFFF),
    borderWidth: Dp = 1.dp,
    showShimmer: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true),
            onClick = onClick
        )
    } else {
        Modifier
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glass_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen_x"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .then(clickableModifier)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = backgroundAlpha),
                        backgroundColor.copy(alpha = (backgroundAlpha + 0.10f).coerceAtMost(0.98f))
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(borderTopColor, borderBottomColor)
                ),
                shape = shape
            )
    ) {
        // Specular highlight line along top inside edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GlassSpecularWhite.copy(alpha = 0.65f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Subtle liquid sheen reflection across card
        if (showShimmer) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x10FFFFFF),
                                Color(0x18FFFFFF),
                                Color.Transparent
                            ),
                            start = Offset(shimmerOffset, 0f),
                            end = Offset(shimmerOffset + 240f, 100f)
                        )
                    )
            )
        }

        content()
    }
}

/**
 * Floating Liquid Glass Pill Container with chromatic glass refraction and high-definition specular edge.
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    backgroundColor: Color = Color(0xFF0E0E16),
    backgroundAlpha: Float = 0.88f,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pill_ambient")
    val ambientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_light"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = backgroundAlpha),
                        backgroundColor.copy(alpha = (backgroundAlpha + 0.08f).coerceAtMost(0.98f))
                    )
                )
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        LiquidGlowViolet.copy(alpha = 0.08f),
                        Color.Transparent,
                        LiquidGlowCyan.copy(alpha = 0.07f)
                    ),
                    start = Offset(ambientShift, 0f),
                    end = Offset(ambientShift + 300f, 100f)
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x99FFFFFF), // Crisp diamond specular top rim
                        Color(0x35FFFFFF),
                        Color(0x15FFFFFF)  // Subtle ambient underside
                    )
                ),
                shape = shape
            )
    ) {
        // Specular crystal line across the top rim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xAAFFFFFF),
                            Color(0x30FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}

/**
 * Animated liquid aurora backdrop brush that glides slowly with radiant reflections.
 */
@Composable
fun rememberLiquidAuroraBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "aurora")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_shift"
    )

    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF000000), // OLED Black
            Color(0xFF07040E),
            Color(0xFF100720), // Deep obsidian violet
            Color(0xFF04121A), // Deep oceanic
            Color(0xFF000000)  // Pure black
        ),
        start = Offset(shift * 0.4f, 0f),
        end = Offset(shift + 800f, 1600f)
    )
}
