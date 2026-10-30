package com.foss.aihub.ui.mathra

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Neon colours taken from the Mathra logo. */
object MathraColors {
    val Navy = Color(0xFF050B24)
    val NavyLight = Color(0xFF0B1536)
    val Card = Color(0xFF0F1B45)
    val NeonBlue = Color(0xFF00B7FF)
    val NeonCyan = Color(0xFF38E8FF)
    val NeonPurple = Color(0xFF8A5CFF)
    val NeonPink = Color(0xFFFF4FD8)
    val Gold = Color(0xFFFFC83D)
    val TextPrimary = Color(0xFFEAF6FF)
    val TextMuted = Color(0xFF9FB4D9)
}

/** Press -> bouncy shrink + spring back. Works on any composable. */
fun Modifier.bounceClick(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ),
        label = "bounce"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/** Fade + slide-up entrance, delayed by [index] so items appear one after another. */
fun Modifier.staggeredEntry(index: Int = 0): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 80L)
        progress.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 70f
    }
}

/** Big glowing gradient button with moving gradient + pulsing glow. */
@Composable
fun MathraGlowButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    colors: List<Color> = listOf(MathraColors.NeonBlue, MathraColors.NeonPurple),
    height: Dp = 64.dp
) {
    val t = rememberInfiniteTransition(label = "glowBtn")
    val shift by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift"
    )
    val glow by t.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .bounceClick(onClick = onClick)
            .shadow(
                elevation = (6 + 10 * glow).dp, shape = shape,
                ambientColor = colors.first(), spotColor = colors.first()
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(0f, 0f),
                    end = Offset(300f + 500f * shift, 200f)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f + 0.35f * glow), shape)
            .height(height)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (subtitle != null) {
                    Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                }
            }
        }
    }
}

/** Square-ish tile button with floating icon animation (used for Ask / Learn / Create / Together). */
@Composable
fun MathraTileButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MathraColors.NeonBlue,
    floatDelay: Int = 0
) {
    val t = rememberInfiniteTransition(label = "tile")
    val bob by t.animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(
            tween(1500 + floatDelay, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "bob"
    )
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .bounceClick(onClick = onClick)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.28f), MathraColors.Card)
                )
            )
            .border(1.dp, accent.copy(alpha = 0.55f), shape)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon, contentDescription = null, tint = accent,
            modifier = Modifier
                .size(30.dp)
                .graphicsLayer { translationY = bob }
        )
        Text(
            label, color = MathraColors.TextPrimary, fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center
        )
    }
}

/** Small pill button (links under chat bubbles, creator card buttons...). */
@Composable
fun MathraPillButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MathraColors.NeonBlue
) {
    val t = rememberInfiniteTransition(label = "pill")
    val glow by t.animateFloat(
        initialValue = 0.35f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pillGlow"
    )
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .bounceClick(onClick = onClick)
            .clip(shape)
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = glow), shape)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        Text(text, color = MathraColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** Round icon-only button (back, mic, send, speaker...). */
@Composable
fun MathraRoundIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MathraColors.NeonBlue,
    filled: Boolean = false,
    size: Dp = 44.dp,
    pulse: Boolean = false
) {
    val t = rememberInfiniteTransition(label = "round")
    val p by t.animateFloat(
        initialValue = 1f, targetValue = if (pulse) 1.12f else 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "roundPulse"
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = p
                scaleY = p
            }
            .bounceClick(onClick = onClick)
            .clip(CircleShape)
            .background(if (filled) Brush.linearGradient(listOf(accent, MathraColors.NeonPurple)) else Brush.linearGradient(listOf(accent.copy(alpha = 0.16f), accent.copy(alpha = 0.16f))))
            .border(1.dp, accent.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (filled) Color.White else accent, modifier = Modifier.size(size * 0.5f))
    }
}
