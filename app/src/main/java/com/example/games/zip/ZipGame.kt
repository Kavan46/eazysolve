package com.example.games.zip

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.models.GameDifficulty
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.PrimaryIndigo
import kotlin.math.abs

data class CellPos(val row: Int, val col: Int)

data class ZipLevel(
    val levelNumber: Int,
    val rows: Int,
    val cols: Int,
    val start: CellPos,
    val end: CellPos,
    val obstacles: Set<CellPos>,
    val targetPath: List<CellPos>
)

object ZipLevelGenerator {
    fun getLevel(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): ZipLevel {
        val rows = when (difficulty) {
            GameDifficulty.EASY -> 4
            GameDifficulty.MEDIUM -> if (level <= 5) 4 else 5
            GameDifficulty.HARD -> 6
            GameDifficulty.EXPERT -> 7
        }
        val cols = rows

        // Deterministic level layout based on level number
        val start = CellPos(0, 0)
        val end = CellPos(rows - 1, cols - 1)

        val obstacleCount = when (difficulty) {
            GameDifficulty.EASY -> 1
            GameDifficulty.MEDIUM -> 2
            GameDifficulty.HARD -> 4
            GameDifficulty.EXPERT -> 6
        }

        val potentialObstacles = listOf(
            CellPos(1, 1), CellPos(2, 2), CellPos(0, 2), CellPos(2, 0),
            CellPos(1, 3), CellPos(3, 1), CellPos(3, 3), CellPos(4, 2),
            CellPos(2, 4), CellPos(4, 4), CellPos(5, 2), CellPos(2, 5)
        ).filter { it.row < rows && it.col < cols && it != start && it != end }

        val obstacles = potentialObstacles.take(obstacleCount).toSet()

        // Generate target path snake
        val path = mutableListOf<CellPos>()
        var cur = start
        path.add(cur)
        val visited = mutableSetOf(cur)

        // Pre-computed fallback path
        for (r in 0 until rows) {
            val cRange = if (r % 2 == 0) (0 until cols) else (cols - 1 downTo 0)
            for (c in cRange) {
                val p = CellPos(r, c)
                if (!obstacles.contains(p) && !visited.contains(p)) {
                    visited.add(p)
                    path.add(p)
                }
            }
        }
        return ZipLevel(level, rows, cols, start, end, obstacles, path)
    }
}

@Composable
fun ZipGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val level = remember(levelNumber, difficulty) { ZipLevelGenerator.getLevel(levelNumber, difficulty) }
    var currentPath by remember(levelNumber, difficulty) { mutableStateOf(listOf(level.start)) }
    var hintCell by remember(levelNumber, difficulty) { mutableStateOf<CellPos?>(null) }
    var startTime by remember(levelNumber, difficulty) { mutableLongStateOf(System.currentTimeMillis()) }

    val totalRequiredCells = (level.rows * level.cols) - level.obstacles.size

    // Handle incoming controls
    LaunchedEffect(onUndoRequested) {
        if (onUndoRequested) {
            if (currentPath.size > 1) {
                currentPath = currentPath.dropLast(1)
                SoundManager.playTap()
            }
            resetControlTriggers()
        }
    }

    LaunchedEffect(onHintRequested) {
        if (onHintRequested) {
            val nextIdx = currentPath.size
            if (nextIdx < level.targetPath.size) {
                hintCell = level.targetPath[nextIdx]
                SoundManager.playHint()
            }
            resetControlTriggers()
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            currentPath = listOf(level.start)
            hintCell = null
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    fun tryMoveTo(target: CellPos) {
        if (level.obstacles.contains(target)) return
        if (target.row !in 0 until level.rows || target.col !in 0 until level.cols) return

        val last = currentPath.lastOrNull() ?: return
        val dist = abs(last.row - target.row) + abs(last.col - target.col)
        if (dist != 1) return

        if (currentPath.contains(target)) {
            // Backtrack if tapping second to last
            if (currentPath.size > 1 && currentPath[currentPath.size - 2] == target) {
                currentPath = currentPath.dropLast(1)
                SoundManager.playTap()
            }
            return
        }

        val newPath = currentPath + target
        currentPath = newPath
        SoundManager.playTap()

        if (hintCell == target) hintCell = null

        // Check Win
        if (newPath.size >= totalRequiredCells && target == level.end) {
            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
            val stars = if (elapsedSec < 35) 3 else if (elapsedSec < 60) 2 else 1
            val score = maxOf(100, 1000 - (elapsedSec * 10))
            SoundManager.playLevelComplete()
            onWin(score, elapsedSec, stars)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Progress Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Path Coverage: ${currentPath.size} / $totalRequiredCells",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = if (currentPath.size == totalRequiredCells) AccentEmerald else PrimaryIndigo,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${((currentPath.size.toFloat() / totalRequiredCells) * 100).toInt()}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // The Grid Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (r in 0 until level.rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (c in 0 until level.cols) {
                            val pos = CellPos(r, c)
                            val isStart = pos == level.start
                            val isEnd = pos == level.end
                            val isObstacle = level.obstacles.contains(pos)
                            val pathIndex = currentPath.indexOf(pos)
                            val inPath = pathIndex != -1
                            val isCurrentHead = currentPath.lastOrNull() == pos
                            val isHint = hintCell == pos

                            val cellBg by animateColorAsState(
                                targetValue = when {
                                    isObstacle -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                    isCurrentHead -> PrimaryIndigo
                                    isStart -> AccentEmerald
                                    isEnd && inPath -> AccentEmerald
                                    inPath -> PrimaryIndigo.copy(alpha = 0.75f)
                                    isHint -> Color(0xFFFFD54F)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                }, label = "cell_color"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cellBg)
                                    .clickable(enabled = !isObstacle) {
                                        tryMoveTo(pos)
                                    }
                                    .testTag("zip_cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isObstacle -> {
                                        Text("✖", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                    isCurrentHead -> {
                                        Text("●", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                    isStart -> {
                                        Text("S", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }
                                    isEnd -> {
                                        Text("E", color = if (inPath) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }
                                    inPath -> {
                                        Text("${pathIndex + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    isHint -> {
                                        Text("💡", fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Connect from 'S' to 'E' passing through every open cell!",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
