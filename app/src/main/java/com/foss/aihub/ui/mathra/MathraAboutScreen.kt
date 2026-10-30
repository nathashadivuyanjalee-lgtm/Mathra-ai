package com.foss.aihub.ui.mathra

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foss.aihub.BuildConfig
import com.foss.aihub.R
import com.foss.aihub.utils.CREATOR_CONTACT
import com.foss.aihub.utils.CREATOR_NAME
import com.foss.aihub.utils.GITHUB_PROFILE_URL
import com.foss.aihub.utils.TIKTOK_URL
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Star(val x: Float, val y: Float, val phase: Float, val size: Float)
private class RainDrop(val x: Float, val speed: Float, val len: Float, val phase: Float, val color: Color)
private class Bubble(val x: Float, val speed: Float, val size: Float, val phase: Float, val sway: Float)
private class Burst(val x: Float, val y: Float, val t0: Float)

/**
 * Animated About screen.
 * Effects (17): 1 twinkling stars, 2 shooting stars, 3 neon rain, 4 rising bubbles, 5 aurora glow,
 * 6 animated waves, 7 tap sparkle bursts, 8 ripple waves from logo, 9 rotating gradient ring,
 * 10 orbiting dots, 11 pulsing logo, 12 floating gold crown, 13 shimmer title, 14 typewriter tagline,
 * 15 breathing glow cards, 16 staggered slide-in, 17 heartbeat footer.
 */
@Composable
fun MathraAboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val stars = remember { val r = Random(7); List(70) { Star(r.nextFloat(), r.nextFloat(), r.nextFloat() * 6.28f, 1f + r.nextFloat() * 2.2f) } }
    val rain = remember {
        val r = Random(11)
        val cs = listOf(MathraColors.NeonCyan, MathraColors.NeonBlue, MathraColors.NeonPurple)
        List(22) { RainDrop(r.nextFloat(), 0.15f + r.nextFloat() * 0.25f, 0.05f + r.nextFloat() * 0.08f, r.nextFloat(), cs[r.nextInt(cs.size)]) }
    }
    val bubbles = remember {
        val r = Random(3)
        List(14) { Bubble(r.nextFloat(), 0.04f + r.nextFloat() * 0.07f, 4f + r.nextFloat() * 14f, r.nextFloat(), 0.02f + r.nextFloat() * 0.04f) }
    }
    val bursts = remember { mutableStateListOf<Burst>() }

    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                time = (now - start) / 1_000_000_000f
                bursts.removeAll { time - it.t0 > 1.6f }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MathraColors.Navy, MathraColors.NavyLight, MathraColors.Navy)))
            .pointerInput(Unit) {
                detectTapGestures { pos -> bursts.add(Burst(pos.x, pos.y, time)) }
            }
    ) {
        // ---------- background effects (1-7) ----------
        Canvas(Modifier.fillMaxSize()) {
            val tt = time
            drawAurora(tt)
            drawStars(stars, tt)
            drawShootingStars(tt)
            drawRain(rain, tt)
            drawBubbles(bubbles, tt)
            drawWaves(tt)
            drawBursts(bursts, tt)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                MathraRoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, onBack)
                Spacer(Modifier.width(12.dp))
                Text("About", color = MathraColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(Modifier.height(10.dp))

            LogoHero(time)

            Spacer(Modifier.height(10.dp))
            // 13: shimmer title
            val shimmerX = (time * 260f) % 1200f
            Text(
                "Mathra Ai Chat Bot",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(MathraColors.NeonCyan, Color.White, MathraColors.NeonPurple, MathraColors.NeonCyan),
                        start = Offset(shimmerX, 0f), end = Offset(shimmerX + 400f, 0f), tileMode = TileMode.Mirror
                    ),
                    fontSize = 32.sp, fontWeight = FontWeight.ExtraBold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.staggeredEntry(1)
            )
            // 14: typewriter tagline
            val phrases = listOf("Smarter • Faster • Together", "Ask • Learn • Create • Together", "ඔයාගේ AI යාළුවා 💙", "Your AI Friend")
            val cycle = 5f
            val idx = ((time / cycle).toInt()) % phrases.size
            val local = time % cycle
            val shown = (local * 14).toInt().coerceAtMost(phrases[idx].length)
            val cursor = if ((time * 2).toInt() % 2 == 0) "▌" else " "
            Text(
                phrases[idx].take(shown) + cursor,
                color = MathraColors.NeonCyan, fontSize = 15.sp, textAlign = TextAlign.Center,
                modifier = Modifier
                    .height(24.dp)
                    .staggeredEntry(2)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "v${BuildConfig.VERSION_NAME}",
                color = MathraColors.TextPrimary, fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MathraColors.NeonBlue.copy(alpha = 0.2f))
                    .border(1.dp, MathraColors.NeonBlue.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(20.dp))

            // 15 + 16: breathing glow cards that slide in one by one
            AboutCard(0, Icons.Rounded.Person, "Created by", "$CREATOR_NAME • දෙනුවන් ආකාශ්", MathraColors.Gold, time) {}
            Spacer(Modifier.height(12.dp))
            AboutCard(1, Icons.Rounded.MusicNote, "Mathra Quotes (TikTok)", "@mathra_quotes_1", MathraColors.NeonPink, time) { openLink(context, TIKTOK_URL) }
            Spacer(Modifier.height(12.dp))
            AboutCard(2, Icons.Rounded.Code, "GitHub", "github.com/denuvan-akash", MathraColors.NeonPurple, time) { openLink(context, GITHUB_PROFILE_URL) }
            Spacer(Modifier.height(12.dp))
            AboutCard(3, Icons.Rounded.Call, "Contact", CREATOR_CONTACT, MathraColors.NeonCyan, time) { dialNumber(context, CREATOR_CONTACT) }
            Spacer(Modifier.height(22.dp))

            // 17: heartbeat footer
            val beat = 1f + 0.18f * heartbeat(time)
            Text(
                "💙",
                fontSize = 30.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = beat
                    scaleY = beat
                }
            )
            Text("Made with love by $CREATOR_NAME", color = MathraColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("© Mathra Quotes", color = MathraColors.TextMuted, fontSize = 12.sp)
            Text("(තිරයේ ඕනෑම තැනක් touch කරන්න ✨)", color = MathraColors.TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(40.dp))
        }
    }
}

