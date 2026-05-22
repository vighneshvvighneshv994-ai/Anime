package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// ==========================================
// 1. DATA CLASSES FOR NAVIGATION AND CHAT
// ==========================================

data class NekoChatMsg(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "neko"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val username: String, val email: String) : AuthState()
}

// ==========================================
// 2. MAIN NEKO VIEWMODEL
// ==========================================

class NekoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NekoRepository(application)
    val dao = repository.dao

    // Auth State
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Active Navigation Screen State (Splash, AuthLogin, AuthSignup, Hub, Details, Player, Admin)
    private val _currentScreen = MutableStateFlow<String>("Splash")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Nav parameters
    private val _selectedAnimeId = MutableStateFlow<String?>(null)
    val selectedAnimeId: StateFlow<String?> = _selectedAnimeId.asStateFlow()

    private val _selectedEpisodeId = MutableStateFlow<String?>(null)
    val selectedEpisodeId: StateFlow<String?> = _selectedEpisodeId.asStateFlow()

    // Home Categorized Anime Streams
    val allAnimes: StateFlow<List<Anime>> = dao.getAllAnimesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingAnimes: StateFlow<List<Anime>> = allAnimes.map { list ->
        list.filter { it.category == "Trending" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val popularAnimes: StateFlow<List<Anime>> = allAnimes.map { list ->
        list.filter { it.category == "Popular" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAddedAnimes: StateFlow<List<Anime>> = allAnimes.map { list ->
        list.filter { it.category == "Recently Added" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites & Downloads & History Flows
    val favorites: StateFlow<List<Favorite>> = dao.getFavoritesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<Download>> = dao.getDownloadsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<History>> = dao.getHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueWatching: StateFlow<List<ContinueWatching>> = dao.getContinueWatchingFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Details Anime flow
    val selectedAnime: StateFlow<Anime?> = _selectedAnimeId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else allAnimes.map { animes -> animes.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedAnimeEpisodes: StateFlow<List<Episode>> = _selectedAnimeId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else dao.getEpisodesForAnimeFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Episode and Player position
    val selectedEpisode: StateFlow<Episode?> = combine(_selectedEpisodeId, selectedAnimeEpisodes) { epId, eps ->
        eps.find { it.id == epId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Search and Genre Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedGenreFilter = MutableStateFlow<String?>(null)
    val selectedGenreFilter = _selectedGenreFilter.asStateFlow()

    val filteredAnimes: StateFlow<List<Anime>> = combine(allAnimes, _searchQuery, _selectedGenreFilter) { list, query, genre ->
        var result = list
        if (query.isNotEmpty()) {
            result = result.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
        }
        if (genre != null && genre != "All") {
            result = result.filter { it.genres.contains(genre, ignoreCase = true) }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI chat list
    private val _aiChatMessages = MutableStateFlow<List<NekoChatMsg>>(
        listOf(
            NekoChatMsg(sender = "neko", text = "Nya! I'm NekoAI, your sparkling cinematic companion! Tell me what sparks your passion, or ask me for a pawsome custom recommendation! 🌸✨")
        )
    )
    val aiChatMessages: StateFlow<List<NekoChatMsg>> = _aiChatMessages.asStateFlow()

    private val _aiGenerating = MutableStateFlow(false)
    val aiGenerating: StateFlow<Boolean> = _aiGenerating.asStateFlow()

    // Notification Alerts Simulation list
    private val _alerts = MutableStateFlow<List<String>>(
        listOf(
            "🔔 Solo Leveling Ep 6 released: Surge of the Monarch!",
            "🔥 Jujutsu Kaisen Cursed Domain trending top #1 in Shibuya!",
            "🎬 Cyberpunk Neon Rebellion season finale available now in 4K HDR ULTRA!"
        )
    )
    val alerts: StateFlow<List<String>> = _alerts.asStateFlow()

    // ==========================================
    // ACTION HANDLERS
    // ==========================================

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun selectAnime(animeId: String) {
        _selectedAnimeId.value = animeId
        navigateTo("Details")
    }

    fun selectEpisodeAndPlay(episodeId: String) {
        _selectedEpisodeId.value = episodeId
        viewModelScope.launch {
            val ep = selectedAnimeEpisodes.value.find { it.id == episodeId }
            val anime = selectedAnime.value
            if (ep != null && anime != null) {
                // Record history Room
                dao.insertHistory(
                    History(
                        animeId = anime.id,
                        animeTitle = anime.title,
                        posterIndex = anime.posterIndex
                    )
                )
                // Default record to ContinueWatching Room
                dao.insertContinueWatching(
                    ContinueWatching(
                        animeId = anime.id,
                        episodeNumber = ep.episodeNumber,
                        progressSeconds = 0,
                        durationSeconds = ep.durationSeconds
                    )
                )
            }
        }
        navigateTo("Player")
    }

    // Guest login
    fun loginAsGuest() {
        _authState.value = AuthState.Authenticated("Guest Nya", "guest@nekostream.com")
        navigateTo("Hub")
    }

    // Full login
    fun loginUser(name: String, email: String) {
        _authState.value = AuthState.Authenticated(name, email)
        navigateTo("Hub")
    }

    fun logout() {
        _authState.value = AuthState.Unauthenticated
        navigateTo("AuthLogin")
    }

    // Favorites click
    fun toggleFavorite(animeId: String) {
        viewModelScope.launch {
            val isFav = dao.isFavorite(animeId)
            if (isFav) {
                dao.deleteFavorite(animeId)
            } else {
                dao.insertFavorite(Favorite(animeId = animeId))
            }
        }
    }

    fun checkFavoriteFlow(animeId: String): Flow<Boolean> {
        return dao.isFavoriteFlow(animeId)
    }

    // Search query
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGenreFilter(genre: String?) {
        _selectedGenreFilter.value = genre
    }

    // Submitting comment to the anime episode Room
    fun postComment(episodeId: String, text: String) {
        if (text.isBlank()) return
        val user = when (val auth = _authState.value) {
            is AuthState.Authenticated -> auth.username
            is AuthState.Unauthenticated -> "NekoOtaku"
        }
        viewModelScope.launch {
            dao.insertComment(
                Comment(
                    id = UUID.randomUUID().toString(),
                    episodeId = episodeId,
                    username = user,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun getCommentsFlow(episodeId: String): Flow<List<Comment>> {
        return dao.getCommentsFlow(episodeId)
    }

    // Video progressive state update saving
    fun saveProgress(animeId: String, episodeNumber: Int, progressSec: Int, durationSec: Int) {
        viewModelScope.launch {
            dao.insertContinueWatching(
                ContinueWatching(
                    animeId = animeId,
                    episodeNumber = episodeNumber,
                    progressSeconds = progressSec,
                    durationSeconds = durationSec,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Simulating start download with progress
    fun downloadEpisode(episodeId: String, animeId: String, animeTitle: String, episodeNumber: Int) {
        repository.startSimulatedDownload(
            episodeId = episodeId,
            animeId = animeId,
            animeTitle = animeTitle,
            episodeNumber = episodeNumber,
            scope = viewModelScope
        )
    }

    // Deleting cached episode
    fun deleteDownloadedEpisode(episodeId: String) {
        viewModelScope.launch {
            dao.deleteDownload(episodeId)
        }
    }

    // Sending Chat query to Gemini Recommendations
    fun sendAiChatQuery(query: String) {
        if (query.isBlank()) return

        val userMsg = NekoChatMsg(sender = "user", text = query)
        val currentList = _aiChatMessages.value.toMutableList()
        currentList.add(userMsg)
        _aiChatMessages.value = currentList
        _aiGenerating.value = true

        viewModelScope.launch {
            val responseText = repository.fetchAiRecommendations(query)
            val updatedList = _aiChatMessages.value.toMutableList()
            updatedList.add(NekoChatMsg(sender = "neko", text = responseText))
            _aiChatMessages.value = updatedList
            _aiGenerating.value = false
        }
    }

    fun clearChat() {
        _aiChatMessages.value = listOf(
            NekoChatMsg(sender = "neko", text = "Nya! Chat logs cleared. What cinematic journey shall NekoAI recommend next? 🌸🐾")
        )
    }

    // Admin panel catalog changes (Upload new, delete existing database indexes)
    fun adminUploadAnime(
        title: String,
        description: String,
        rating: Double,
        genres: String,
        category: String,
        episodesCount: Int
    ) {
        val uniqueId = "admin_" + title.replace(" ", "_").lowercase() + "_" + UUID.randomUUID().toString().take(4)
        val anime = Anime(
            id = uniqueId,
            title = title,
            description = description,
            rating = rating,
            genres = genres,
            bannerIndex = (Math.random() * 6).toInt(),
            posterIndex = (Math.random() * 6).toInt(),
            category = category,
            likes = 120,
            views = 980,
            isCustomAdmin = true
        )

        viewModelScope.launch {
            dao.insertAnime(anime)

            // Auto-create episodes
            val episodes = (1..episodesCount).map { num ->
                Episode(
                    id = "${uniqueId}_ep_$num",
                    animeId = uniqueId,
                    episodeNumber = num,
                    title = "Episode $num: The Rising Tide",
                    description = "A magnificent custom chapter added by your administrator. Ready to unlock high-fidelity adrenaline streams!",
                    durationSeconds = 1440
                )
            }
            dao.insertEpisodes(episodes)
        }
    }

    fun adminDeleteAnime(animeId: String) {
        viewModelScope.launch {
            dao.deleteAnimeById(animeId)
            dao.deleteEpisodesForAnime(animeId)
            dao.deleteContinueWatching(animeId)
            dao.deleteHistory(animeId)
            // Cleanup any related downloads
            val currentDownloads = downloads.value
            for (dl in currentDownloads) {
                if (dl.animeId == animeId) {
                    dao.deleteDownload(dl.episodeId)
                }
            }
        }
    }
}
