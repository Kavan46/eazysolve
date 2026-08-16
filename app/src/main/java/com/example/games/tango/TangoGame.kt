package com.example.games.tango

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.models.GameDifficulty
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryIndigo

enum class TangoSymbol {
    EMPTY, SUN, MOON
}

data class ClueEdge(val r1: Int, val c1: Int, val r2: Int, val c2: Int, val isSame: Boolean)

data class TangoLevel(
    val size: Int,
    val initialBoard: List<List<TangoSymbol>>,
    val solutionBoard: List<List<TangoSymbol>>,
    val clues: List<ClueEdge>
)

object TangoGenerator {
    fun getLevel(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): TangoLevel {
        val size = when (difficulty) {
            GameDifficulty.EASY -> 4
            GameDifficulty.MEDIUM -> if (level <= 5) 4 else 6
            GameDifficulty.HARD -> 6
            GameDifficulty.EXPERT -> 6
        }
        if (size == 4) {
            val sol = listOf(
                listOf(TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.MOON),
                listOf(TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.SUN),
                listOf(TangoSymbol.SUN, TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.MOON),
                listOf(TangoSymbol.MOON, TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.SUN)
            )
            val init = listOf(
                listOf(TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.MOON),
                listOf(TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.EMPTY),
                listOf(TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.MOON),
                listOf(TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.SUN)
            )
            val clues = listOf(
                ClueEdge(0, 0, 0, 1, false), // SUN != MOON
                ClueEdge(2, 0, 2, 1, true)   // SUN == SUN
            )
            return TangoLevel(4, init, sol, clues)
        } else {
            // 6x6 level
            val sol6 = listOf(
                listOf(TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.MOON),
                listOf(TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.SUN),
                listOf(TangoSymbol.SUN, TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.MOON),
                listOf(TangoSymbol.MOON, TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.SUN),
                listOf(TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.SUN, TangoSymbol.MOON),
                listOf(TangoSymbol.MOON, TangoSymbol.SUN, TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.MOON, TangoSymbol.SUN)
            )
            val init6 = when (difficulty) {
                GameDifficulty.EXPERT -> listOf(
                    listOf(TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.SUN),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.EMPTY),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.EMPTY)
                )
                GameDifficulty.HARD -> listOf(
                    listOf(TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.EMPTY),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.SUN),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY),
                    listOf(TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.EMPTY),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.MOON),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.MOON, TangoSymbol.EMPTY)
                )
                else -> listOf(
                    listOf(TangoSymbol.SUN, TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.EMPTY),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.MOON, TangoSymbol.SUN),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY),
                    listOf(TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.EMPTY),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.EMPTY, TangoSymbol.MOON, TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.MOON),
                    listOf(TangoSymbol.EMPTY, TangoSymbol.SUN, TangoSymbol.EMPTY, TangoSymbol.MOON, TangoSymbol.MOON, TangoSymbol.EMPTY)
                )
            }
            val clues6 = listOf(
                ClueEdge(0, 0, 0, 1, false),
                ClueEdge(2, 0, 2, 1, true)
            )
            return TangoLevel(6, init6, sol6, clues6)
        }
    }
}

@Composable
fun TangoGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val level = remember(levelNumber, difficulty) { TangoGenerator.getLevel(levelNumber, difficulty) }
    val size = level.size

    var board by remember(levelNumber, difficulty) {
        mutableStateOf(level.initialBoard.map { it.toMutableList() })
    }
    var undoStack by remember(levelNumber) { mutableStateOf(listOf<List<List<TangoSymbol>>>()) }
    var startTime by remember(levelNumber) { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(onUndoRequested) {
        if (onUndoRequested && undoStack.isNotEmpty()) {
            val prev = undoStack.last()
            undoStack = undoStack.dropLast(1)
            board = prev.map { it.toMutableList() }
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    LaunchedEffect(onHintRequested) {
        if (onHintRequested) {
            for (r in 0 until size) {
                for (c in 0 until size) {
                    if (board[r][c] != level.solutionBoard[r][c]) {
                        undoStack = undoStack + listOf(board.map { it.toList() })
                        val newB = board.map { it.toMutableList() }
                        newB[r][c] = level.solutionBoard[r][c]
                        board = newB
                        SoundManager.playHint()
                        checkTangoWin(newB, level.solutionBoard, size, startTime, onWin)
                        break
                    }
                }
            }
            resetControlTriggers()
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            board = level.initialBoard.map { it.toMutableList() }
            undoStack = emptyList()
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    fun cycleCell(r: Int, c: Int) {
        if (level.initialBoard[r][c] != TangoSymbol.EMPTY) return
        undoStack = undoStack + listOf(board.map { it.toList() })
        val next = when (board[r][c]) {
            TangoSymbol.EMPTY -> TangoSymbol.SUN
            TangoSymbol.SUN -> TangoSymbol.MOON
            TangoSymbol.MOON -> TangoSymbol.EMPTY
        }
        val newB = board.map { it.toMutableList() }
        newB[r][c] = next
        board = newB
        SoundManager.playTap()
        checkTangoWin(newB, level.solutionBoard, size, startTime, onWin)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Rules banner
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Equal ☀️ & 🌙 per line", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("•", color = MaterialTheme.colorScheme.outline)
                Text("Max 2 in a row", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (r in 0 until size) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until size) {
                            val symbol = board[r][c]
                            val isInitial = level.initialBoard[r][c] != TangoSymbol.EMPTY

                            val bg by animateColorAsState(
                                targetValue = when (symbol) {
                                    TangoSymbol.SUN -> AccentAmber.copy(alpha = 0.18f)
                                    TangoSymbol.MOON -> AccentViolet.copy(alpha = 0.18f)
                                    TangoSymbol.EMPTY -> MaterialTheme.colorScheme.surface
                                }, label = "tango_cell"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(3.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .border(
                                        1.dp,
                                        if (isInitial) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = !isInitial) {
                                        cycleCell(r, c)
                                    }
                                    .testTag("tango_cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                when (symbol) {
                                    TangoSymbol.SUN -> Text("☀️", fontSize = if (size == 4) 26.sp else 20.sp)
                                    TangoSymbol.MOON -> Text("🌙", fontSize = if (size == 4) 26.sp else 20.sp)
                                    TangoSymbol.EMPTY -> Text("·", color = MaterialTheme.colorScheme.outline, fontSize = 24.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Tap any open cell to toggle between Sun ☀️ and Moon 🌙",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun checkTangoWin(
    board: List<List<TangoSymbol>>,
    solution: List<List<TangoSymbol>>,
    size: Int,
    startTime: Long,
    onWin: (Int, Int, Int) -> Unit
) {
    for (r in 0 until size) {
        for (c in 0 until size) {
            if (board[r][c] != solution[r][c]) return
        }
    }
    val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
    val stars = if (elapsedSec < 45) 3 else if (elapsedSec < 75) 2 else 1
    val score = maxOf(100, 1000 - (elapsedSec * 7))
    SoundManager.playLevelComplete()
    onWin(score, elapsedSec, stars)
}
