package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_progress")
data class GameProgressEntity(
    @PrimaryKey val gameKey: String,
    val currentLevel: Int = 1,
    val highestLevel: Int = 1,
    val totalSolved: Int = 0,
    val bestScore: Int = 0,
    val bestTimeSeconds: Int = 0,
    val totalStars: Int = 0,
    val currentStreak: Int = 0,
    val isFavorite: Boolean = false,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 250, // Starting bonus
    val xp: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalLevelsCompleted: Int = 0,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val lastActiveDate: String = "",
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val dailyChallengeCompletedDate: String = "",
    val weeklyDayIndexCompletedMask: Int = 0 // Bitmask 0..6
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val isUnlocked: Boolean = false,
    val rewardCoins: Int = 50,
    val isRewardClaimed: Boolean = false,
    val iconEmoji: String = "🏆"
)
