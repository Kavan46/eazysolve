package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.adaptive.AdaptiveTier
import com.example.data.models.GameDifficulty
import com.example.data.models.GameType
import com.example.ui.theme.*

@Composable
fun CoinBadge(
    coins: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    Surface(
        shape = CircleShape,
        color = extendedColors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmberBorder.copy(alpha = 0.6f)),
        modifier = modifier.clickable(enabled = onClick != null) {
            HapticManager.playLightTap()
            onClick?.invoke()
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("🪙", fontSize = 13.sp)
            Text(
                text = "$coins",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StreakBadge(
    streakDays: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    Surface(
        shape = CircleShape,
        color = extendedColors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrangeBorder.copy(alpha = 0.6f)),
        modifier = modifier.clickable(enabled = onClick != null) {
            HapticManager.playLightTap()
            onClick?.invoke()
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("🔥", fontSize = 13.sp)
            Text(
                text = "${streakDays}D",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = AccentOrange
            )
        }
    }
}

@Composable
fun GameTopBar(
    title: String,
    levelNumber: Int,
    timerSeconds: Int,
    gameType: GameType? = null,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    challengeTimerEnabled: Boolean = false,
    adaptiveTier: AdaptiveTier? = null,
    adaptiveScoreMultiplier: Float? = null,
    timeLimitOverrideSeconds: Int? = null,
    onBack: () -> Unit,
    onPause: () -> Unit,
    onHelpClick: (() -> Unit)? = null,
    onDifficultyClick: (() -> Unit)? = null
) {
    val extendedColors = LocalExtendedColors.current
    val effectiveTimeLimit = timeLimitOverrideSeconds ?: difficulty.timeLimitSeconds
    val isTimerLow = challengeTimerEnabled && (effectiveTimeLimit - timerSeconds) <= 15
    val timerColor by animateColorAsState(
        targetValue = if (isTimerLow) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "timerColor"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, extendedColors.cardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button & Game Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(
                            onClick = {
                                HapticManager.playLightTap()
                                SoundManager.playTap()
                                onBack()
                            },
                            modifier = Modifier.testTag("game_back_btn")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (gameType != null) {
                        GameIconBadge(
                            gameType = gameType,
                            size = 36.dp,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Center: Title + Level + Difficulty Badges
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        // Level Tag
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "LVL $levelNumber",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }

                        // Difficulty Pill or Adaptive Tier Pill
                        Surface(
                            shape = CircleShape,
                            color = when {
                                adaptiveTier == AdaptiveTier.MASTER_FLOW -> AccentOrangeBg
                                adaptiveTier == AdaptiveTier.CHALLENGER -> PastelAmber
                                adaptiveTier == AdaptiveTier.SUPPORTIVE -> AccentEmeraldBg
                                adaptiveTier == AdaptiveTier.RECOVERY -> PastelViolet
                                difficulty == GameDifficulty.EASY -> AccentEmeraldBg
                                difficulty == GameDifficulty.MEDIUM -> PastelAmber
                                difficulty == GameDifficulty.HARD -> PastelRose
                                else -> PastelViolet
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when {
                                    adaptiveTier == AdaptiveTier.MASTER_FLOW -> AccentOrange.copy(alpha = 0.6f)
                                    adaptiveTier == AdaptiveTier.CHALLENGER -> AccentAmber.copy(alpha = 0.6f)
                                    adaptiveTier == AdaptiveTier.SUPPORTIVE -> AccentEmerald.copy(alpha = 0.6f)
                                    adaptiveTier == AdaptiveTier.RECOVERY -> AccentViolet.copy(alpha = 0.6f)
                                    difficulty == GameDifficulty.EASY -> AccentEmerald.copy(alpha = 0.5f)
                                    difficulty == GameDifficulty.MEDIUM -> AccentAmber.copy(alpha = 0.5f)
                                    difficulty == GameDifficulty.HARD -> ErrorRed.copy(alpha = 0.5f)
                                    else -> AccentViolet.copy(alpha = 0.5f)
                                }
                            ),
                            modifier = Modifier.clickable(enabled = onDifficultyClick != null) {
                                HapticManager.playLightTap()
                                onDifficultyClick?.invoke()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = adaptiveTier?.badgeEmoji ?: difficulty.badgeEmoji,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = if (adaptiveTier != null && adaptiveTier != AdaptiveTier.OPTIMAL) {
                                        adaptiveTier.title
                                    } else {
                                        difficulty.shortName
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = when {
                                        adaptiveTier == AdaptiveTier.MASTER_FLOW -> AccentOrange
                                        adaptiveTier == AdaptiveTier.CHALLENGER -> Color(0xFFB45309)
                                        adaptiveTier == AdaptiveTier.SUPPORTIVE -> AccentEmerald
                                        adaptiveTier == AdaptiveTier.RECOVERY -> AccentViolet
                                        difficulty == GameDifficulty.EASY -> AccentEmerald
                                        difficulty == GameDifficulty.MEDIUM -> Color(0xFFB45309)
                                        difficulty == GameDifficulty.HARD -> ErrorRed
                                        else -> AccentViolet
                                    }
                                )
                            }
                        }

                        // Timer Display
                        val displaySec = if (challengeTimerEnabled) {
                            (effectiveTimeLimit - timerSeconds).coerceAtLeast(0)
                        } else {
                            timerSeconds
                        }
                        val min = displaySec / 60
                        val sec = displaySec % 60
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (challengeTimerEnabled) {
                                Text("⏳", fontSize = 10.sp)
                            }
                            Text(
                                text = String.format("%02d:%02d", min, sec),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = timerColor
                            )
                        }
                    }
                }

                // Right Actions: Help / Rules & Pause Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onHelpClick != null) {
                        Surface(
                            shape = CircleShape,
                            color = extendedColors.cardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                            modifier = Modifier.size(38.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    HapticManager.playLightTap()
                                    SoundManager.playTap()
                                    onHelpClick()
                                },
                                modifier = Modifier.testTag("game_help_btn")
                            ) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = "Rules",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = extendedColors.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(
                            onClick = {
                                HapticManager.playLightTap()
                                SoundManager.playTap()
                                onPause()
                            },
                            modifier = Modifier.testTag("game_pause_btn")
                        ) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Challenge Mode countdown bar
            if (challengeTimerEnabled) {
                val totalLimit = effectiveTimeLimit.toFloat()
                val progress = ((totalLimit - timerSeconds).coerceAtLeast(0f) / totalLimit).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = if (isTimerLow) ErrorRed else PrimaryIndigo,
                    trackColor = extendedColors.subtleBorder.copy(alpha = 0.3f),
                )
            }
        }
    }
}

