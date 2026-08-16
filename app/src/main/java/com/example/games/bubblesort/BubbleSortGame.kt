package com.example.games.bubblesort

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.models.GameDifficulty
import com.example.ui.theme.PrimaryIndigo

data class BubbleSortLevel(
    val levelNumber: Int,
    val initialTubes: List<List<Color>>,
    val tubeCapacity: Int
)

object BubbleSortGenerator {
    private val C_RED = Color(0xFFEF4444)
    private val C_BLUE = Color(0xFF3B82F6)
    private val C_GREEN = Color(0xFF10B981)
    private val C_YELLOW = Color(0xFFF59E0B)
    private val C_PURPLE = Color(0xFF8B5CF6)
    private val C_TEAL = Color(0xFF06B6D4)

    fun getLevel(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): BubbleSortLevel {
        val cap = when (difficulty) {
            GameDifficulty.EASY -> 3
            GameDifficulty.MEDIUM -> 4
            GameDifficulty.HARD -> 4
            GameDifficulty.EXPERT -> 5
        }

        val tubes: List<List<Color>> = when (level % 4) {
            1 -> listOf(
                listOf(C_RED, C_BLUE, C_RED, C_BLUE).take(cap),
                listOf(C_BLUE, C_RED, C_BLUE, C_RED).take(cap),
                emptyList(),
                emptyList()
            )
            2 -> listOf(
                listOf(C_GREEN, C_YELLOW, C_RED, C_GREEN).take(cap),
                listOf(C_RED, C_GREEN, C_YELLOW, C_YELLOW).take(cap),
                listOf(C_YELLOW, C_RED, C_GREEN, C_RED).take(cap),
                emptyList(),
                emptyList()
            )
            3 -> listOf(
                listOf(C_BLUE, C_PURPLE, C_TEAL, C_BLUE).take(cap),
                listOf(C_PURPLE, C_BLUE, C_PURPLE, C_TEAL).take(cap),
                listOf(C_TEAL, C_TEAL, C_BLUE, C_PURPLE).take(cap),
                emptyList(),
                emptyList()
            )
            else -> listOf(
                listOf(C_RED, C_BLUE, C_GREEN, C_YELLOW).take(cap),
                listOf(C_YELLOW, C_GREEN, C_BLUE, C_RED).take(cap),
                listOf(C_GREEN, C_RED, C_YELLOW, C_BLUE).take(cap),
                listOf(C_BLUE, C_YELLOW, C_RED, C_GREEN).take(cap),
                emptyList(),
                emptyList()
            )
        }

        return BubbleSortLevel(level, tubes, cap)
    }
}

