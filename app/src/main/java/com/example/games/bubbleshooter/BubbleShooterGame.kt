package com.example.games.bubbleshooter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.models.GameDifficulty
import com.example.ui.theme.*

data class BubbleShooterLevel(
    val levelNumber: Int,
    val initialGrid: List<List<Color?>>, // 7 rows x 6 cols
    val shotsAllowed: Int,
    val colorPalette: List<Color>
)

object BubbleShooterGenerator {
    private val C_RED = Color(0xFFEF4444)
    private val C_BLUE = Color(0xFF3B82F6)
    private val C_GREEN = Color(0xFF10B981)
    private val C_YELLOW = Color(0xFFF59E0B)
    private val C_PURPLE = Color(0xFF8B5CF6)
    private val C_CYAN = Color(0xFF06B6D4)

    fun getLevel(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): BubbleShooterLevel {
        val (colors, shots) = when (difficulty) {
            GameDifficulty.EASY -> Pair(listOf(C_RED, C_BLUE, C_GREEN), 24)
            GameDifficulty.MEDIUM -> Pair(listOf(C_RED, C_BLUE, C_GREEN, C_YELLOW), 20)
            GameDifficulty.HARD -> Pair(listOf(C_RED, C_BLUE, C_GREEN, C_YELLOW, C_PURPLE), 18)
            GameDifficulty.EXPERT -> Pair(listOf(C_RED, C_BLUE, C_GREEN, C_YELLOW, C_PURPLE, C_CYAN), 15)
        }

        val rows = 7
        val cols = 6
        val initialGrid = MutableList(rows) { r ->
            MutableList<Color?>(cols) { c ->
                if (r < 3) {
                    // Place grouped clusters on the ceiling
                    colors[(r + (c / 2) + level) % colors.size]
                } else if (r == 3 && (c % 2 == 0)) {
                    colors[(c + level) % colors.size]
                } else {
                    null
                }
            }
        }

        return BubbleShooterLevel(
            levelNumber = level,
            initialGrid = initialGrid,
            shotsAllowed = shots,
            colorPalette = colors
        )
    }
}