@Composable
fun BottomGameControls(
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onRestart: () -> Unit,
    coins: Int,
    freeHintsAvailable: Int = 0
) {
    val extendedColors = LocalExtendedColors.current
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
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo
            OutlinedButton(
                onClick = {
                    HapticManager.playLightTap()
                    SoundManager.playTap()
                    onUndo()
                },
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = extendedColors.cardBackground,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(42.dp).testTag("btn_undo")
            ) {
                Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Undo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Hint with Free Badge if active
            Box {
                Button(
                    onClick = {
                        HapticManager.playTap()
                        SoundManager.playTap()
                        onHint()
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (freeHintsAvailable > 0) AccentEmerald else MaterialTheme.colorScheme.secondary
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    modifier = Modifier.height(42.dp).testTag("btn_hint")
                ) {
                    Text("💡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (freeHintsAvailable > 0) "Free Hint" else "Hint",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                if (freeHintsAvailable > 0) {
                    Surface(
                        shape = CircleShape,
                        color = AccentOrange,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                    ) {
                        Text(
                            text = "+$freeHintsAvailable",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Restart
            OutlinedButton(
                onClick = {
                    HapticManager.playLightTap()
                    SoundManager.playTap()
                    onRestart()
                },
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = extendedColors.cardBackground,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(42.dp).testTag("btn_restart")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restart", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onSelectDifficulty: ((GameDifficulty) -> Unit)? = null
) {
    val extendedColors = LocalExtendedColors.current
    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Game Paused",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Difficulty Selector Row in Pause Menu
                if (onSelectDifficulty != null) {
                    Text(
                        text = "DIFFICULTY LEVEL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GameDifficulty.entries.forEach { diff ->
                            val isSelected = difficulty == diff
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else extendedColors.cardBackground,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryIndigo) else androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        HapticManager.playTap()
                                        onSelectDifficulty(diff)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(diff.badgeEmoji, fontSize = 12.sp)
                                    Text(
                                        text = diff.shortName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = {
                        HapticManager.playTap()
                        onResume()
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resume Game", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSecondary)
                }

                OutlinedButton(
                    onClick = {
                        HapticManager.playLightTap()
                        onRestart()
                    },
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart Level", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        HapticManager.playTap()
                        onToggleSound()
                    },
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (soundEnabled) "Sound: ON" else "Sound: OFF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                TextButton(
                    onClick = {
                        HapticManager.playLightTap()
                        onHome()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Home", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun WinCelebrationDialog(
    score: Int,
    timeSeconds: Int,
    stars: Int,
    coinsEarned: Int,
    xpEarned: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    onShare: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val scaleAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "win_dialog_scale"
    )

    LaunchedEffect(Unit) {
        HapticManager.playSuccess()
    }

    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .scale(scaleAnim)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AccentEmeraldBg,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎉", fontSize = 30.sp)
                    }
                }

                Text(
                    text = "Level Complete!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Difficulty badge multiplier
                Surface(
                    shape = CircleShape,
                    color = when (difficulty) {
                        GameDifficulty.EASY -> AccentEmeraldBg
                        GameDifficulty.MEDIUM -> PastelAmber
                        GameDifficulty.HARD -> PastelRose
                        GameDifficulty.EXPERT -> PastelViolet
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder)
                ) {
                    Text(
                        text = "${difficulty.badgeEmoji} ${difficulty.displayName} • ${difficulty.scoreMultiplier}× Bonus",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                // Stars row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    for (i in 1..3) {
                        Text(
                            text = if (i <= stars) "⭐" else "☆",
                            fontSize = 26.sp,
                            color = if (i <= stars) StarGold else Slate300
                        )
                    }
                }

                // Stats Surface
                Surface(
                    color = extendedColors.cardBackground,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Score", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            Text("$score", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Time", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            val min = timeSeconds / 60
                            val sec = timeSeconds % 60
                            Text(String.format("%02d:%02d", min, sec), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Reward", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            Text("+$coinsEarned 🪙", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CoinGold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = {
                        HapticManager.playTap()
                        onNextLevel()
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("win_next_btn")
                ) {
                    Text("Next Level", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            HapticManager.playLightTap()
                            onShare()
                        },
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            HapticManager.playLightTap()
                            onRetry()
                        },
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(
                    onClick = {
                        HapticManager.playLightTap()
                        onHome()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
