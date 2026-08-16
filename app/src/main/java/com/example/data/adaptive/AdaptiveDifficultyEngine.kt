package com.example.data.adaptive

import com.example.data.models.GameDifficulty
import com.example.data.models.GameType
import kotlin.math.max
import kotlin.math.min

enum class AdaptiveTier(
    val title: String,
    val badgeEmoji: String,
    val subtitle: String,
    val scoreMultiplier: Float,
    val timeAdjustmentSec: Int,
    val freeHintsCount: Int,
    val clueDensityFactor: Float
) {
    MASTER_FLOW(
        title = "Flow State",
        badgeEmoji = "🔥",
        subtitle = "Peak mastery! +25% Score Multiplier active",
        scoreMultiplier = 1.25f,
        timeAdjustmentSec = -10,
        freeHintsCount = 0,
        clueDensityFactor = -0.1f
    ),
    CHALLENGER(
        title = "Challenger",
        badgeEmoji = "⚡",
        subtitle = "High accuracy! +15% Score Boost",
        scoreMultiplier = 1.15f,
        timeAdjustmentSec = -5,
        freeHintsCount = 0,
        clueDensityFactor = 0.0f
    ),
    OPTIMAL(
        title = "Balanced Flow",
        badgeEmoji = "🎯",
        subtitle = "Optimal calibrated puzzle balance",
        scoreMultiplier = 1.0f,
        timeAdjustmentSec = 0,
        freeHintsCount = 0,
        clueDensityFactor = 0.0f
    ),
    SUPPORTIVE(
        title = "Supportive Assist",
        badgeEmoji = "💡",
        subtitle = "+20s extra time & +1 Free Adaptive Hint",
        scoreMultiplier = 1.0f,
        timeAdjustmentSec = 20,
        freeHintsCount = 1,
        clueDensityFactor = 0.15f
    ),
    RECOVERY(
        title = "Practice Guardian",
        badgeEmoji = "🛡️",
        subtitle = "+35s extra time & +2 Free Adaptive Hints",
        scoreMultiplier = 1.0f,
        timeAdjustmentSec = 35,
        freeHintsCount = 2,
        clueDensityFactor = 0.25f
    )
}

