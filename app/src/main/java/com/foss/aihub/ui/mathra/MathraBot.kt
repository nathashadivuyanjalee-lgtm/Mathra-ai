package com.foss.aihub.ui.mathra

import com.foss.aihub.utils.CREATOR_CONTACT
import com.foss.aihub.utils.GITHUB_PROFILE_URL
import com.foss.aihub.utils.TIKTOK_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class BotAction(val label: String, val kind: String, val value: String)

data class ChatMsg(
    val id: Long,
    val fromUser: Boolean,
    val text: String,
    val actions: List<BotAction> = emptyList(),
    val isError: Boolean = false
)

data class LocalReply(val text: String, val actions: List<BotAction>)

object MathraBot {

    // ---- Remote AI (for normal questions). Change these to use your own provider/key. ----
    private const val API_URL = "https://text.pollinations.ai/openai"
    private const val MODEL = "openai"

    private const val SYSTEM_PROMPT = """You are "Mathra Ai Chat Bot", a friendly, helpful AI assistant ("Your AI Friend").
You were created by Denuwan Akash (දෙනුවන් ආකාශ්), the owner of Mathra Quotes on TikTok.
Never say you were made by OpenAI, Google, Anthropic or anyone else; if asked who made you, say you were made by Denuwan Akash, owner of Mathra Quotes on TikTok.
Creator contact number: $CREATOR_CONTACT.
Language rule: if the user writes Sinhala (සිංහල) or Singlish (Sinhala typed in English letters), answer in natural Sinhala script. If the user writes English, answer in English.
Keep answers clear and reasonably short. Be warm and polite."""

    fun isSinhala(text: String): Boolean = text.any { it in '\u0D80'..'\u0DFF' }

    // ---------- local (always correct) answers about the creator ----------
    private val contactRx = Regex(
        "contact|phone number|mobile number|whatsapp|call him|call you|number eka|nambare|nambar|" +
            "කන්ටැක්ට්|කොන්ටැක්ට්|කන්ටැක්|කොන්ටැක්|සම්බන්ධ කර|සම්බන්ධ වෙ|ඇමතුම|වට්ස්ඇප්|" +
            "දුරකථන|දුරකතන|නම්බර්|නම්බරය|නම්බරේ|නම්බර"
    )
    private val creatorEnRx = Regex(
        "(who|whom).{0,25}(made|created|built|developed|designed|programmed|invented|owns|owner|founder|behind)|" +
            "your (creator|developer|owner|maker|founder|author|boss)|(made|created|built|developed) you"
    )
    private val creatorSiRx = Regex(
        "හැදුවේ|හැදුවෙ|හැදුව|හැදූ|හදපු|හදලා|නිර්මාණය කළ|නිර්මාතෘ|නිර්මාපක|නිර්මාණකරු|හිමිකරු|අයිතිකරු|අයිතිකාරයා|ඩෙවලපර්|ඩිවෙලපර්"
    )
    private val creatorSinglishRx = Regex(
        "kauda hadu|kawda hadu|kauruda hadu|kawuda hadu|haduwe|hadapu|hadune|oyawa hadu|mawa hadu|hadu kauda"
    )
    private val tiktokRx = Regex("tiktok|tik tok|mathra quotes|ටික්ටොක්|ටික් ටොක්|ටික්ටොක")
    private val githubRx = Regex("github|git hub|ගිට්හබ්|ගිට් හබ්|ගිතුබ්|ගිට්හබ")
    private val identityRx = Regex(
        "who are you|what is your name|what's your name|your name|ඔයා කවුද|ඔබ කවුද|ඔයාගේ නම|ඔබේ නම|නම මොකක්ද|oya kauda|oyage nama|obage nama"
    )

    private val allActions = listOf(
        BotAction("Call 0783756055", "call", CREATOR_CONTACT),
        BotAction("TikTok", "tiktok", TIKTOK_URL),
        BotAction("GitHub", "github", GITHUB_PROFILE_URL)
    )

