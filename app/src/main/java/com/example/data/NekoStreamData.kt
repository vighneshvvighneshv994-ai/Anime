package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. ROOM ENTITIES
// ==========================================

@Entity(tableName = "animes")
data class Anime(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val rating: Double,
    val genres: String, // Comma-separated (e.g., "Action, Fantasy, Shonen")
    val bannerIndex: Int, // Refers to high-quality anime banner gradient/placeholder styling
    val posterIndex: Int, // Refers to custom poster styled shapes
    val category: String, // "Trending", "Popular", "Recently Added"
    val likes: Int = 120,
    val views: Int = 1200,
    val isCustomAdmin: Boolean = false
)

@Entity(tableName = "episodes")
data class Episode(
    @PrimaryKey val id: String,
    val animeId: String,
    val episodeNumber: Int,
    val title: String,
    val description: String,
    val durationSeconds: Int = 1440, // 24 mins standard
    val videoUrl: String = "https://example.com/stream/video.mp4"
)

@Entity(tableName = "continue_watching")
data class ContinueWatching(
    @PrimaryKey val animeId: String,
    val episodeNumber: Int,
    val progressSeconds: Int,
    val durationSeconds: Int = 1440,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val animeId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class Download(
    @PrimaryKey val episodeId: String,
    val animeId: String,
    val animeTitle: String,
    val episodeNumber: Int,
    val progress: Float, // 0.0f to 1.0f
    val status: String // "Downloading", "Completed"
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey val id: String,
    val episodeId: String,
    val username: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class History(
    @PrimaryKey val animeId: String,
    val animeTitle: String,
    val posterIndex: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// 2. ROOM DAO
// ==========================================

@Dao
interface NekoDao {
    // Anime catalog queries
    @Query("SELECT * FROM animes")
    fun getAllAnimesFlow(): Flow<List<Anime>>

    @Query("SELECT * FROM animes WHERE id = :id")
    suspend fun getAnimeById(id: String): Anime?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: Anime)

    @Delete
    suspend fun deleteAnime(anime: Anime)

    @Query("DELETE FROM animes WHERE id = :animeId")
    suspend fun deleteAnimeById(animeId: String)

    // Episode list queries
    @Query("SELECT * FROM episodes WHERE animeId = :animeId ORDER BY episodeNumber ASC")
    fun getEpisodesForAnimeFlow(animeId: String): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE animeId = :animeId ORDER BY episodeNumber ASC")
    suspend fun getEpisodesForAnime(animeId: String): List<Episode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: Episode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<Episode>)

    @Query("DELETE FROM episodes WHERE animeId = :animeId")
    suspend fun deleteEpisodesForAnime(animeId: String)

    // Favorites queries
    @Query("SELECT * FROM favorites")
    fun getFavoritesFlow(): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE animeId = :animeId")
    suspend fun deleteFavorite(animeId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE animeId = :animeId)")
    fun isFavoriteFlow(animeId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE animeId = :animeId)")
    suspend fun isFavorite(animeId: String): Boolean

    // Continue Watching queries
    @Query("SELECT * FROM continue_watching ORDER BY timestamp DESC")
    fun getContinueWatchingFlow(): Flow<List<ContinueWatching>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContinueWatching(cw: ContinueWatching)

    @Query("DELETE FROM continue_watching WHERE animeId = :animeId")
    suspend fun deleteContinueWatching(animeId: String)

    // Watch History queries
    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC")
    fun getHistoryFlow(): Flow<List<History>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: History)

    @Query("DELETE FROM watch_history WHERE animeId = :animeId")
    suspend fun deleteHistory(animeId: String)

    // Offline Episode Downloads queries
    @Query("SELECT * FROM downloads ORDER BY episodeId DESC")
    fun getDownloadsFlow(): Flow<List<Download>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: Download)

    @Query("DELETE FROM downloads WHERE episodeId = :episodeId")
    suspend fun deleteDownload(episodeId: String)

    // Comments queries
    @Query("SELECT * FROM comments WHERE episodeId = :episodeId ORDER BY timestamp DESC")
    fun getCommentsFlow(episodeId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
}

// ==========================================
// 3. ROOM DATABASE HOLDER
// ==========================================

@Database(
    entities = [
        Anime::class,
        Episode::class,
        ContinueWatching::class,
        Favorite::class,
        Download::class,
        Comment::class,
        History::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NekoDatabase : RoomDatabase() {
    abstract val dao: NekoDao
}
