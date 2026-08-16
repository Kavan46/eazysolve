package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.models.DailyChallengeGenerator
import com.example.data.models.GameCatalog
import com.example.data.models.GameType
import com.example.ui.components.CoinBadge
import com.example.ui.components.StreakBadge
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onPlayDailyGame: (GameType) -> Unit
) {
    val userStats by viewModel.userStats.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val todayDaily by viewModel.todayDailyChallenge.collectAsState()
    val isTodayDailyCompleted by viewModel.isTodayDailyCompleted.collectAsState()
    val extendedColors = LocalExtendedColors.current

    val streakDays = userStats?.currentStreak ?: 1
    val weekCalendarDays = remember(todayDaily.dateKey) {
        DailyChallengeGenerator.getWeekCalendarDays()
    }

    val selectedGameInfo = remember(todayDaily.gameType) {
        GameCatalog.games.find { it.type == todayDaily.gameType } ?: GameCatalog.games.first()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, extendedColors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = extendedColors.cardBackground,
                            border = BorderStroke(1.dp, extendedColors.subtleBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(onClick = {
                                HapticManager.playLightTap()
                                SoundManager.playTap()
                                onNavigateBack()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "DAILY CHALLENGE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    CoinBadge(coins = userStats?.coins ?: 100)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Streak Flame Hero
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.cardBackground,
                    border = BorderStroke(
                        1.5.dp,
                        if (isTodayDailyCompleted) AccentEmerald.copy(alpha = 0.5f) else AccentOrangeBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = if (isTodayDailyCompleted) AccentEmerald.copy(alpha = 0.15f) else AccentOrangeBg,
                            border = BorderStroke(
                                1.dp,
                                if (isTodayDailyCompleted) AccentEmerald else AccentOrangeBorder
                            ),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (isTodayDailyCompleted) "🌟" else "🔥", fontSize = 34.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isTodayDailyCompleted) "$streakDays DAY STREAK • COMPLETED! 🎉" else "$streakDays DAY STREAK ACTIVE",
                            color = if (isTodayDailyCompleted) AccentEmerald else AccentOrange,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isTodayDailyCompleted)
                                "You crushed today's puzzle! Streak is secured for today."
                            else
                                "Solve today's featured puzzle constraint to keep the flame alive!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Week Calendar Track
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, extendedColors.subtleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                weekCalendarDays.forEach { day ->
                                    val isCompleted = appSettings.completedDailyDates.contains(day.dateKey)
                                    val isToday = day.isToday

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = day.dayLabel,
                                            color = when {
                                                isToday -> PrimaryIndigo
                                                isCompleted -> AccentEmerald
                                                else -> extendedColors.textMuted
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isToday) FontWeight.Black else FontWeight.Medium
                                        )
                                        Text(
                                            text = "${day.dayNumber}",
                                            color = if (isToday) MaterialTheme.colorScheme.onSurface else extendedColors.textMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = when {
                                                isCompleted -> AccentEmerald
                                                isToday -> if (isTodayDailyCompleted) AccentEmerald else PrimaryIndigo
                                                day.isPast -> extendedColors.cardBorder
                                                else -> extendedColors.subtleBorder
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isCompleted) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = "Completed",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                } else if (isToday) {
                                                    Text("⭐", fontSize = 12.sp)
                                                } else if (day.isPast) {
                                                    Text("•", color = extendedColors.textMuted, fontSize = 16.sp)
                                                } else {
                                                    Icon(
                                                        Icons.Default.Lock,
                                                        contentDescription = "Locked",
                                                        tint = extendedColors.textMuted,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Today's Featured Challenge Card
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.cardBackground,
                    border = BorderStroke(
                        1.5.dp,
                        if (isTodayDailyCompleted) AccentEmerald.copy(alpha = 0.5f) else extendedColors.cardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TODAY • ${todayDaily.dateDisplay.uppercase()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp,
                                color = PrimaryIndigo
                            )
                            Surface(
                                shape = CircleShape,
                                color = if (isTodayDailyCompleted) AccentEmeraldBg else AccentAmberBg,
                                border = BorderStroke(
                                    1.dp,
                                    if (isTodayDailyCompleted) AccentEmeraldBorder else AccentAmberBorder
                                )
                            ) {
                                Text(
                                    text = if (isTodayDailyCompleted) "CLAIMED ⭐" else "+${todayDaily.bonusCoins} 🪙 +${todayDaily.bonusXp} XP",
                                    color = if (isTodayDailyCompleted) AccentEmerald else AccentAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isTodayDailyCompleted) AccentEmeraldBg else PrimaryIndigo.copy(alpha = 0.12f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isTodayDailyCompleted) AccentEmeraldBorder else PrimaryIndigo.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(selectedGameInfo.symbolEmoji, fontSize = 32.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = todayDaily.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = selectedGameInfo.category.name + " • Level ${todayDaily.targetLevel}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = todayDaily.constraintDescription,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Daily Challenge Constraint Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, extendedColors.subtleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    if (isTodayDailyCompleted) Icons.Default.Check else Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = if (isTodayDailyCompleted) AccentEmerald else AccentAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isTodayDailyCompleted) "Challenge Completed!" else "Constraint Rule",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isTodayDailyCompleted)
                                            "You cleared Level ${todayDaily.targetLevel} of ${selectedGameInfo.name} with flying colors!"
                                        else
                                            "Complete level with target requirements to unlock the Daily Master badge.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                HapticManager.playTap()
                                SoundManager.playTap()
                                viewModel.launchGame(todayDaily.gameType, todayDaily.targetLevel, isDaily = true)
                                onPlayDailyGame(todayDaily.gameType)
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTodayDailyCompleted) AccentEmerald else PrimaryIndigo
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("play_daily_btn")
                        ) {
                            Text(
                                text = if (isTodayDailyCompleted) "Replay Daily Puzzle (Practice)" else "Start Today's Challenge (+100 🪙)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

