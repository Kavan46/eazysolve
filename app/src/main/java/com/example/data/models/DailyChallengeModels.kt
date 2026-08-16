package com.example.data.models

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyChallengeInfo(
    val dateKey: String, // "yyyy-MM-dd"
    val dateDisplay: String, // e.g. "Sunday, Aug 16"
    val gameType: GameType,
    val title: String,
    val description: String,
    val constraintDescription: String,
    val targetLevel: Int,
    val bonusCoins: Int = 100,
    val bonusXp: Int = 50,
    val targetScoreOrTimeSeconds: Int = 90
)

object DailyChallengeGenerator {

    private val challengeConstraints = listOf(
        "⚡ Speed Blitz: Solve the puzzle in under 90 seconds!",
        "🧠 Master Logic: Clear the level with 0 hints used.",
        "✨ Triple Star: Achieve a 3-star rating on this level.",
        "🎯 Precision Mind: Finish without making more than 1 mistake.",
        "🔥 Brain Sprint: Clear in under 60 seconds for extra bonus!"
    )

    fun getChallengeForDate(calendar: Calendar = Calendar.getInstance()): DailyChallengeInfo {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        
        val dateKey = dateFormat.format(calendar.time)
        val dateDisplay = displayFormat.format(calendar.time)

        // Deterministic daily hash from the date string
        val hash = dateKey.hashCode().let { if (it < 0) -it else it }
        val allGames = GameCatalog.games
        val gameIndex = hash % allGames.size
        val selectedGame = allGames[gameIndex]
        val constraintIndex = (hash / 11) % challengeConstraints.size
        val constraint = challengeConstraints[constraintIndex]

        val targetLevel = (hash % 3) + 1 // levels 1 to 3 for daily
        val targetSeconds = when (hash % 3) {
            0 -> 60
            1 -> 90
            else -> 120
        }

        return DailyChallengeInfo(
            dateKey = dateKey,
            dateDisplay = dateDisplay,
            gameType = selectedGame.type,
            title = "DAILY ${selectedGame.name.uppercase()}",
            description = "Today's special featured puzzle. Solve to extend your streak and claim bonus coins!",
            constraintDescription = constraint,
            targetLevel = targetLevel,
            bonusCoins = 100,
            bonusXp = 50,
            targetScoreOrTimeSeconds = targetSeconds
        )
    }

    fun getWeekCalendarDays(todayCalendar: Calendar = Calendar.getInstance()): List<DailyCalendarDay> {
        val days = mutableListOf<DailyCalendarDay>()
        val cal = todayCalendar.clone() as Calendar
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, ...
        // Move to Monday of current week
        val daysFromMonday = (dayOfWeek - Calendar.MONDAY + 7) % 7
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayKey = dateFormat.format(todayCalendar.time)

        for (i in 0..6) {
            val dateKey = dateFormat.format(cal.time)
            val dayNumber = cal.get(Calendar.DAY_OF_MONTH)
            val isToday = dateKey == todayKey
            val isPast = cal.before(todayCalendar) && !isToday

            days.add(
                DailyCalendarDay(
                    dayLabel = dayNames[i],
                    dayNumber = dayNumber,
                    dateKey = dateKey,
                    isToday = isToday,
                    isPast = isPast
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return days
    }
}

data class DailyCalendarDay(
    val dayLabel: String,
    val dayNumber: Int,
    val dateKey: String,
    val isToday: Boolean,
    val isPast: Boolean
)
