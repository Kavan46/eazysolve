package com.example.games.sudoku

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Edit
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
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryIndigo

data class SudokuPuzzle(
    val size: Int,
    val initialBoard: List<List<Int>>,
    val solutionBoard: List<List<Int>>
)

object SudokuGenerator {
    fun getPuzzle(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): SudokuPuzzle {
        val use4x4 = when (difficulty) {
            GameDifficulty.EASY -> true
            GameDifficulty.MEDIUM -> level <= 10
            GameDifficulty.HARD -> false
            GameDifficulty.EXPERT -> false
        }

        if (use4x4) {
            val templates = listOf(
                Pair(
                    listOf(
                        listOf(1, 0, 0, 4),
                        listOf(0, 0, 1, 0),
                        listOf(0, 4, 0, 0),
                        listOf(2, 0, 0, 3)
                    ),
                    listOf(
                        listOf(1, 2, 3, 4),
                        listOf(4, 3, 1, 2),
                        listOf(3, 4, 2, 1),
                        listOf(2, 1, 4, 3)
                    )
                ),
                Pair(
                    listOf(
                        listOf(0, 3, 4, 0),
                        listOf(4, 0, 0, 2),
                        listOf(1, 0, 0, 3),
                        listOf(0, 4, 1, 0)
                    ),
                    listOf(
                        listOf(2, 3, 4, 1),
                        listOf(4, 1, 3, 2),
                        listOf(1, 2, 4, 3),
                        listOf(3, 4, 1, 2)
                    )
                ),
                Pair(
                    listOf(
                        listOf(0, 0, 3, 1),
                        listOf(3, 0, 0, 0),
                        listOf(0, 0, 0, 4),
                        listOf(4, 1, 0, 0)
                    ),
                    listOf(
                        listOf(2, 4, 3, 1),
                        listOf(3, 1, 4, 2),
                        listOf(1, 3, 2, 4),
                        listOf(4, 2, 1, 3)
                    )
                )
            )
            val selected = templates[(level - 1).coerceAtLeast(0) % templates.size]
            return SudokuPuzzle(4, selected.first, selected.second)
        } else {
            // 6x6 board with 2x3 blocks
            val init6 = when (difficulty) {
                GameDifficulty.EXPERT -> listOf(
                    listOf(0, 0, 3, 0, 0, 0),
                    listOf(5, 0, 0, 3, 0, 0),
                    listOf(0, 5, 0, 0, 0, 3),
                    listOf(2, 0, 0, 4, 0, 0),
                    listOf(0, 0, 5, 0, 3, 0),
                    listOf(0, 0, 0, 1, 0, 0)
                )
                GameDifficulty.HARD -> listOf(
                    listOf(0, 0, 3, 0, 1, 0),
                    listOf(5, 0, 0, 3, 2, 0),
                    listOf(0, 5, 4, 0, 0, 3),
                    listOf(2, 0, 1, 4, 0, 0),
                    listOf(0, 4, 5, 0, 3, 0),
                    listOf(0, 2, 0, 1, 0, 0)
                )
                else -> listOf(
                    listOf(0, 0, 3, 0, 1, 0),
                    listOf(5, 6, 0, 3, 2, 0),
                    listOf(0, 5, 4, 2, 0, 3),
                    listOf(2, 0, 1, 4, 5, 0),
                    listOf(0, 4, 5, 0, 3, 2),
                    listOf(0, 2, 0, 1, 0, 0)
                )
            }
            val sol6 = listOf(
                listOf(4, 2, 3, 5, 1, 6),
                listOf(5, 6, 1, 3, 2, 4),
                listOf(1, 5, 4, 2, 6, 3),
                listOf(2, 3, 1, 4, 5, 6),
                listOf(6, 4, 5, 1, 3, 2),
                listOf(3, 2, 6, 1, 4, 5)
            )
            return SudokuPuzzle(6, init6, sol6)
        }
    }
}