@Composable
fun BubbleSortGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val level = remember(levelNumber, difficulty) { BubbleSortGenerator.getLevel(levelNumber, difficulty) }
    var tubes by remember(levelNumber, difficulty) {
        mutableStateOf(level.initialTubes.map { it.toMutableList() })
    }
    val tubeCapacity = level.tubeCapacity
    var selectedTubeIndex by remember(levelNumber, difficulty) { mutableStateOf<Int?>(null) }
    var undoStack by remember(levelNumber, difficulty) { mutableStateOf(listOf<List<List<Color>>>()) }
    var movesCount by remember(levelNumber, difficulty) { mutableIntStateOf(0) }
    var startTime by remember(levelNumber, difficulty) { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(onUndoRequested) {
        if (onUndoRequested && undoStack.isNotEmpty()) {
            val prev = undoStack.last()
            undoStack = undoStack.dropLast(1)
            tubes = prev.map { it.toMutableList() }
            selectedTubeIndex = null
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    LaunchedEffect(onHintRequested) {
        if (onHintRequested) {
            // Find valid transfer
            for (i in tubes.indices) {
                if (tubes[i].isNotEmpty()) {
                    val color = tubes[i].last()
                    for (j in tubes.indices) {
                        if (i != j && tubes[j].size < tubeCapacity && (tubes[j].isEmpty() || tubes[j].last() == color)) {
                            selectedTubeIndex = i
                            SoundManager.playHint()
                            HapticManager.playTap()
                            break
                        }
                    }
                    if (selectedTubeIndex != null) break
                }
            }
            resetControlTriggers()
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            tubes = level.initialTubes.map { it.toMutableList() }
            selectedTubeIndex = null
            undoStack = emptyList()
            movesCount = 0
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    fun onTubeClick(index: Int) {
        val sel = selectedTubeIndex
        if (sel == null) {
            // Select source tube if it has balls
            if (tubes[index].isNotEmpty()) {
                selectedTubeIndex = index
                SoundManager.playTap()
                HapticManager.playLightTap()
            }
        } else if (sel == index) {
            // Deselect
            selectedTubeIndex = null
            SoundManager.playTap()
        } else {
            // Attempt move from sel to index
            val sourceTube = tubes[sel]
            val destTube = tubes[index]

            if (destTube.size < tubeCapacity) {
                val movingColor = sourceTube.last()
                if (destTube.isEmpty() || destTube.last() == movingColor) {
                    // Valid Move!
                    undoStack = undoStack + listOf(tubes.map { it.toList() })
                    val newTubes = tubes.map { it.toMutableList() }
                    newTubes[sel].removeAt(newTubes[sel].size - 1)
                    newTubes[index].add(movingColor)
                    tubes = newTubes
                    selectedTubeIndex = null
                    movesCount++
                    SoundManager.playBubblePop()
                    HapticManager.playSuccess()

                    // Check Win
                    checkSortWin(newTubes, tubeCapacity, startTime, movesCount, onWin)
                    return
                }
            }
            // Invalid move -> select clicked tube if it has balls, or deselect
            selectedTubeIndex = if (tubes[index].isNotEmpty()) index else null
            SoundManager.playWrongMove()
            HapticManager.playError()
        }
    }

    fun addExtraTube() {
        undoStack = undoStack + listOf(tubes.map { it.toList() })
        tubes = tubes + listOf(mutableListOf())
        SoundManager.playTap()
        HapticManager.playTap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Moves & Extra Tube Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Moves: $movesCount",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            FilledTonalButton(
                onClick = { addExtraTube() },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Extra Tube", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tubes Layout (Arranged in flexible rows for clean scaling)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val chunkCount = if (tubes.size > 4) (tubes.size + 1) / 2 else tubes.size
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                tubes.chunked(chunkCount).forEach { rowTubes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        rowTubes.forEach { tubeBubbles ->
                            val actualIndex = tubes.indexOf(tubeBubbles)
                            val isSelected = selectedTubeIndex == actualIndex

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { onTubeClick(actualIndex) }
                                    .testTag("bubble_tube_$actualIndex")
                            ) {
                                // Hovering lifted ball preview above tube
                                Box(
                                    modifier = Modifier.height(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected && tubeBubbles.isNotEmpty()) {
                                        val topColor = tubeBubbles.last()
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(Color.White.copy(alpha = 0.7f), topColor)
                                                    )
                                                )
                                                .border(1.5.dp, Color.White, CircleShape)
                                        )
                                    }
                                }

                                // Glass Tube Container
                                Surface(
                                    shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp, topStart = 8.dp, topEnd = 8.dp),
                                    color = if (isSelected) PrimaryIndigo.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.5.dp else 1.5.dp,
                                        color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                                    ),
                                    shadowElevation = if (isSelected) 4.dp else 1.dp,
                                    modifier = Modifier
                                        .width(52.dp)
                                        .height((tubeCapacity * 38 + 24).dp)
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(bottom = 8.dp, top = 6.dp)
                                    ) {
                                        // If selected, display balls except the one currently lifted at the top
                                        val displayedBalls = if (isSelected && tubeBubbles.isNotEmpty()) {
                                            tubeBubbles.dropLast(1)
                                        } else {
                                            tubeBubbles
                                        }

                                        displayedBalls.forEach { bubbleColor ->
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.radialGradient(
                                                            listOf(
                                                                Color.White.copy(alpha = 0.65f),
                                                                bubbleColor,
                                                                bubbleColor.copy(alpha = 0.9f)
                                                            )
                                                        )
                                                    )
                                                    .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${actualIndex + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Tap a tube to lift its top bubble, then tap another to pour!",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun checkSortWin(
    tubes: List<List<Color>>,
    capacity: Int,
    startTime: Long,
    moves: Int,
    onWin: (Int, Int, Int) -> Unit
) {
    // Win if all non-empty tubes are completely full with 1 uniform color
    val isWin = tubes.all { tube ->
        tube.isEmpty() || (tube.size == capacity && tube.distinct().size == 1)
    }
    if (isWin) {
        val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
        val stars = if (moves <= 16) 3 else if (moves <= 26) 2 else 1
        val score = maxOf(150, 1200 - (elapsedSec * 5) - (moves * 10))
        SoundManager.playLevelComplete()
        onWin(score, elapsedSec, stars)
    }
}
