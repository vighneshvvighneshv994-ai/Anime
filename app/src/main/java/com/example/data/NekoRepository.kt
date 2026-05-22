package com.example.data

import android.content.Context
import androidx.room.Room
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

// ==========================================
// 1. GEMINI MOSHI DATA STRUCTURES
// ==========================================

class GeminiResponse {
    var candidates: List<GeminiCandidate>? = null
}

class GeminiCandidate {
    var content: GeminiContent? = null
}

class GeminiContent {
    var parts: List<GeminiPart>? = null
}

class GeminiPart {
    var text: String? = null
}

// ==========================================
// 2. NEKO REPOSITORY WITH INTEGRATIONS
// ==========================================

class NekoRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        NekoDatabase::class.java,
        "nekostream.db"
    ).build()

    val dao = db.dao

    // Moshi Setup
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    init {
        // Prepopulate with rich anime catalogs on first launch
        CoroutineScope(Dispatchers.IO).launch {
            prepopulateIfEmpty()
        }
    }

    private suspend fun prepopulateIfEmpty() {
        val currentAnimes = dao.getAllAnimesFlow().first()
        if (currentAnimes.isEmpty()) {
            val sampleAnimes = listOf(
                Anime(
                    id = "demon_slayer",
                    title = "Demon Slayer: Flame Legacy",
                    description = "Tanjiro fights fierce demons to rescue and cure Nezuko. After the battle in the Train, a new threat arises in the forbidden temple, challenging his Sun Breathing powers to their absolute boiling point.",
                    rating = 4.9,
                    genres = "Action, Shonen, Fantasy, Supernatural",
                    bannerIndex = 0,
                    posterIndex = 0,
                    category = "Trending",
                    likes = 2450,
                    views = 125000
                ),
                Anime(
                    id = "cyberpunk",
                    title = "Cyberpunk: Neon Rebellion",
                    description = "In the corrupt, dark future streets of Neo-Shibuya, a young tech-scavenger interfaces with an outlaw military cyberdeck, triggering a massive gang war with deep-state mega-corporations.",
                    rating = 4.8,
                    genres = "Sci-Fi, Cyberpunk, Action, Mature",
                    bannerIndex = 1,
                    posterIndex = 1,
                    category = "Trending",
                    likes = 1890,
                    views = 98700
                ),
                Anime(
                    id = "jjk",
                    title = "Jujutsu Kaisen: Cursed Domain",
                    description = "Yuji Itadori enters Jujutsu High to fight against cursed energy. When high-grade cursed items are scattered across Kyoto, Gojo leads the sorcerers into a deadly mind-bending domain barrier.",
                    rating = 4.9,
                    genres = "Supernatural, Action, Shonen, Dark Fantasy",
                    bannerIndex = 2,
                    posterIndex = 2,
                    category = "Popular",
                    likes = 3120,
                    views = 184000
                ),
                Anime(
                    id = "neko_academy",
                    title = "Neko Academy: Slice of Life",
                    description = "Welcome to the elite Neko Academy, where cute half-cat girls learn to navigate high school, culinary arts, romance, and an abundance of cozy daily occurrences under cherry blossoms.",
                    rating = 4.4,
                    genres = "Slice of Life, Comedy, Romance, Cute",
                    bannerIndex = 3,
                    posterIndex = 3,
                    category = "Recently Added",
                    likes = 840,
                    views = 42200
                ),
                Anime(
                    id = "chainsaw_man",
                    title = "Chainsaw Man: Blood Lust",
                    description = "Denji, a poor teenager, merges with his pet chainsaw devil Pochita, becoming Chainsaw Man and leading public safety devil hunter forces in violent, high-octane city showdowns.",
                    rating = 4.7,
                    genres = "Action, Dark Fantasy, Shonen, Mature",
                    bannerIndex = 4,
                    posterIndex = 4,
                    category = "Popular",
                    likes = 2900,
                    views = 153000
                ),
                Anime(
                    id = "sololev",
                    title = "Solo Leveling: Shadow Monarch",
                    description = "The weakest hunter of mankind, Sung Jinwoo, is left to die in a double dungeon. When he returns with a mysterious leveling interface, he rises through ranks to command a massive legion of shadow soldiers.",
                    rating = 4.9,
                    genres = "Action, Fantasy, System, Overpowered",
                    bannerIndex = 5,
                    posterIndex = 5,
                    category = "Recently Added",
                    likes = 3500,
                    views = 210000
                )
            )

            for (anime in sampleAnimes) {
                dao.insertAnime(anime)
            }

            // Populate episodes for key animes
            val episodes = mutableListOf<Episode>()
            val sampleTitles = listOf(
                "The Awakening Flame",
                "Dark Domain Resonance",
                "Chasing the Crimson Skyline",
                "Cherry Blossoms & Cat Ears",
                "Shadow Gate Unleashed",
                "Final Resonance Overdrive"
            )

            val animeList = listOf("demon_slayer", "cyberpunk", "jjk", "neko_academy", "chainsaw_man", "sololev")
            for (animeId in animeList) {
                for (epNum in 1..6) {
                    val title = if (epNum - 1 < sampleTitles.size) sampleTitles[epNum - 1] else "Episode $epNum: Uncharted Territory"
                    episodes.add(
                        Episode(
                            id = "${animeId}_ep_$epNum",
                            animeId = animeId,
                            episodeNumber = epNum,
                            title = title,
                            description = "In epic Episode $epNum, high stakes conflicts evolve. The protagonists push past their limits as unexpected revelations shake their core alignments.",
                            durationSeconds = 1440
                        )
                    )
                }
            }
            dao.insertEpisodes(episodes)

            // Inject some default comments
            val commentBase = commentList()
            for (cm in commentBase) {
                dao.insertComment(cm)
            }

            // Populate a small Continue Watching item so the home screen has visual activity
            dao.insertContinueWatching(
                ContinueWatching(
                    animeId = "demon_slayer",
                    episodeNumber = 1,
                    progressSeconds = 480, // 8 mins in
                    durationSeconds = 1440
                )
            )
        }
    }

    private fun commentList(): List<Comment> {
        val animeList = listOf("demon_slayer", "cyberpunk", "jjk", "neko_academy", "chainsaw_man", "sololev")
        val comments = mutableListOf<Comment>()
        val reviewers = listOf("GokuFan99", "NezukoChan_cute", "KanekiKen", "LuffyKing", "SaitamaOnePunch", "AsukaLover")
        val texts = listOf(
            "This episode has the absolute highest animation quality! Breathtaking action!",
            "OMG! That fight sequence was insane. Literally jumped out of my seat!",
            "I love the neon gradients and music in this, feels extremely premium.",
            "Honestly the best show of the season. Highly recommended!",
            "Can't wait until next week, what an unbelievable cliffhanger!",
            "The story pacing is magnificent, direct adaptation of the manga at its finest."
        )

        var counter = 0
        for (animeId in animeList) {
            val epId = "${animeId}_ep_1"
            for (i in 0..2) {
                comments.add(
                    Comment(
                        id = "comment_$counter",
                        episodeId = epId,
                        username = reviewers[(i + counter) % reviewers.size],
                        text = texts[(i + counter) % texts.size],
                        timestamp = System.currentTimeMillis() - (3600000 * i)
                    )
                )
                counter++
            }
        }
        return comments
    }

    // ==========================================
    // 3. GEMINI AI RECOMMENDATIONS AGENT CODE
    // ==========================================

    suspend fun fetchAiRecommendations(userQuery: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Graceful fallback to rich static rule-based local recommendation agent
            delay(1500)
            return@withContext getLocalAiRecommendation(userQuery)
        }

        val systemInstruction = """
            You are NekoAI, the helpful, cute, anime-loving mascot recommendations agent of NekoStream app.
            Your task is to recommend anime shows to the user based on their preferences, emotional vibe, or query.
            Always keep your tone enthusiastic, cute (using expressions like 'Nya!', 'pawsome', or 'Kawaii!'), and cinematic.
            Recommend from our available list first, describing them dramatically:
            - 'Demon Slayer: Flame Legacy' (Action, Shonen, Fantasy)
            - 'Cyberpunk: Neon Rebellion' (Sci-Fi, Cyberpunk, Action)
            - 'Jujutsu Kaisen: Cursed Domain' (Supernatural, Action)
            - 'Neko Academy: Slice of Life' (Slice of Life, Comedy, Romance)
            - 'Chainsaw Man: Blood Lust' (Dark Fantasy, Action)
            - 'Solo Leveling: Shadow Monarch' (Action, Overpowered, System Fantasy)

            Formatting: Format with clean bullet points. Keep it professional yet charmingly anime-themed.
        """.trimIndent()

        // Create the JSON request body
        val escapedQuery = userQuery.replace("\"", "\\\"").replace("\n", "\\n")
        val escapedSys = systemInstruction.replace("\"", "\\\"").replace("\n", "\\n")

        val requestJson = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "The user is asking: $escapedQuery"
                    }
                  ]
                }
              ],
              "generationConfig": {
                "temperature": 0.7
              },
              "systemInstruction": {
                "parts": [
                  {
                    "text": "$escapedSys"
                  }
                ]
              }
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestJson.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Nya! NekoAI got a server response code of ${response.code}. Let me give you a custom pawsome recommendation anyway!\n\nCheck out **Demon Slayer: Flame Legacy** (epic elements!) or **Neko Academy**!"
                }
                val bodyStr = response.body?.string() ?: ""
                val adapter = moshi.adapter(GeminiResponse::class.java)
                val gemResponse = adapter.fromJson(bodyStr)
                gemResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Nya! I scanned the nebula but couldn't decode the reply! Try asking NekoAI again about your favorite genres."
            }
        } catch (e: Exception) {
            "Nya! NekoAI faced a cosmic error connectivity issue: ${e.localizedMessage}. Let me give you a custom pawsome recommendation anyway! " +
                    "\n\nIf you want action, check out **Demon Slayer: Flame Legacy** or **Jujutsu Kaisen**! For a cozy experience, **Neko Academy** is pure comfy bliss! Nya~"
        }
    }

    private fun getLocalAiRecommendation(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("action") || q.contains("fight") || q.contains("shonen") -> {
                "Nya~! I detect a burning desire for pure action! 🔥 I highly recommend **Demon Slayer: Flame Legacy**! The swordplay and breathing effects are breathtaking! If you want darker domain curses, check out **Jujutsu Kaisen: Cursed Domain** right now! Pawsome battles await you! Nya~"
            }
            q.contains("cyber") || q.contains("sci-fi") || q.contains("future") || q.contains("tech") -> {
                "Beep boop! 🌌 NekoAI recommends plugging into **Cyberpunk: Neon Rebellion**! In Neo-Shibuya, the high-tech, low-life vibes are extremely premium. The sound track and neon glows are super cinematic! Nya!"
            }
            q.contains("cute") || q.contains("comfy") || q.contains("romance") || q.contains("cozy") || q.contains("slice") -> {
                "Aww! 🌸 Deep in your heart, you want some warm, fluffy comfort! NekoAI warmly presents **Neko Academy: Slice of Life**! Hang out with cherry blossoms, cute cat-ear highschoolers, and delicious fluffy pastries. It's absolute sweet healing! Nya~"
            }
            q.contains("dark") || q.contains("scary") || q.contains("adult") || q.contains("gore") || q.contains("demon") -> {
                "Ooh, spooky... 💀 You have an appetite for gritty, jaw-dropping dark narratives! I recommend **Chainsaw Man: Blood Lust** for intense psychological elements and insane chainsaw shredding! Or feel the ultimate power in **Solo Leveling: Shadow Monarch**! Arise, your shadow army is waiting! Nya~"
            }
            else -> {
                "Nya!! Welcome! I am NekoAI, your friendly anime recommendations guide! 🐾✨ Based on your prompt, here is a special custom list: \n\n" +
                        "1. **Solo Leveling: Shadow Monarch** — Perfect for fans of leveling up to overpower enemies!\n" +
                        "2. **Demon Slayer: Flame Legacy** — For emotional, top-tier gorgeous shonen animation!\n" +
                        "3. **Neko Academy** — If you just want to cozy up and eat cherry-blossom cookies! 🐱🍪\n\n" +
                        "What genres are you craving today? Action, Sci-Fi, supernatural, or slice of life? Nya~"
            }
        }
    }

    // ==========================================
    // 4. OFFLINE DOWNLOADS SIMULATOR WITH COROUTINES
    // ==========================================

    fun startSimulatedDownload(episodeId: String, animeId: String, animeTitle: String, episodeNumber: Int, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            // Initializing download card state
            dao.insertDownload(
                Download(
                    episodeId = episodeId,
                    animeId = animeId,
                    animeTitle = animeTitle,
                    episodeNumber = episodeNumber,
                    progress = 0.05f,
                    status = "Downloading"
                )
            )

            // Dynamic tracking loop
            var currentProgress = 0.05f
            while (currentProgress < 1.0f) {
                delay(1200) // update every 1.2s
                currentProgress += (0.1f + (Math.random().toFloat() * 0.1f)) // add random chunk between 10-20%
                if (currentProgress > 1.0f) currentProgress = 1.0f

                val isStillRegistered = dao.getDownloadsFlow().first().any { it.episodeId == episodeId }
                if (!isStillRegistered) {
                    // Canceled or deleted in mid-run
                    return@launch
                }

                dao.insertDownload(
                    Download(
                        episodeId = episodeId,
                        animeId = animeId,
                        animeTitle = animeTitle,
                        episodeNumber = episodeNumber,
                        progress = currentProgress,
                        status = if (currentProgress >= 1.0f) "Completed" else "Downloading"
                    )
                )
            }
        }
    }
}
