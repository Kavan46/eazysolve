package com.example.games.queens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.theme.*
import kotlin.math.abs

enum class QueenCellState {
    EMPTY, QUEEN, CROSS
}

data class QueensLevel(
    val size: Int,
    val regions: List<List<Int>>, // Region ID 0..size-1 per cell
    val solutionQueens: List<Pair<Int, Int>>
)

object QueensGenerator {
    private val regionPalette = listOf(
        Color(0xFFFFE4E6), // Rose
        Color(0xFFE0F2FE), // Sky
        Color(0xFFFEF3C7), // Amber
        Color(0xFFD1FAE5), // Emerald
        Color(0xFFEDE9FE), // Violet
        Color(0xFFFFEDD5), // Orange
        Color(0xFFCFFAFE), // Cyan
        Color(0xFFFCE7F3)  // Fuchsia
    )

    fun getRegionColor(regionId: Int): Color {
        return regionPalette[regionId % regionPalette.size]
    }

    fun getLevel(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): QueensLevel {
        val size = when (difficulty) {
            GameDifficulty.EASY -> 5
            GameDifficulty.MEDIUM -> if (level <= 5) 5 else 6
            GameDifficulty.HARD -> 7
            GameDifficulty.EXPERT -> 8
        }
        if (size == 5) {
            val regions = listOf(
                listOf(0, 0, 1, 1, 1),
                listOf(0, 0, 2, 2, 1),
                listOf(0, 3, 2, 2, 4),
                listOf(3, 3, 3, 4, 4),
                listOf(3, 3, 4, 4, 4)
            )
            val sol = listOf(
                Pair(0, 1),
                Pair(1, 3),
                Pair(2, 0),
                Pair(3, 2),
                Pair(4, 4)
            )
            return QueensLevel(5, regions, sol)
        } else if (size == 6) {
            val regions = listOf(
                listOf(0, 0, 0, 1, 1, 1),
                listOf(0, 2, 2, 1, 3, 3),
                listOf(2, 2, 4, 4, 3, 3),
                listOf(5, 2, 4, 4, 4, 3),
                listOf(5, 5, 5, 4, 4, 3),
                listOf(5, 5, 5, 5, 3, 3)
            )
            val sol = listOf(
                Pair(0, 1),
                Pair(1, 4),
                Pair(2, 0),
                Pair(3, 5),
                Pair(4, 2),
                Pair(5, 3)
            )
            return QueensLevel(6, regions, sol)
        } else if (size == 7) {
            val regions = listOf(
                listOf(0, 0, 0, 1, 1, 1, 2),
                listOf(0, 3, 3, 1, 2, 2, 2),
                listOf(3, 3, 4, 4, 2, 5, 5),
                listOf(3, 4, 4, 4, 5, 5, 5),
                listOf(6, 4, 4, 4, 5, 5, 5),
                listOf(6, 6, 6, 6, 6, 5, 5),
                listOf(6, 6, 6, 6, 6, 6, 6)
            )
            val sol = listOf(
                Pair(0, 1),
                Pair(1, 4),
                Pair(2, 6),
                Pair(3, 0),
                Pair(4, 3),
                Pair(5, 5),
                Pair(6, 2)
            )
            return QueensLevel(7, regions, sol)
        } else {
            // 8x8
            val regions = List(8) { r ->
                List(8) { c ->
                    (r / 2) * 2 + (c / 4)
                }
            }
            val sol = listOf(
                Pair(0, 2), Pair(1, 5), Pair(2, 1), Pair(3, 6),
                Pair(4, 0), Pair(5, 3), Pair(6, 7), Pair(7, 4)
            )
            return QueensLevel(8, regions, sol)
        }
    }
}