private fun heartbeat(t: Float): Float {
    val p = (t * 1.2f) % 1f
    return when {
        p < 0.12f -> p / 0.12f
        p < 0.24f -> 1f - (p - 0.12f) / 0.12f
        p < 0.36f -> (p - 0.24f) / 0.12f * 0.7f
        p < 0.5f -> 0.7f * (1f - (p - 0.36f) / 0.14f)
        else -> 0f
    }
}

@Composable
private fun LogoHero(time: Float) {
    val inf = rememberInfiniteTransition(label = "logoHero")
    val pulse by inf.animateFloat(
        1f, 1.06f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
    )
    val crown by inf.animateFloat(
        -6f, 6f, infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "crown"
    )
    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height / 2)
            val base = size.minDimension * 0.30f
            // 8: ripple waves
            for (i in 0 until 3) {
                val p = ((time * 0.35f) + i / 3f) % 1f
                drawCircle(
                    color = MathraColors.NeonBlue.copy(alpha = (1f - p) * 0.5f),
                    radius = base + p * size.minDimension * 0.2f,
                    center = c, style = Stroke(width = 3.dp.toPx())
                )
            }
            // 9: rotating gradient ring
            rotate(time * 60f, pivot = c) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(MathraColors.NeonBlue, MathraColors.NeonPurple, MathraColors.NeonPink, MathraColors.NeonCyan, MathraColors.NeonBlue),
                        center = c
                    ),
                    radius = base + 8.dp.toPx(), center = c, style = Stroke(width = 5.dp.toPx())
                )
            }
            // 10: orbiting dots
            for (i in 0 until 4) {
                val a = time * 1.4f + i * (PI.toFloat() / 2f)
                val r = base + 28.dp.toPx()
                val p = Offset(c.x + cos(a) * r, c.y + sin(a) * r)
                drawCircle(MathraColors.NeonCyan.copy(alpha = 0.25f), 9.dp.toPx(), p)
                drawCircle(Color.White, 3.5.dp.toPx(), p)
            }
        }
        // 11: pulsing logo
        Image(
            painter = painterResource(R.drawable.mathra_logo),
            contentDescription = "Mathra Ai Chat Bot",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .clip(CircleShape)
        )
        // 12: floating gold crown
        Text(
            "👑", fontSize = 30.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer { translationY = crown }
        )
    }
}

@Composable
private fun AboutCard(
    index: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    time: Float,
    onClick: () -> Unit
) {
    val breathe = 0.45f + 0.45f * (0.5f + 0.5f * sin(time * 2f + index))
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .staggeredEntry(3 + index)
            .bounceClick(onClick = onClick)
            .clip(shape)
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.18f), MathraColors.Card)))
            .border(1.5.dp, accent.copy(alpha = breathe), shape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = MathraColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = MathraColors.TextMuted, fontSize = 13.sp)
        }
    }
}

