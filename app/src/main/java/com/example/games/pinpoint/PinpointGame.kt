package com.example.games.pinpoint

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.models.GameDifficulty
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentSky
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryIndigo
import kotlinx.coroutines.delay

data class PinpointQuestion(
    val clues: List<String>,
    val correctAnswer: String,
    val options: List<String>,
    val initialRevealed: Int = 1
)

object PinpointGenerator {
    private val questions = listOf(
        PinpointQuestion(
            clues = listOf("🍏 Granny Smith", "🍊 Valencia", "🍌 Cavendish", "🥭 Alphonso", "🍇 Concord"),
            correctAnswer = "FRUITS",
            options = listOf("FRUITS", "VEGETABLES", "TREES", "COCKTAILS", "CONDIMENTS", "DESSERTS")
        ),
        PinpointQuestion(
            clues = listOf("🗼 Eiffel Tower", "🏛️ Colosseum", "🏰 Neuschwanstein", "🕰️ Big Ben", "🗼 Leaning Tower"),
            correctAnswer = "EUROPEAN LANDMARKS",
            options = listOf("EUROPEAN LANDMARKS", "US MONUMENTS", "ANCIENT WONDERS", "MUSEUMS", "BRIDGES", "PARKS")
        ),
        PinpointQuestion(
            clues = listOf("🪐 Saturn", "🔴 Mars", "🔵 Neptune", "♃ Jupiter", "🌕 Venus"),
            correctAnswer = "PLANETS",
            options = listOf("PLANETS", "CONSTELLATIONS", "MOONS", "ASTEROIDS", "GALAXIES", "NEBULAS")
        ),
        PinpointQuestion(
            clues = listOf("🎸 Gibson", "🎹 Yamaha", "🎻 Stradivarius", "🥁 Pearl", "🎺 Bach"),
            correctAnswer = "MUSICAL BRANDS",
            options = listOf("MUSICAL BRANDS", "CAR BRANDS", "SOUND SYSTEMS", "COMPOSERS", "DANCE STYLES", "RECORD LABELS")
        ),
        PinpointQuestion(
            clues = listOf("☕ Espresso", "🍵 Matcha", "🧋 Boba", "🥛 Latte", "🧊 Cold Brew"),
            correctAnswer = "CAFE BEVERAGES",
            options = listOf("CAFE BEVERAGES", "DESSERTS", "BREAKFAST CEREALS", "BAKERY ITEMS", "SOUPS", "SNACKS")
        ),
        PinpointQuestion(
            clues = listOf("🦁 Lion", "🐅 Tiger", "🐆 Cheetah", "🐆 Jaguar", "🐈 Cougar"),
            correctAnswer = "BIG CATS",
            options = listOf("BIG CATS", "CANINES", "BEARS", "PRIMATES", "MARSUPIALS", "REPTILES")
        ),
        PinpointQuestion(
            clues = listOf("🗼 Tokyo", "🏰 London", "🗼 Paris", "🏛️ Rome", "🗽 New York"),
            correctAnswer = "GLOBAL MEGACITIES",
            options = listOf("GLOBAL MEGACITIES", "ANCIENT RUINS", "ISLAND NATIONS", "MOUNTAIN TOWNS", "DESERT OASES", "PORTS")
        ),
        PinpointQuestion(
            clues = listOf("⚽ Real Madrid", "⚽ Barcelona", "⚽ Liverpool", "⚽ Bayern", "⚽ Juventus"),
            correctAnswer = "FOOTBALL CLUBS",
            options = listOf("FOOTBALL CLUBS", "NBA TEAMS", "F1 TEAMS", "TENNIS TOURS", "CRICKET CLUBS", "RUGBY TEAMS")
        )
    )

    fun getQuestion(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): PinpointQuestion {
        val base = questions[(level - 1).coerceAtLeast(0) % questions.size]
        val (optionsCount, initClues) = when (difficulty) {
            GameDifficulty.EASY -> Pair(3, 2)
            GameDifficulty.MEDIUM -> Pair(4, 1)
            GameDifficulty.HARD -> Pair(5, 1)
            GameDifficulty.EXPERT -> Pair(6, 1)
        }
        val options = (listOf(base.correctAnswer) + base.options.filter { it != base.correctAnswer }.take(optionsCount - 1)).shuffled()
        return base.copy(options = options, initialRevealed = initClues)
    }
}

