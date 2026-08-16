package com.example.data.repository

import com.example.data.db.*
import com.example.data.models.GameCatalog
import com.example.data.models.GameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GameRepository(private val db: AppDatabase) {
    private val progressDao = db.gameProgressDao()
    private val statsDao = db.userStatsDao()
    private val achievementDao = db.achievementDao()

    val allGameProgress: Flow<List<GameProgressEntity>> = progressDao.getAllProgress()
    val userStats: Flow<UserStatsEntity?> = statsDao.getUserStatsFlow()
    val achievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    suspend fun initializeDefaults() = withContext(Dispatchers.IO) {
        // Initialize user stats if absent
        val existingStats = statsDao.getUserStats()
        val todayStr = getCurrentDateString()
        if (existingStats == null) {
            statsDao.insertOrUpdate(
                UserStatsEntity(
                    id = 1,
                    coins = 300,
                    xp = 50,
                    totalGamesPlayed = 0,
                    totalLevelsCompleted = 0,
                    currentStreak = 1,
                    longestStreak = 1,
                    lastActiveDate = todayStr,
                    soundEnabled = true,
                    musicEnabled = true,
                    notificationsEnabled = true
                )
            )
        } else {
            // Check streak continuity
            updateStreakIfNeeded(existingStats, todayStr)
        }

        // Initialize progress for all games if missing
        GameCatalog.games.forEach { game ->
            val existing = progressDao.getProgressByKey(game.type.key)
            if (existing == null) {
                progressDao.insertOrUpdate(
                    GameProgressEntity(
                        gameKey = game.type.key,
                        currentLevel = 1,
                        highestLevel = 1,
                        totalSolved = 0,
                        bestScore = 0,
                        bestTimeSeconds = 0,
                        totalStars = 0,
                        currentStreak = 1
                    )
                )
            }
        }

        // Initialize default achievements
        val initialAchievements = listOf(
            AchievementEntity("first_solve", "First Solve", "Complete your very first puzzle level.", 1, 0, false, 50, false, "🌱"),
            AchievementEntity("brain_starter", "Brain Starter", "Complete 10 levels across any games.", 10, 0, false, 100, false, "🧠"),
            AchievementEntity("puzzle_master", "Puzzle Master", "Complete 50 levels.", 50, 0, false, 250, false, "🏆"),
            AchievementEntity("speed_solver", "Speed Demon", "Solve any puzzle in under 45 seconds.", 1, 0, false, 75, false, "⚡"),
            AchievementEntity("streak_3", "Streak Seeker", "Maintain a 3-day playing streak.", 3, 0, false, 100, false, "🔥"),
            AchievementEntity("streak_7", "Weekly Champion", "Maintain a 7-day playing streak.", 7, 0, false, 200, false, "👑"),
            AchievementEntity("coin_collector", "Treasure Hunter", "Amass 500 total coins in your bank.", 500, 300, false, 150, false, "💰"),
            AchievementEntity("game_explorer", "Omni Solver", "Play at least 6 different puzzle games.", 6, 0, false, 150, false, "🗺️")
        )
        achievementDao.insertAll(initialAchievements)
    }

    private suspend fun updateStreakIfNeeded(stats: UserStatsEntity, todayStr: String) {
        val lastDate = stats.lastActiveDate
        if (lastDate.isEmpty()) {
            statsDao.insertOrUpdate(stats.copy(lastActiveDate = todayStr, currentStreak = 1))
            return
        }
        if (lastDate == todayStr) return // Already active today

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val last = try { sdf.parse(lastDate) } catch (_: Exception) { null } ?: return
        val now = try { sdf.parse(todayStr) } catch (_: Exception) { null } ?: return
        val diffDays = ((now.time - last.time) / (1000 * 60 * 60 * 24)).toInt()

        val newStreak = if (diffDays == 1) stats.currentStreak + 1 else 1
        val newLongest = maxOf(stats.longestStreak, newStreak)
        statsDao.insertOrUpdate(
            stats.copy(
                lastActiveDate = todayStr,
                currentStreak = newStreak,
                longestStreak = newLongest
            )
        )
    }

    suspend fun recordLevelCompleted(
        gameType: GameType,
        level: Int,
        score: Int,
        timeSeconds: Int,
        stars: Int
    ) = withContext(Dispatchers.IO) {
        val gameKey = gameType.key
        val existing = progressDao.getProgressByKey(gameKey) ?: GameProgressEntity(gameKey = gameKey)
        val newHighest = maxOf(existing.highestLevel, level + 1)
        val newCurrent = level + 1
        val newBestScore = maxOf(existing.bestScore, score)
        val newBestTime = if (existing.bestTimeSeconds == 0) timeSeconds else minOf(existing.bestTimeSeconds, timeSeconds)
        val newSolved = existing.totalSolved + 1
        val newStars = existing.totalStars + stars

        progressDao.insertOrUpdate(
            existing.copy(
                currentLevel = newCurrent,
                highestLevel = newHighest,
                totalSolved = newSolved,
                bestScore = newBestScore,
                bestTimeSeconds = newBestTime,
                totalStars = newStars,
                lastPlayedTimestamp = System.currentTimeMillis()
            )
        )

        // Update User Stats
        val stats = statsDao.getUserStats() ?: UserStatsEntity(id = 1)
        val earnedCoins = 25 + (stars * 10)
        val earnedXp = 50 + (stars * 20)
        val updatedStats = stats.copy(
            coins = stats.coins + earnedCoins,
            xp = stats.xp + earnedXp,
            totalGamesPlayed = stats.totalGamesPlayed + 1,
            totalLevelsCompleted = stats.totalLevelsCompleted + 1
        )
        statsDao.insertOrUpdate(updatedStats)

        // Check Achievements
        checkAchievements(updatedStats, timeSeconds)
    }

    private suspend fun checkAchievements(stats: UserStatsEntity, latestTimeSecs: Int) {
        // 1. First solve
        updateAchievementProgress("first_solve", stats.totalLevelsCompleted)
        // 2. Brain starter
        updateAchievementProgress("brain_starter", stats.totalLevelsCompleted)
        // 3. Puzzle master
        updateAchievementProgress("puzzle_master", stats.totalLevelsCompleted)
        // 4. Speed solver
        if (latestTimeSecs in 1..45) {
            updateAchievementProgress("speed_solver", 1)
        }
        // 5. Streaks
        updateAchievementProgress("streak_3", stats.currentStreak)
        updateAchievementProgress("streak_7", stats.currentStreak)
        // 6. Coins
        updateAchievementProgress("coin_collector", stats.coins)
    }

    private suspend fun updateAchievementProgress(id: String, value: Int) {
        val a = achievementDao.getAchievement(id) ?: return
        val newCurrent = maxOf(a.currentValue, value)
        val isNowUnlocked = a.isUnlocked || (newCurrent >= a.targetValue)
        achievementDao.update(a.copy(currentValue = newCurrent, isUnlocked = isNowUnlocked))
    }

    suspend fun claimAchievementReward(id: String) = withContext(Dispatchers.IO) {
        val a = achievementDao.getAchievement(id) ?: return@withContext
        if (a.isUnlocked && !a.isRewardClaimed) {
            achievementDao.update(a.copy(isRewardClaimed = true))
            val stats = statsDao.getUserStats() ?: return@withContext
            statsDao.insertOrUpdate(stats.copy(coins = stats.coins + a.rewardCoins))
        }
    }

    suspend fun spendCoins(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val stats = statsDao.getUserStats() ?: return@withContext false
        if (stats.coins >= amount) {
            statsDao.insertOrUpdate(stats.copy(coins = stats.coins - amount))
            true
        } else {
            false
        }
    }

    suspend fun addCoins(amount: Int) = withContext(Dispatchers.IO) {
        val stats = statsDao.getUserStats() ?: return@withContext
        statsDao.insertOrUpdate(stats.copy(coins = stats.coins + amount))
    }

    suspend fun toggleFavorite(gameType: GameType) = withContext(Dispatchers.IO) {
        val existing = progressDao.getProgressByKey(gameType.key) ?: return@withContext
        progressDao.insertOrUpdate(existing.copy(isFavorite = !existing.isFavorite))
    }

    suspend fun toggleSound(enabled: Boolean) = withContext(Dispatchers.IO) {
        val stats = statsDao.getUserStats() ?: return@withContext
        statsDao.insertOrUpdate(stats.copy(soundEnabled = enabled))
    }

    suspend fun toggleMusic(enabled: Boolean) = withContext(Dispatchers.IO) {
        val stats = statsDao.getUserStats() ?: return@withContext
        statsDao.insertOrUpdate(stats.copy(musicEnabled = enabled))
    }

    suspend fun toggleNotifications(enabled: Boolean) = withContext(Dispatchers.IO) {
        val stats = statsDao.getUserStats() ?: return@withContext
        statsDao.insertOrUpdate(stats.copy(notificationsEnabled = enabled))
    }

    suspend fun markDailyChallengeCompleted() = withContext(Dispatchers.IO) {
        val stats = statsDao.getUserStats() ?: return@withContext
        val todayStr = getCurrentDateString()
        val calendar = Calendar.getInstance()
        val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0..6
        val newMask = stats.weeklyDayIndexCompletedMask or (1 shl dayOfWeek)
        statsDao.insertOrUpdate(
            stats.copy(
                dailyChallengeCompletedDate = todayStr,
                weeklyDayIndexCompletedMask = newMask,
                coins = stats.coins + 100,
                xp = stats.xp + 150
            )
        )
    }

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }
}