    fun localAnswer(question: String): LocalReply? {
        val q = question.lowercase(Locale.ROOT)
        val si = isSinhala(question)
        val singlish = !si && creatorSinglishRx.containsMatchIn(q)

        fun pick(sinhala: String, english: String): String = when {
            si -> sinhala
            singlish -> sinhala + "\n\n" + english
            else -> english
        }

        return when {
            contactRx.containsMatchIn(q) -> LocalReply(
                pick(
                    "මාව හදපු දෙනුවන් ආකාශ් (Denuwan Akash) ව සම්බන්ධ කරගන්න ඕන නම් $CREATOR_CONTACT අංකයට අමතන්න. 📞\n\nTikTok: $TIKTOK_URL\nGitHub: $GITHUB_PROFILE_URL",
                    "To contact my creator Denuwan Akash, call $CREATOR_CONTACT. 📞\n\nTikTok: $TIKTOK_URL\nGitHub: $GITHUB_PROFILE_URL"
                ), allActions
            )

            creatorEnRx.containsMatchIn(q) || creatorSiRx.containsMatchIn(question) || creatorSinglishRx.containsMatchIn(q) -> LocalReply(
                pick(
                    "මාව හැදුවේ Mathra Quotes TikTok එකේ අයිතිකරු වන දෙනුවන් ආකාශ් (Denuwan Akash) විසින්. 💙",
                    "I was created by Denuwan Akash, the owner of Mathra Quotes on TikTok. 💙"
                ), allActions
            )

            tiktokRx.containsMatchIn(q) -> LocalReply(
                pick(
                    "මේක තමයි Mathra Quotes TikTok එක 🎵\n$TIKTOK_URL",
                    "Here is the Mathra Quotes TikTok page 🎵\n$TIKTOK_URL"
                ), listOf(allActions[1])
            )

            githubRx.containsMatchIn(q) -> LocalReply(
                pick(
                    "මේක තමයි දෙනුවන් ආකාශ්ගේ GitHub එක 💻\n$GITHUB_PROFILE_URL",
                    "Here is Denuwan Akash's GitHub 💻\n$GITHUB_PROFILE_URL"
                ), listOf(allActions[2])
            )

            identityRx.containsMatchIn(q) -> LocalReply(
                pick(
                    "මම Mathra Ai Chat Bot — ඔයාගේ AI යාළුවා! 🤖💙 මාව හැදුවේ Mathra Quotes TikTok එකේ අයිතිකරු දෙනුවන් ආකාශ් විසින්.",
                    "I'm Mathra Ai Chat Bot — your AI friend! 🤖💙 I was created by Denuwan Akash, the owner of Mathra Quotes on TikTok."
                ), emptyList()
            )

            else -> null
        }
    }

    fun errorText(question: String): String =
        if (isSinhala(question))
            "සමාවෙන්න, දැන් පිළිතුරක් ගන්න බැරි වුණා. ඉන්ටර්නෙට් සම්බන්ධතාවය බලලා ආයෙත් උත්සාහ කරන්න. 🙏"
        else
            "Sorry, I couldn't get an answer right now. Please check your internet connection and try again. 🙏"

    // ---------- remote answer ----------
    suspend fun askRemote(history: List<ChatMsg>): String? = withContext(Dispatchers.IO) {
        try {
            val msgs = JSONArray()
            msgs.put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
            history.filter { !it.isError }.takeLast(10).forEach {
                msgs.put(
                    JSONObject().put("role", if (it.fromUser) "user" else "assistant")
                        .put("content", it.text)
                )
            }
            val body = JSONObject().put("model", MODEL).put("messages", msgs)
            val conn = URL(API_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 45000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code !in 200..299) return@withContext null
            val text = conn.inputStream.bufferedReader().readText()
            JSONObject(text).getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content").trim().ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }

    // ---------- helpers for speech ----------
    fun cleanForSpeech(t: String): String = t
        .replace(Regex("https?://\\S+"), "")
        .replace(Regex("[*#`_>~|]"), "")
        .replace(Regex("[\\p{So}\\p{Sk}\\u200B]"), "")
        .replace(Regex("\\d{7,}")) { m -> m.value.toCharArray().joinToString(" ") }
        .replace(Regex("\\s+"), " ")
        .trim()

    /** Splits text into runs of (isSinhala, text) so each run is read with the right voice. */
    fun splitByScript(t: String): List<Pair<Boolean, String>> {
        val out = mutableListOf<Pair<Boolean, String>>()
        val sb = StringBuilder()
        var cur: Boolean? = null
        for (ch in t) {
            val kind: Boolean? = when {
                ch in '\u0D80'..'\u0DFF' -> true
                ch.isLetter() -> false
                else -> null
            }
            if (kind != null && cur != null && kind != cur) {
                out.add(cur to sb.toString().trim())
                sb.clear()
            }
            if (kind != null) cur = kind
            sb.append(ch)
        }
        if (sb.isNotBlank()) out.add((cur ?: false) to sb.toString().trim())
        return out.filter { it.second.isNotBlank() }
    }
}