@Composable
fun QueensGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val level = remember(levelNumber, difficulty) { QueensGenerator.getLevel(levelNumber, difficulty) }
    val size = level.size
    var board by remember(levelNumber, difficulty) {
        mutableStateOf(List(size) { MutableList(size) { QueenCellState.EMPTY } })
    }
    var undoStack by remember(levelNumber, difficulty) { mutableStateOf(listOf<List<List<QueenCellState>>>()) }
    var startTime by remember(levelNumber, difficulty) { mutableLongStateOf(System.currentTimeMillis()) }

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
            // Find a missing queen from the solution
            val currentQueens = mutableListOf<Pair<Int, Int>>()
            for (r in 0 until size) {
                for (c in 0 until size) {
                    if (board[r][c] == QueenCellState.QUEEN) currentQueens.add(Pair(r, c))
                }
            }
            val missing = level.solutionQueens.firstOrNull { !currentQueens.contains(it) }
            if (missing != null) {
                undoStack = undoStack + listOf(board.map { it.toList() })
                val newB = board.map { it.toMutableList() }
                newB[missing.first][missing.second] = QueenCellState.QUEEN
                board = newB
                SoundManager.playHint()
                checkQueensWin(newB, level.solutionQueens, size, startTime, onWin)
            }
            resetControlTriggers()
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            board = List(size) { MutableList(size) { QueenCellState.EMPTY } }
            undoStack = emptyList()
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    // Find conflicting queens
    val conflictingCells = remember(board) {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        val queenPositions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == QueenCellState.QUEEN) queenPositions.add(Pair(r, c))
            }
        }
        for (i in 0 until queenPositions.size) {
            for (j in i + 1 until queenPositions.size) {
                val p1 = queenPositions[i]
                val p2 = queenPositions[j]
                val sameRow = p1.first == p2.first
                val sameCol = p1.second == p2.second
                val sameRegion = level.regions[p1.first][p1.second] == level.regions[p2.first][p2.second]
                val touches = abs(p1.first - p2.first) <= 1 && abs(p1.second - p2.second) <= 1
                if (sameRow || sameCol || sameRegion || touches) {
                    conflicts.add(p1)
                    conflicts.add(p2)
                }
            }
        }
        conflicts
    }

    fun cycleCell(r: Int, c: Int) {
        undoStack = undoStack + listOf(board.map { it.toList() })
        val next = when (board[r][c]) {
            QueenCellState.EMPTY -> QueenCellState.QUEEN
            QueenCellState.QUEEN -> QueenCellState.CROSS
            QueenCellState.CROSS -> QueenCellState.EMPTY
        }
        val newB = board.map { it.toMutableList() }
        newB[r][c] = next
        board = newB
        if (next == QueenCellState.QUEEN) SoundManager.playTap() else SoundManager.playTap()

        checkQueensWin(newB, level.solutionQueens, size, startTime, onWin)
    }

    val placedQueensCount = board.sumOf { row -> row.count { it == QueenCellState.QUEEN } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Status Card
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Queens Placed: $placedQueensCount / $size",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (conflictingCells.isNotEmpty()) {
                    Text(
                        text = "⚠️ Conflict!",
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "✅ Safe",
                        color = AccentEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                .padding(6.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (r in 0 until size) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until size) {
                            val state = board[r][c]
                            val regionId = level.regions[r][c]
                            val regionColor = QueensGenerator.getRegionColor(regionId)
                            val isConflict = conflictingCells.contains(Pair(r, c))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isConflict) ErrorRed.copy(alpha = 0.35f) else regionColor)
                                    .border(
                                        width = if (isConflict) 2.dp else 1.dp,
                                        color = if (isConflict) ErrorRed else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { cycleCell(r, c) }
                                    .testTag("queens_cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                when (state) {
                                    QueenCellState.QUEEN -> Text("👑", fontSize = if (size == 5) 24.sp else 20.sp)
                                    QueenCellState.CROSS -> Text("✕", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    QueenCellState.EMPTY -> {}
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tap once for 👑 Queen, twice for ✕ Mark, 3 times to clear.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun checkQueensWin(
    board: List<List<QueenCellState>>,
    solution: List<Pair<Int, Int>>,
    size: Int,
    startTime: Long,
    onWin: (Int, Int, Int) -> Unit
) {
    var count = 0
    for (p in solution) {
        if (board[p.first][p.second] == QueenCellState.QUEEN) count++
    }
    if (count == size) {
        val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
        val stars = if (elapsedSec < 40) 3 else if (elapsedSec < 70) 2 else 1
        val score = maxOf(120, 1100 - (elapsedSec * 8))
        SoundManager.playLevelComplete()
        onWin(score, elapsedSec, stars)
    }
}