@Composable
fun PinpointGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val q = remember(levelNumber, difficulty) { PinpointGenerator.getQuestion(levelNumber, difficulty) }
    var revealedCluesCount by remember(levelNumber, difficulty) { mutableIntStateOf(q.initialRevealed) }
    var wrongOptions by remember(levelNumber, difficulty) { mutableStateOf(setOf<String>()) }
    var selectedAnswer by remember(levelNumber, difficulty) { mutableStateOf<String?>(null) }
    var isCorrectWon by remember(levelNumber, difficulty) { mutableStateOf(false) }
    var startTime by remember(levelNumber, difficulty) { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(onUndoRequested) {
        if (onUndoRequested) {
            selectedAnswer = null
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    LaunchedEffect(onHintRequested) {
        if (onHintRequested) {
            // Eliminate 1 wrong option
            val availableWrongs = q.options.filter { it != q.correctAnswer && !wrongOptions.contains(it) }
            if (availableWrongs.isNotEmpty()) {
                wrongOptions = wrongOptions + availableWrongs.first()
                SoundManager.playHint()
                HapticManager.playTap()
            }
            resetControlTriggers()
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            revealedCluesCount = q.initialRevealed
            wrongOptions = emptySet()
            selectedAnswer = null
            isCorrectWon = false
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    LaunchedEffect(isCorrectWon) {
        if (isCorrectWon) {
            // Smooth celebration pause before transitioning to next level celebration
            delay(700)
            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
            val multiplier = (5 - revealedCluesCount).coerceAtLeast(1)
            val stars = if (multiplier >= 3) 3 else if (multiplier == 2) 2 else 1
            val score = 350 * multiplier
            onWin(score, elapsedSec, stars)
        }
    }

    fun onSelectOption(option: String) {
        if (isCorrectWon) return
        selectedAnswer = option
        if (option == q.correctAnswer) {
            isCorrectWon = true
            SoundManager.playLevelComplete()
            HapticManager.playSuccess()
        } else {
            wrongOptions = wrongOptions + option
            SoundManager.playWrongMove()
            HapticManager.playError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Multiplier & Clue Count Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score Multiplier: ${5 - revealedCluesCount}x",
                    fontWeight = FontWeight.Bold,
                    color = AccentSky,
                    fontSize = 15.sp
                )
                Text(
                    text = "Clues: $revealedCluesCount / ${q.clues.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Clue Cards List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            q.clues.forEachIndexed { idx, clue ->
                val isRevealed = idx < revealedCluesCount
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isRevealed) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isRevealed) PrimaryIndigo.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    shadowElevation = if (isRevealed) 2.dp else 0.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isRevealed) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${idx + 1}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (isRevealed) clue else "🔒 Locked Clue",
                            fontWeight = if (isRevealed) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            color = if (isRevealed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            if (revealedCluesCount < q.clues.size) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        revealedCluesCount++
                        SoundManager.playTap()
                        HapticManager.playTap()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reveal Next Clue (-1x Multiplier)")
                }
            }
        }

        // Multiple-Choice Options Grid with Red/Green feedback
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Interactive feedback banner
            if (isCorrectWon) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentEmerald.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, AccentEmerald),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✨ Correct! Advancing to Next Level...", fontWeight = FontWeight.Bold, color = AccentEmerald, fontSize = 13.sp)
                    }
                }
            } else if (wrongOptions.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ErrorRed.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("❌ Wrong choice! Pick another category to retry", fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 12.sp)
                    }
                }
            }

            q.options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowOptions.forEach { opt ->
                        val isWrong = wrongOptions.contains(opt)
                        val isCorrect = isCorrectWon && opt == q.correctAnswer

                        val buttonBg by animateColorAsState(
                            targetValue = when {
                                isCorrect -> AccentEmerald
                                isWrong -> ErrorRed
                                else -> PrimaryIndigo
                            },
                            label = "opt_bg"
                        )

                        Button(
                            onClick = { onSelectOption(opt) },
                            enabled = !isWrong && !isCorrectWon,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("pinpoint_opt_$opt"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonBg,
                                disabledContainerColor = if (isWrong) ErrorRed.copy(alpha = 0.8f) else if (isCorrect) AccentEmerald else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = if (isWrong) "❌ $opt" else if (isCorrect) "✓ $opt" else opt,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