// ---------------------------------------------------------------- background drawing

private fun DrawScope.drawAurora(t: Float) {
    val w = size.width
    val h = size.height
    val c1 = Offset(w * (0.5f + 0.4f * sin(t * 0.25f)), h * (0.2f + 0.08f * cos(t * 0.3f)))
    val c2 = Offset(w * (0.5f + 0.4f * cos(t * 0.2f)), h * (0.6f + 0.1f * sin(t * 0.22f)))
    drawCircle(Brush.radialGradient(listOf(MathraColors.NeonBlue.copy(alpha = 0.30f), Color.Transparent), c1, w * 0.8f), w * 0.8f, c1)
    drawCircle(Brush.radialGradient(listOf(MathraColors.NeonPurple.copy(alpha = 0.26f), Color.Transparent), c2, w * 0.8f), w * 0.8f, c2)
}

private fun DrawScope.drawStars(stars: List<Star>, t: Float) {
    for (s in stars) {
        val a = 0.25f + 0.75f * (0.5f + 0.5f * sin(t * 2f + s.phase))
        drawCircle(Color.White.copy(alpha = a), s.size, Offset(s.x * size.width, s.y * size.height))
    }
}

private fun DrawScope.drawShootingStars(t: Float) {
    for (i in 0 until 2) {
        val p = ((t / 4f) + i * 0.5f) % 1f
        if (p < 0.35f) {
            val k = p / 0.35f
            val sx = size.width * (0.9f - 0.6f * k + i * 0.1f)
            val sy = size.height * (0.05f + 0.3f * k + i * 0.2f)
            drawLine(
                Brush.linearGradient(listOf(Color.White, Color.Transparent), Offset(sx, sy), Offset(sx + 120f, sy - 70f)),
                Offset(sx, sy), Offset(sx + 120f, sy - 70f), strokeWidth = 3f
            )
        }
    }
}

private fun DrawScope.drawRain(rain: List<RainDrop>, t: Float) {
    for (d in rain) {
        val p = (t * d.speed + d.phase) % 1f
        val y = p * (size.height + 200f) - 100f
        val x = d.x * size.width
        drawLine(
            Brush.verticalGradient(listOf(Color.Transparent, d.color.copy(alpha = 0.55f)), startY = y - d.len * size.height, endY = y),
            Offset(x, y - d.len * size.height), Offset(x, y), strokeWidth = 2.5f
        )
    }
}

private fun DrawScope.drawBubbles(bubbles: List<Bubble>, t: Float) {
    for (b in bubbles) {
        val p = (t * b.speed + b.phase) % 1f
        val y = size.height * (1f - p)
        val x = size.width * (b.x + b.sway * sin(t + b.phase * 6f))
        drawCircle(MathraColors.NeonCyan.copy(alpha = 0.35f * (1f - p)), b.size, Offset(x, y), style = Stroke(1.5f))
        drawCircle(MathraColors.NeonBlue.copy(alpha = 0.10f * (1f - p)), b.size, Offset(x, y))
    }
}

private fun DrawScope.drawWaves(t: Float) {
    val colors = listOf(MathraColors.NeonBlue.copy(alpha = 0.22f), MathraColors.NeonPurple.copy(alpha = 0.22f), MathraColors.NeonCyan.copy(alpha = 0.18f))
    for (i in 0 until 3) {
        val path = Path()
        val baseY = size.height * (0.93f - i * 0.025f)
        path.moveTo(0f, size.height)
        var x = 0f
        while (x <= size.width + 8f) {
            val y = baseY + sin(x / size.width * 6.28f * (1.5f + i * 0.4f) + t * (1.2f + i * 0.5f)) * 14f
            path.lineTo(x, y)
            x += 8f
        }
        path.lineTo(size.width, size.height)
        path.close()
        drawPath(path, colors[i])
    }
}

private fun DrawScope.drawBursts(bursts: List<Burst>, t: Float) {
    val cols = listOf(MathraColors.NeonCyan, MathraColors.NeonPink, MathraColors.Gold, MathraColors.NeonPurple)
    for (b in bursts) {
        val age = (t - b.t0) / 1.5f
        if (age !in 0f..1f) continue
        for (i in 0 until 14) {
            val a = i * (6.28318f / 14f)
            val d = age * 150f
            val p = Offset(b.x + cos(a) * d, b.y + sin(a) * d + age * age * 60f)
            drawCircle(cols[i % cols.size].copy(alpha = 1f - age), 5f * (1f - age) + 1.5f, p)
        }
    }
}