data class GameResultRecord(
    val gameType: GameType,
    val levelNumber: Int,
    val isWin: Boolean,
    val stars: Int,
    val timeSeconds: Int,
    val parTimeSeconds: Int = 90,
    val hintsUsed: Int = 0,
    val mistakes: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class AdaptiveEngineState(
    val isEnabled: Boolean = true,
    val currentTier: AdaptiveTier = AdaptiveTier.OPTIMAL,
    val winStreak: Int = 0,
    val lossOrRestartStreak: Int = 0,
    val recentSuccessRate: Float = 0.8f, // 0.0 to 1.0
    val averageSolveRatio: Float = 1.0f, // actualTime / parTime
    val adaptiveScore: Int = 65, // 0 to 100
    val totalAdaptiveHintsGranted: Int = 0,
    val recentHistory: List<GameResultRecord> = emptyList()
) {
    val scoreBonusPercent: Int
        get() = ((currentTier.scoreMultiplier - 1.0f) * 100).toInt()

    fun getAdjustedTimeLimit(baseSeconds: Int): Int {
        if (!isEnabled) return baseSeconds
        return (baseSeconds + currentTier.timeAdjustmentSec).coerceAtLeast(30)
    }

    fun getAdjustedScore(baseScore: Int, baseDifficultyMultiplier: Float): Int {
        if (!isEnabled) return (baseScore * baseDifficultyMultiplier).toInt()
        val totalMultiplier = baseDifficultyMultiplier * currentTier.scoreMultiplier
        return (baseScore * totalMultiplier).toInt()
    }
}

object AdaptiveDifficultyEngine {

    fun calculateNewState(
        currentState: AdaptiveEngineState,
        newRecord: GameResultRecord
    ): AdaptiveEngineState {
        val updatedHistory = (listOf(newRecord) + currentState.recentHistory).take(10)

        var newWinStreak = currentState.winStreak
        var newLossStreak = currentState.lossOrRestartStreak

        if (newRecord.isWin && newRecord.stars >= 2) {
            newWinStreak += 1
            newLossStreak = 0
        } else if (!newRecord.isWin || newRecord.stars <= 1) {
            newLossStreak += 1
            newWinStreak = max(0, newWinStreak - 1)
        }

        // Calculate metrics over last 10 games
        val totalGames = updatedHistory.size
        val wins = updatedHistory.count { it.isWin }
        val successRate = if (totalGames > 0) wins.toFloat() / totalGames else 0.8f

        val totalStars = updatedHistory.filter { it.isWin }.sumOf { it.stars }
        val avgStars = if (wins > 0) totalStars.toFloat() / wins else 2.0f

        val speedRatios = updatedHistory.filter { it.isWin }.map {
            it.timeSeconds.toFloat() / it.parTimeSeconds.coerceAtLeast(30)
        }
        val avgSpeedRatio = if (speedRatios.isNotEmpty()) speedRatios.average().toFloat() else 1.0f

        // Compute 0-100 Adaptive Mastery Score
        // Base 50 + (WinStreak * 5) - (LossStreak * 8) + (Stars * 8) + (Speed Bonus)
        var score = 50.0f
        score += (newWinStreak * 6.0f).coerceAtMost(25.0f)
        score -= (newLossStreak * 9.0f).coerceAtMost(30.0f)
        score += ((avgStars - 2.0f) * 12.0f)
        score += ((1.0f - avgSpeedRatio) * 15.0f)

        val boundedScore = score.coerceIn(10.0f, 98.0f).toInt()

        // Determine Tier
        val tier = when {
            newWinStreak >= 4 && boundedScore >= 80 && avgStars >= 2.8f -> AdaptiveTier.MASTER_FLOW
            newWinStreak >= 2 && boundedScore >= 70 -> AdaptiveTier.CHALLENGER
            newLossStreak >= 3 || boundedScore <= 35 -> AdaptiveTier.RECOVERY
            newLossStreak >= 2 || boundedScore <= 48 -> AdaptiveTier.SUPPORTIVE
            else -> AdaptiveTier.OPTIMAL
        }

        return currentState.copy(
            currentTier = tier,
            winStreak = newWinStreak,
            lossOrRestartStreak = newLossStreak,
            recentSuccessRate = successRate,
            averageSolveRatio = avgSpeedRatio,
            adaptiveScore = boundedScore,
            totalAdaptiveHintsGranted = currentState.totalAdaptiveHintsGranted + tier.freeHintsCount,
            recentHistory = updatedHistory
        )
    }

    /**
     * Compute game-specific adaptive adjustments (such as starting clues, tube count, or timer)
     */
    fun getAdaptiveLevelParameters(
        gameType: GameType,
        baseDifficulty: GameDifficulty,
        adaptiveState: AdaptiveEngineState
    ): AdaptiveLevelParams {
        if (!adaptiveState.isEnabled) {
            return AdaptiveLevelParams(
                timeLimitSeconds = baseDifficulty.timeLimitSeconds,
                freeHints = 0,
                scoreMultiplier = baseDifficulty.scoreMultiplier,
                tier = AdaptiveTier.OPTIMAL,
                hintDescription = "Standard Difficulty"
            )
        }

        val tier = adaptiveState.currentTier
        val adjustedTime = adaptiveState.getAdjustedTimeLimit(baseDifficulty.timeLimitSeconds)
        val combinedMultiplier = baseDifficulty.scoreMultiplier * tier.scoreMultiplier

        return AdaptiveLevelParams(
            timeLimitSeconds = adjustedTime,
            freeHints = tier.freeHintsCount,
            scoreMultiplier = combinedMultiplier,
            tier = tier,
            hintDescription = "${tier.badgeEmoji} ${tier.title}: ${tier.subtitle}"
        )
    }
}

data class AdaptiveLevelParams(
    val timeLimitSeconds: Int,
    val freeHints: Int,
    val scoreMultiplier: Float,
    val tier: AdaptiveTier,
    val hintDescription: String
)
