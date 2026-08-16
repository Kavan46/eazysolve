package com.example.games.wend

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.models.GameDifficulty
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.PrimaryIndigo
import kotlin.math.abs

data class WendLevel(
    val category: String,
    val grid: List<List<Char>>,
    val targetWords: List<String>,
    val wordPositions: Map<String, List<Pair<Int, Int>>>
)

object WendGenerator {
    private val levels = listOf(
        WendLevel(
            category = "🐾 Animals",
            grid = listOf(
                listOf('C', 'A', 'T', 'X', 'B'),
                listOf('D', 'O', 'G', 'L', 'I'),
                listOf('L', 'I', 'O', 'N', 'R'),
                listOf('B', 'E', 'A', 'R', 'D'),
                listOf('F', 'R', 'O', 'G', 'S')
            ),
            targetWords = listOf("CAT", "DOG", "LION", "BEAR", "FROG", "BIRD"),
            wordPositions = mapOf(
                "CAT" to listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
                "DOG" to listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2)),
                "LION" to listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2), Pair(2, 3)),
                "BEAR" to listOf(Pair(3, 0), Pair(3, 1), Pair(3, 2), Pair(3, 3)),
                "FROG" to listOf(Pair(4, 0), Pair(4, 1), Pair(4, 2), Pair(4, 3)),
                "BIRD" to listOf(Pair(0, 4), Pair(1, 4), Pair(2, 4), Pair(3, 4))
            )
        ),
        WendLevel(
            category = "🍎 Fruits",
            grid = listOf(
                listOf('A', 'P', 'P', 'L', 'E'),
                listOf('P', 'E', 'A', 'R', 'S'),
                listOf('K', 'I', 'W', 'I', 'F'),
                listOf('M', 'A', 'N', 'G', 'O'),
                listOf('P', 'L', 'U', 'M', 'S')
            ),
            targetWords = listOf("APPLE", "PEAR", "KIWI", "MANGO", "PLUM"),
            wordPositions = mapOf(
                "APPLE" to listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3), Pair(0, 4)),
                "PEAR" to listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2), Pair(1, 3)),
                "KIWI" to listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2), Pair(2, 3)),
                "MANGO" to listOf(Pair(3, 0), Pair(3, 1), Pair(3, 2), Pair(3, 3), Pair(3, 4)),
                "PLUM" to listOf(Pair(4, 0), Pair(4, 1), Pair(4, 2), Pair(4, 3))
            )
        ),
        WendLevel(
            category = "🚀 Cosmos & Space",
            grid = listOf(
                listOf('S', 'T', 'A', 'R', 'S'),
                listOf('M', 'O', 'O', 'N', 'P'),
                listOf('M', 'A', 'R', 'S', 'A'),
                listOf('S', 'U', 'N', 'X', 'C'),
                listOf('N', 'O', 'V', 'A', 'E')
            ),
            targetWords = listOf("STAR", "MOON", "MARS", "SUN", "NOVA", "SPACE"),
            wordPositions = mapOf(
                "STAR" to listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3)),
                "MOON" to listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2), Pair(1, 3)),
                "MARS" to listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2), Pair(2, 3)),
                "SUN" to listOf(Pair(3, 0), Pair(3, 1), Pair(3, 2)),
                "NOVA" to listOf(Pair(4, 0), Pair(4, 1), Pair(4, 2), Pair(4, 3)),
                "SPACE" to listOf(Pair(0, 4), Pair(1, 4), Pair(2, 4), Pair(3, 4), Pair(4, 4))
            )
        ),
        WendLevel(
            category = "🌊 Ocean Life",
            grid = listOf(
                listOf('F', 'I', 'S', 'H', 'T'),
                listOf('S', 'E', 'A', 'L', 'U'),
                listOf('C', 'R', 'A', 'B', 'N'),
                listOf('K', 'E', 'L', 'P', 'A'),
                listOf('W', 'A', 'V', 'E', 'S')
            ),
            targetWords = listOf("FISH", "SEAL", "CRAB", "KELP", "WAVE", "TUNA"),
            wordPositions = mapOf(
                "FISH" to listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3)),
                "SEAL" to listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2), Pair(1, 3)),
                "CRAB" to listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2), Pair(2, 3)),
                "KELP" to listOf(Pair(3, 0), Pair(3, 1), Pair(3, 2), Pair(3, 3)),
                "WAVE" to listOf(Pair(4, 0), Pair(4, 1), Pair(4, 2), Pair(4, 3)),
                "TUNA" to listOf(Pair(0, 4), Pair(1, 4), Pair(2, 4), Pair(3, 4))
            )
        )
    )

    fun getLevel(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): WendLevel {
        val base = levels[(level - 1).coerceAtLeast(0) % levels.size]
        val wordsToTake = when (difficulty) {
            GameDifficulty.EASY -> 3
            GameDifficulty.MEDIUM -> 4
            GameDifficulty.HARD -> 5
            GameDifficulty.EXPERT -> base.targetWords.size
        }
        val subWords = base.targetWords.take(wordsToTake)
        return base.copy(targetWords = subWords)
    }
}