@Composable
fun SudokuGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val puzzle = remember(levelNumber, difficulty) { SudokuGenerator.getPuzzle(levelNumber, difficulty) }
    val size = puzzle.size

    var board by remember(levelNumber, difficulty) {
        mutableStateOf(puzzle.initialBoard.map { it.toMutableList() })
    }
    var notes by remember(levelNumber, difficulty) {
        mutableStateOf(List(size) { List(size) { mutableSetOf<Int>() } })
    }
    var selectedCell by remember(levelNumber, difficulty) { mutableStateOf<Pair<Int, Int>?>(null) }
    var isNotesMode by remember { mutableStateOf(false) }
    var mistakes by remember(levelNumber) { mutableIntStateOf(0) }
    var undoStack by remember(levelNumber) { mutableStateOf(listOf<List<List<Int>>>()) }
    var startTime by remember(levelNumber) { mutableLongStateOf(System.currentTimeMillis()) }

    // Controls
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
            // Find first unsolved cell
            var solvedOne = false
            for (r in 0 until size) {
                for (c in 0 until size) {
                    if (board[r][c] == 0) {
                        undoStack = undoStack + listOf(board.map { it.toList() })
                        val newB = board.map { it.toMutableList() }
                        newB[r][c] = puzzle.solutionBoard[r][c]
                        board = newB
                        selectedCell = Pair(r, c)
                        SoundManager.playHint()
                        solvedOne = true
                        break
                    }
                }
                if (solvedOne) break
            }
            resetControlTriggers()
            checkWin(board, puzzle.solutionBoard, size, startTime, mistakes, onWin)
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            board = puzzle.initialBoard.map { it.toMutableList() }
            selectedCell = null
            mistakes = 0
            undoStack = emptyList()
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    fun onNumberInput(num: Int) {
        val cell = selectedCell ?: return
        val r = cell.first
        val c = cell.second
        if (puzzle.initialBoard[r][c] != 0) return // Fixed cell

        if (isNotesMode) {
            val curNotes = notes[r][c].toMutableSet()
            if (curNotes.contains(num)) curNotes.remove(num) else curNotes.add(num)
            notes = notes.mapIndexed { ri, rowList ->
                rowList.mapIndexed { ci, set ->
                    if (ri == r && ci == c) curNotes else set
                }
            }
            SoundManager.playTap()
        } else {
            undoStack = undoStack + listOf(board.map { it.toList() })
            val newB = board.map { it.toMutableList() }
            newB[r][c] = num
            board = newB

            // Validate against solution
            if (num != puzzle.solutionBoard[r][c]) {
                mistakes++
                SoundManager.playWrongMove()
            } else {
                SoundManager.playTap()
            }

            checkWin(newB, puzzle.solutionBoard, size, startTime, mistakes, onWin)
        }
    }

    fun onErase() {
        val cell = selectedCell ?: return
        val r = cell.first
        val c = cell.second
        if (puzzle.initialBoard[r][c] != 0) return
        undoStack = undoStack + listOf(board.map { it.toList() })
        val newB = board.map { it.toMutableList() }
        newB[r][c] = 0
        board = newB
        SoundManager.playTap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mistakes & Mode info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mistakes: $mistakes / 3",
                fontWeight = FontWeight.Bold,
                color = if (mistakes >= 3) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            FilledTonalIconToggleButton(
                checked = isNotesMode,
                onCheckedChange = { isNotesMode = it }
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Notes Mode",
                    tint = if (isNotesMode) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                            val isSelected = selectedCell == Pair(r, c)
                            val isInitial = puzzle.initialBoard[r][c] != 0
                            val value = board[r][c]
                            val isWrong = value != 0 && value != puzzle.solutionBoard[r][c]

                            val cellBg by animateColorAsState(
                                targetValue = when {
                                    isSelected -> PrimaryIndigo.copy(alpha = 0.2f)
                                    isWrong -> ErrorRed.copy(alpha = 0.2f)
                                    isInitial -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.surface
                                }, label = "sudoku_cell"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellBg)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedCell = Pair(r, c)
                                        SoundManager.playTap()
                                    }
                                    .testTag("sudoku_cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value != 0) {
                                    Text(
                                        text = "$value",
                                        fontWeight = if (isInitial) FontWeight.Black else FontWeight.Bold,
                                        fontSize = if (size == 4) 22.sp else 18.sp,
                                        color = when {
                                            isWrong -> ErrorRed
                                            isInitial -> MaterialTheme.colorScheme.onSurface
                                            else -> PrimaryIndigo
                                        }
                                    )
                                } else {
                                    val cellNotes = notes[r][c]
                                    if (cellNotes.isNotEmpty()) {
                                        Text(
                                            text = cellNotes.sorted().joinToString(" "),
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Number Keypad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            for (num in 1..size) {
                FilledTonalButton(
                    onClick = { onNumberInput(num) },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("sudoku_num_$num")
                ) {
                    Text("$num", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            FilledTonalIconButton(
                onClick = { onErase() },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "Erase")
            }
        }
    }
}

private fun checkWin(
    board: List<List<Int>>,
    solution: List<List<Int>>,
    size: Int,
    startTime: Long,
    mistakes: Int,
    onWin: (Int, Int, Int) -> Unit
) {
    for (r in 0 until size) {
        for (c in 0 until size) {
            if (board[r][c] != solution[r][c]) return
        }
    }
    // Completed!
    val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
    val stars = when {
        mistakes == 0 && elapsedSec < 60 -> 3
        mistakes <= 1 -> 2
        else -> 1
    }
    val score = maxOf(150, 1200 - (elapsedSec * 8) - (mistakes * 100))
    SoundManager.playLevelComplete()
    onWin(score, elapsedSec, stars)
}
