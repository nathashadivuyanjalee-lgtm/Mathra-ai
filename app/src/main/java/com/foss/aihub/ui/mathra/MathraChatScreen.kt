package com.foss.aihub.ui.mathra

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foss.aihub.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun openLink(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
        Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
    }
}

fun dialNumber(context: Context, number: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    } catch (_: Exception) {
        Toast.makeText(context, number, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun MathraChatScreen(onBack: () -> Unit, initialPrompt: String? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val voice = remember { MathraVoice(context) }
    DisposableEffect(Unit) { onDispose { voice.shutdown() } }

    val messages = remember { mutableStateListOf<ChatMsg>() }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var autoSpeak by remember { mutableStateOf(true) }
    var sinhalaMic by remember { mutableStateOf(true) }
    var bannerDismissed by remember { mutableStateOf(false) }
    var idCounter by remember { mutableLongStateOf(0L) }
    val listState = rememberLazyListState()

    fun send(raw: String) {
        val q = raw.trim()
        if (q.isEmpty() || loading) return
        idCounter += 1
        messages.add(ChatMsg(idCounter, true, q))
        input = ""
        scope.launch {
            loading = true
            val local = MathraBot.localAnswer(q)
            val reply: ChatMsg
            if (local != null) {
                delay(600)
                idCounter += 1
                reply = ChatMsg(idCounter, false, local.text, local.actions)
            } else {
                val ans = MathraBot.askRemote(messages.toList())
                idCounter += 1
                reply = if (ans != null) ChatMsg(idCounter, false, ans)
                else ChatMsg(idCounter, false, MathraBot.errorText(q), isError = true)
            }
            loading = false
            messages.add(reply)
            if (autoSpeak && !reply.isError) voice.speak(reply.text)
        }
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val spoken = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (res.resultCode == Activity.RESULT_OK && !spoken.isNullOrBlank()) send(spoken)
    }

    fun startMic() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (sinhalaMic) "si-LK" else "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (sinhalaMic) "කතා කරන්න..." else "Speak now...")
        }
        try {
            micLauncher.launch(i)
        } catch (_: Exception) {
            Toast.makeText(context, "Voice input not available on this phone", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        idCounter += 1
        messages.add(
            ChatMsg(
                idCounter, false,
                "ආයුබෝවන්! මම Mathra Ai Chat Bot 💙 ඔයාගේ AI යාළුවා.\nHello! How can I help you?"
            )
        )
        if (!initialPrompt.isNullOrBlank()) {
            delay(400)
            send(initialPrompt)
        }
    }

    LaunchedEffect(messages.size, loading) {
        val count = messages.size + if (loading) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MathraColors.Navy, MathraColors.NavyLight, MathraColors.Navy)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // ---------- top bar ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MathraRoundIconButton(icon = Icons.AutoMirrored.Rounded.ArrowBack, onClick = {
                    voice.stop(); onBack()
                })
                Spacer(Modifier.width(10.dp))
                Image(
                    painter = painterResource(R.drawable.mathra_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MathraColors.NeonCyan, CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Mathra Ai Chat Bot", color = MathraColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(
                        if (voice.speaking) "🔊 කතා කරනවා..." else "Your AI Friend",
                        color = MathraColors.NeonCyan, fontSize = 12.sp
                    )
                }
                if (voice.speaking) {
                    MathraRoundIconButton(
                        icon = Icons.Rounded.Stop, onClick = { voice.stop() },
                        accent = MathraColors.NeonPink, pulse = true, size = 40.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                MathraRoundIconButton(
                    icon = if (autoSpeak) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                    onClick = {
                        autoSpeak = !autoSpeak
                        if (!autoSpeak) voice.stop()
                    },
                    accent = if (autoSpeak) MathraColors.NeonCyan else MathraColors.TextMuted,
                    filled = autoSpeak, size = 40.dp
                )
            }

            // ---------- Sinhala voice missing banner ----------
            if (voice.sinhalaMissing && !bannerDismissed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MathraColors.Gold.copy(alpha = 0.15f))
                        .border(1.dp, MathraColors.Gold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "සිංහල හඬ මේ ෆෝන් එකේ install වෙලා නෑ. Sinhala voice is not installed on this phone.",
                        color = MathraColors.TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    MathraPillButton(text = "Install", icon = Icons.AutoMirrored.Rounded.VolumeUp, accent = MathraColors.Gold, onClick = {
                        try {
                            context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                        } catch (_: Exception) {
                            try {
                                context.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
                            } catch (_: Exception) {
                                Toast.makeText(context, "Settings > Languages > Text-to-speech", Toast.LENGTH_LONG).show()
                            }
                        }
                    })
                    Spacer(Modifier.width(6.dp))
                    MathraPillButton(text = "OK", icon = Icons.Rounded.Stop, accent = MathraColors.TextMuted, onClick = { bannerDismissed = true })
                }
            }

            // ---------- messages ----------
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(msg = msg, onSpeak = { voice.speak(msg.text) }, context = context)
                }
                if (loading) {
                    item(key = "typing") { TypingIndicator() }
                }
            }

            // ---------- quick suggestions ----------
            if (messages.size <= 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ඔයාව හැදුවේ කවුද?",
                        "Who made you?",
                        "Contact creator",
                        "ඔයාට සිංහලෙන් කතා කරන්න පුළුවන්ද?"
                    ).forEach { s ->
                        MathraPillButton(text = s, icon = Icons.Rounded.Mic, accent = MathraColors.NeonPurple, onClick = { send(s) })
                    }
                }
            }

            // ---------- input bar ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MathraPillButton(
                    text = if (sinhalaMic) "සිං" else "EN",
                    icon = Icons.Rounded.Mic,
                    accent = MathraColors.NeonCyan,
                    onClick = { sinhalaMic = !sinhalaMic }
                )
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.dp, MathraColors.NeonBlue.copy(alpha = 0.6f), RoundedCornerShape(26.dp)),
                    placeholder = { Text("පණිවිඩයක් ලියන්න... / Type a message...", color = MathraColors.TextMuted, fontSize = 13.sp) },
                    maxLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MathraColors.Card,
                        unfocusedContainerColor = MathraColors.Card,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MathraColors.TextPrimary,
                        unfocusedTextColor = MathraColors.TextPrimary,
                        cursorColor = MathraColors.NeonCyan
                    )
                )
                Spacer(Modifier.width(8.dp))
                if (input.isBlank()) {
                    MathraRoundIconButton(icon = Icons.Rounded.Mic, onClick = { startMic() }, accent = MathraColors.NeonPink, filled = true, size = 50.dp, pulse = true)
                } else {
                    MathraRoundIconButton(icon = Icons.AutoMirrored.Rounded.Send, onClick = { send(input) }, accent = MathraColors.NeonBlue, filled = true, size = 50.dp)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMsg, onSpeak: () -> Unit, context: Context) {
    val fromUser = msg.fromUser
    val shape = RoundedCornerShape(
        topStart = 20.dp, topEnd = 20.dp,
        bottomStart = if (fromUser) 20.dp else 4.dp,
        bottomEnd = if (fromUser) 4.dp else 20.dp
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .staggeredEntry(0),
        horizontalAlignment = if (fromUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(
                    if (fromUser) Brush.linearGradient(listOf(MathraColors.NeonBlue, MathraColors.NeonPurple))
                    else Brush.linearGradient(listOf(MathraColors.Card, MathraColors.Card))
                )
                .border(
                    1.dp,
                    if (fromUser) Color.Transparent else (if (msg.isError) MathraColors.NeonPink else MathraColors.NeonBlue).copy(alpha = 0.5f),
                    shape
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = msg.text.replace("**", ""),
                color = MathraColors.TextPrimary,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }
        if (!fromUser) {
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MathraRoundIconButton(icon = Icons.AutoMirrored.Rounded.VolumeUp, onClick = onSpeak, accent = MathraColors.NeonCyan, size = 32.dp)
                MathraRoundIconButton(icon = Icons.Rounded.ContentCopy, onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("Mathra", msg.text))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }, accent = MathraColors.NeonPurple, size = 32.dp)
            }
            if (msg.actions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    msg.actions.forEach { a ->
                        when (a.kind) {
                            "call" -> MathraPillButton(a.label, Icons.Rounded.Call, { dialNumber(context, a.value) }, accent = MathraColors.NeonCyan)
                            "tiktok" -> MathraPillButton(a.label, Icons.Rounded.MusicNote, { openLink(context, a.value) }, accent = MathraColors.NeonPink)
                            else -> MathraPillButton(a.label, Icons.Rounded.Code, { openLink(context, a.value) }, accent = MathraColors.NeonPurple)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    val t = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MathraColors.Card)
            .border(1.dp, MathraColors.NeonBlue.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until 3) {
            val y by t.animateFloat(
                initialValue = 0f, targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    tween(450), RepeatMode.Reverse, initialStartOffset = StartOffset(i * 150)
                ),
                label = "dot$i"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { translationY = y }
                    .clip(CircleShape)
                    .background(MathraColors.NeonCyan)
            )
        }
    }
}