@Composable
fun BubbleShooterGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val level = remember(levelNumber, difficulty) { BubbleShooterGenerator.getLevel(levelNumber, difficulty) }
    var grid by remember(levelNumber, difficulty) {
        mutableStateOf(level.initialGrid.map { it.toMutableList() })
    }
    val bubblePalette = remember(levelNumber, difficulty) { level.colorPalette }
    var currentBubbleColor by remember(levelNumber, difficulty) { mutableStateOf(bubblePalette[0]) }
    var nextBubbleColor by remember(levelNumber, difficulty) { mutableStateOf(bubblePalette.getOrElse(1) { bubblePalette[0] }) }
    var remainingShots by remember(levelNumber, difficulty) { mutableIntStateOf(level.shotsAllowed) }
    var score by remember(levelNumber, difficulty) { mutableIntStateOf(0) }
    var undoStack by remember(levelNumber, difficulty) { mutableStateOf(listOf<List<List<Color?>>>()) }
    var startTime by remember(levelNumber, difficulty) { mutableLongStateOf(System.currentTimeMillis()) }
    var comboCount by remember(levelNumber, difficulty) { mutableIntStateOf(0) }

    LaunchedEffect(onUndoRequested) {
        if (onUndoRequested && undoStack.isNotEmpty()) {
            val prev = undoStack.last()
            undoStack = undoStack.dropLast(1)
            grid = prev.map { it.toMutableList() }
            remainingShots++
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    LaunchedEffect(onHintRequested) {
        if (onHintRequested) {
            // Match the color with the most bottom bubbles
            for (r in grid.indices.reversed()) {
                val availableColors = grid[r].filterNotNull()
                if (availableColors.isNotEmpty()) {
                    currentBubbleColor = availableColors.first()
                    SoundManager.playHint()
                    HapticManager.playTap()
                    break
                }
            }
            resetControlTriggers()
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            grid = level.initialGrid.map { it.toMutableList() }
            currentBubbleColor = bubblePalette[0]
            nextBubbleColor = bubblePalette.getOrElse(1) { bubblePalette[0] }
            remainingShots = level.shotsAllowed
            score = 0
            comboCount = 0
            undoStack = emptyList()
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    // Function to drop floating bubbles not anchored to ceiling r=0
    fun dropFloatingBubbles(newGrid: List<MutableList<Color?>>): Int {
        val rows = newGrid.size
        val cols = newGrid[0].size
        val connectedToCeiling = mutableSetOf<Pair<Int, Int>>()

        fun markCeiling(r: Int, c: Int) {
            if (r !in 0 until rows || c !in 0 until cols) return
            if (newGrid[r][c] == null || connectedToCeiling.contains(Pair(r, c))) return
            connectedToCeiling.add(Pair(r, c))
            markCeiling(r + 1, c)
            markCeiling(r - 1, c)
            markCeiling(r, c + 1)
            markCeiling(r, c - 1)
        }

        // Flood fill from ceiling row 0
        for (c in 0 until cols) {
            if (newGrid[0][c] != null) {
                markCeiling(0, c)
            }
        }

        var dropped = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (newGrid[r][c] != null && !connectedToCeiling.contains(Pair(r, c))) {
                    newGrid[r][c] = null
                    dropped++
                }
            }
        }
        return dropped
    }

    fun swapBubbles() {
        val temp = currentBubbleColor
        currentBubbleColor = nextBubbleColor
        nextBubbleColor = temp
        SoundManager.playTap()
        HapticManager.playLightTap()
    }

    fun shootAtCol(col: Int) {
        if (remainingShots <= 0) return

        // Ceiling is row 0. Find the bottom-most open spot in this column that connects to ceiling or bubbles
        var targetRow = -1
        for (r in grid.indices) {
            if (grid[r][col] == null) {
                targetRow = r
            } else {
                break
            }
        }

        // If targetRow is -1, the column is already full at row 0
        if (targetRow == -1) {
            // Find lowest empty spot below
            for (r in grid.indices) {
                if (grid[r][col] == null) {
                    targetRow = r
                    break
                }
            }
        }

        if (targetRow == -1) {
            SoundManager.playWrongMove()
            return // column completely full
        }

        undoStack = undoStack + listOf(grid.map { it.toList() })
        val newGrid = grid.map { it.toMutableList() }
        val shotColor = currentBubbleColor
        newGrid[targetRow][col] = shotColor

        // Find connected cluster of same color
        val matched = mutableSetOf<Pair<Int, Int>>()
        fun floodFill(r: Int, c: Int) {
            if (r !in newGrid.indices || c !in 0 until 6) return
            if (newGrid[r][c] != shotColor || matched.contains(Pair(r, c))) return
            matched.add(Pair(r, c))
            floodFill(r + 1, c)
            floodFill(r - 1, c)
            floodFill(r, c + 1)
            floodFill(r, c - 1)
        }
        floodFill(targetRow, col)

        if (matched.size >= 3) {
            matched.forEach { (mr, mc) ->
                newGrid[mr][mc] = null
            }
            comboCount++
            val droppedCount = dropFloatingBubbles(newGrid)
            val matchPoints = matched.size * 100 * comboCount
            val dropPoints = droppedCount * 150
            score += matchPoints + dropPoints
            SoundManager.playBubblePop()
            HapticManager.playSuccess()
        } else {
            comboCount = 0
            SoundManager.playTap()
            HapticManager.playLightTap()
        }

        grid = newGrid
        remainingShots--
        currentBubbleColor = nextBubbleColor

        // Pick next bubble from remaining active colors on board
        val activeColors = newGrid.flatten().filterNotNull().distinct()
        nextBubbleColor = if (activeColors.isNotEmpty()) activeColors.random() else bubblePalette.random()

        // Check Win
        val remainingBubbles = newGrid.sumOf { row -> row.count { it != null } }
        if (remainingBubbles == 0) {
            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
            val stars = if (remainingShots >= 10) 3 else if (remainingShots >= 5) 2 else 1
            val finalScore = score + (remainingShots * 75)
            SoundManager.playLevelComplete()
            onWin(finalScore, elapsedSec, stars)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status Bar
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
                    text = "Shots: $remainingShots",
                    fontWeight = FontWeight.Bold,
                    color = if (remainingShots < 5) ErrorRed else MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                if (comboCount > 1) {
                    Text(
                        text = "🔥 ${comboCount}x Combo!",
                        fontWeight = FontWeight.Bold,
                        color = AccentAmber,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "Score: $score",
                    fontWeight = FontWeight.Bold,
                    color = AccentEmerald,
                    fontSize = 15.sp
                )
            }
        }

        // Bubble Arena Grid (Ceiling at row 0)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                grid.forEachIndexed { r, rowBubbles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowBubbles.forEachIndexed { c, bubbleColor ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (bubbleColor != null) {
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.6f),
                                                    bubbleColor,
                                                    bubbleColor.copy(alpha = 0.85f)
                                                )
                                            )
                                        } else {
                                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                        }
                                    )
                                    .border(
                                        width = if (bubbleColor != null) 1.5.dp else 0.5.dp,
                                        color = if (bubbleColor != null) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    )
                                    .clickable { shootAtCol(c) }
                                    .testTag("shooter_cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bubbleColor != null) {
                                    // Highlight shine
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .align(Alignment.TopStart)
                                            .offset(x = 10.dp, y = 10.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.75f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Launcher with Bubble Swap & Tap-to-Aim
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Next Bubble (Click to Swap)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { swapBubbles() }
                ) {
                    Text("Next", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = nextBubbleColor,
                        border = BorderStroke(1.5.dp, Color.White),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(32.dp)
                    ) {}
                }

                // Current Active Shooter Bubble
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(72.dp)
                        .clickable { swapBubbles() },
                    border = BorderStroke(2.5.dp, PrimaryIndigo),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = currentBubbleColor,
                            border = BorderStroke(2.dp, Color.White),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.TopStart)
                                        .offset(x = 12.dp, y = 12.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.8f))
                                    )
                            }
                        }
                    }
                }

                // Swap Icon Button
                IconButton(
                    onClick = { swapBubbles() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Bubble", tint = PrimaryIndigo)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap any column above to fire & pop clusters of 3+ bubbles!",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
