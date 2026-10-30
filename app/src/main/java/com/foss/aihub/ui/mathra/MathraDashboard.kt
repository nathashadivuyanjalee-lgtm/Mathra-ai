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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foss.aihub.R
import com.foss.aihub.models.AiService
import com.foss.aihub.utils.CREATOR_CONTACT
import com.foss.aihub.utils.CREATOR_NAME
import com.foss.aihub.utils.GITHUB_PROFILE_URL
import com.foss.aihub.utils.TIKTOK_URL
import java.util.Calendar

@Composable
fun MathraDashboard(
    aiServices: List<AiService>,
    onOpenChat: (String?) -> Unit,
    onOpenService: (AiService) -> Unit,
    onOpenHub: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    val context = LocalContext.current
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "සුභ උදෑසනක්! ☀️  Good morning"
        in 12..16 -> "සුභ දහවලක්! 🌤️  Good afternoon"
        in 17..20 -> "සුභ සන්ධ්‍යාවක්! 🌆  Good evening"
        else -> "සුභ රාත්‍රියක්! 🌙  Good night"
    }

    val t = rememberInfiniteTransition(label = "dash")
    val ring by t.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "ring"
    )
    val pulse by t.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val orb by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "orb"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MathraColors.Navy, MathraColors.NavyLight, MathraColors.Navy)))
            .drawBehind {
                // soft moving neon glow orbs
                drawCircle(
                    Brush.radialGradient(
                        listOf(MathraColors.NeonBlue.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(size.width * (0.15f + 0.6f * orb), size.height * 0.18f),
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = Offset(size.width * (0.15f + 0.6f * orb), size.height * 0.18f)
                )
                drawCircle(
                    Brush.radialGradient(
                        listOf(MathraColors.NeonPurple.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(size.width * (0.9f - 0.6f * orb), size.height * 0.75f),
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = Offset(size.width * (0.9f - 0.6f * orb), size.height * 0.75f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- hero ----------
            Box(
                modifier = Modifier
                    .size(176.dp)
                    .staggeredEntry(0),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    rotate(ring) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(
                                    MathraColors.NeonBlue, MathraColors.NeonPurple,
                                    MathraColors.NeonPink, MathraColors.NeonCyan, MathraColors.NeonBlue
                                )
                            ),
                            style = Stroke(width = 5.dp.toPx())
                        )
                    }
                }
                Image(
                    painter = painterResource(R.drawable.mathra_logo),
                    contentDescription = "Mathra Ai Chat Bot",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(152.dp)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        }
                        .clip(CircleShape)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Mathra Ai Chat Bot",
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(MathraColors.NeonCyan, MathraColors.NeonBlue, MathraColors.NeonPurple)),
                    fontSize = 30.sp, fontWeight = FontWeight.ExtraBold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.staggeredEntry(1)
            )
            Text(
                "Smarter • Faster • Together",
                color = MathraColors.TextMuted, fontSize = 13.sp,
                modifier = Modifier.staggeredEntry(2)
            )
            Spacer(Modifier.height(6.dp))
            Text(greeting, color = MathraColors.TextPrimary, fontSize = 15.sp, modifier = Modifier.staggeredEntry(2))
            Spacer(Modifier.height(18.dp))

            // ---------- main chat button ----------
            MathraGlowButton(
                text = "Mathra AI සමඟ කතා කරන්න",
                subtitle = "Chat with Mathra AI • සිංහල & English 🔊",
                icon = Icons.Rounded.SmartToy,
                onClick = { onOpenChat(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntry(3)
            )
            Spacer(Modifier.height(14.dp))

            // ---------- Ask / Learn / Create / Together ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntry(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MathraTileButton("Ask", Icons.Rounded.Lightbulb, { onOpenChat("ඔයාට මොනවද කරන්න පුළුවන්?") }, Modifier.weight(1f), MathraColors.NeonCyan, 0)
                MathraTileButton("Learn", Icons.Rounded.School, { onOpenChat("මට අලුත් දෙයක් ඉගැන්නුවොත්?") }, Modifier.weight(1f), MathraColors.NeonBlue, 150)
                MathraTileButton("Create", Icons.Rounded.RocketLaunch, { onOpenChat("Write a short motivational quote") }, Modifier.weight(1f), MathraColors.NeonPurple, 300)
                MathraTileButton("Together", Icons.Rounded.Groups, { onOpenChat("Who made you?") }, Modifier.weight(1f), MathraColors.NeonPink, 450)
            }
            Spacer(Modifier.height(20.dp))

            // ---------- AI services ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntry(5),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AI Services", color = MathraColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                MathraPillButton("View all", Icons.Rounded.Apps, onOpenHub, accent = MathraColors.NeonCyan)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .staggeredEntry(6),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                aiServices.take(14).forEach { s ->
                    MathraPillButton(s.name, Icons.Rounded.SmartToy, { onOpenService(s) }, accent = s.accentColor)
                }
            }
            Spacer(Modifier.height(20.dp))

            // ---------- creator card ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntry(7)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(MathraColors.Card, MathraColors.NavyLight)))
                    .border(1.dp, Brush.linearGradient(listOf(MathraColors.NeonBlue, MathraColors.NeonPurple)), RoundedCornerShape(24.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("👑 Created by", color = MathraColors.Gold, fontSize = 12.sp)
                Text(CREATOR_NAME, color = MathraColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("දෙනුවන් ආකාශ් • Mathra Quotes (TikTok)", color = MathraColors.TextMuted, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MathraPillButton(CREATOR_CONTACT, Icons.Rounded.Call, { dialNumber(context, CREATOR_CONTACT) }, accent = MathraColors.NeonCyan)
                    MathraPillButton("TikTok", Icons.Rounded.MusicNote, { openLink(context, TIKTOK_URL) }, accent = MathraColors.NeonPink)
                    MathraPillButton("GitHub", Icons.Rounded.Code, { openLink(context, GITHUB_PROFILE_URL) }, accent = MathraColors.NeonPurple)
                }
            }
            Spacer(Modifier.height(16.dp))

            // ---------- bottom buttons ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntry(8),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MathraGlowButton(
                    "Settings", Icons.Rounded.Settings, onSettings,
                    Modifier.weight(1f), colors = listOf(MathraColors.NeonPurple, MathraColors.NeonPink), height = 54.dp
                )
                MathraGlowButton(
                    "About", Icons.Rounded.Info, onAbout,
                    Modifier.weight(1f), colors = listOf(MathraColors.NeonBlue, MathraColors.NeonCyan), height = 54.dp
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("Your AI Friend 💙", color = MathraColors.TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
        }
    }
}