@Composable
fun WendGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val level = remember(levelNumber, difficulty) { WendGenerator.getLevel(levelNumber, difficulty) }
    var displayedWords by remember(levelNumber, difficulty) { mutableStateOf(level.targetWords) }
    var selectedPath by remember(levelNumber, difficulty) { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var foundWords by remember(levelNumber, difficulty) { mutableStateOf(setOf<String>()) }
    var foundCells by remember(levelNumber, difficulty) { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var hintCell by remember(levelNumber, difficulty) { mutableStateOf<Pair<Int, Int>?>(null) }
    var startTime by remember(levelNumber, difficulty) { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(onUndoRequested) {
        if (onUndoRequested && selectedPath.isNotEmpty()) {
            selectedPath = selectedPath.dropLast(1)
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    LaunchedEffect(onHintRequested) {
        if (onHintRequested) {
            val remainingWord = displayedWords.firstOrNull { !foundWords.contains(it) }
            if (remainingWord != null) {
                val posList = level.wordPositions[remainingWord]
                if (posList != null && posList.isNotEmpty()) {
                    hintCell = posList.first()
                    SoundManager.playHint()
                    HapticManager.playTap()
                }
            }
            resetControlTriggers()
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            displayedWords = level.targetWords
            selectedPath = emptyList()
            foundWords = emptySet()
            foundCells = emptySet()
            hintCell = null
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    fun shuffleWords() {
        displayedWords = displayedWords.shuffled()
        SoundManager.playTap()
        HapticManager.playTap()
    }

    fun onCellClick(r: Int, c: Int) {
        val clicked = Pair(r, c)
        if (foundCells.contains(clicked)) return

        if (selectedPath.contains(clicked)) {
            // Deselect back to this cell or clear
            if (selectedPath.last() == clicked) {
                selectedPath = selectedPath.dropLast(1)
            } else {
                val idx = selectedPath.indexOf(clicked)
                selectedPath = selectedPath.take(idx + 1)
            }
            SoundManager.playTap()
            return
        }

        if (selectedPath.isEmpty()) {
            selectedPath = listOf(clicked)
            SoundManager.playTap()
            HapticManager.playLightTap()
        } else {
            val last = selectedPath.last()
            val dr = abs(r - last.first)
            val dc = abs(c - last.second)
            if ((dr == 1 && dc == 0) || (dr == 0 && dc == 1) || (dr == 1 && dc == 1)) {
                val newPath = selectedPath + clicked
                selectedPath = newPath
                SoundManager.playTap()
                HapticManager.playLightTap()

                // Check if forming a target word
                val formedWord = newPath.map { level.grid[it.first][it.second] }.joinToString("")
                val reversedWord = formedWord.reversed()

                val matchedWord = displayedWords.firstOrNull {
                    !foundWords.contains(it) && (it == formedWord || it == reversedWord)
                }

                if (matchedWord != null) {
                    foundWords = foundWords + matchedWord
                    foundCells = foundCells + newPath
                    selectedPath = emptyList()
                    hintCell = null
                    SoundManager.playMatchSuccess()
                    HapticManager.playSuccess()

                    // Check Win
                    if (foundWords.size == displayedWords.size) {
                        val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
                        val stars = if (elapsedSec < 35) 3 else if (elapsedSec < 60) 2 else 1
                        val score = maxOf(100, 1000 - (elapsedSec * 6))
                        SoundManager.playLevelComplete()
                        onWin(score, elapsedSec, stars)
                    }
                }
            } else {
                // Restart path from new cell
                selectedPath = listOf(clicked)
                SoundManager.playTap()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Target Words Chips with Shuffle Button
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${level.category} (${foundWords.size}/${displayedWords.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FilledTonalButton(
                        onClick = { shuffleWords() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle Words", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shuffle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    displayedWords.forEach { word ->
                        val isFound = foundWords.contains(word)
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isFound) AccentEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isFound) AccentEmerald else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                if (isFound) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = word,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textDecoration = if (isFound) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (isFound) AccentEmerald else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Letter Grid
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            shadowElevation = 2.dp,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                level.grid.forEachIndexed { r, rowChars ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowChars.forEachIndexed { c, char ->
                            val cell = Pair(r, c)
                            val isSelected = selectedPath.contains(cell)
                            val isFound = foundCells.contains(cell)
                            val isHint = hintCell == cell

                            val bg by animateColorAsState(
                                targetValue = when {
                                    isFound -> AccentEmerald.copy(alpha = 0.25f)
                                    isSelected -> PrimaryIndigo
                                    isHint -> AccentTeal.copy(alpha = 0.35f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                label = "cellBg"
                            )

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(bg)
                                    .border(
                                        width = if (isSelected || isFound) 2.dp else 1.dp,
                                        color = when {
                                            isFound -> AccentEmerald
                                            isSelected -> PrimaryIndigo
                                            isHint -> AccentTeal
                                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        },
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onCellClick(r, c) }
                                    .testTag("wend_cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char.toString(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    color = if (isSelected) Color.White else if (isFound) AccentEmerald else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Spell Preview Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentWord = selectedPath.map { level.grid[it.first][it.second] }.joinToString("")
                Text(
                    text = if (currentWord.isEmpty()) "Tap adjacent letters to trace words" else currentWord,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 2.sp,
                    color = if (currentWord.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else PrimaryIndigo
                )
                if (selectedPath.isNotEmpty()) {
                    TextButton(onClick = { selectedPath = emptyList() }) {
                        Text("Clear", color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
