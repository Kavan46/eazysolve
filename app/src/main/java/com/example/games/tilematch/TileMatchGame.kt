package com.example.games.tilematch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Warning
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
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.models.GameDifficulty
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentSky
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryIndigo

data class MatchTile(
    val id: Int,
    val symbol: String,
    val category: String,
    val slotIndex: Int
)

data class TileMatchLevel(
    val levelNumber: Int,
    val totalSlots: Int,
    val initialBoard: Map<Int, MatchTile> // slotIndex -> MatchTile
)

object TileMatchGenerator {
    // Rich mixed categories
    private val animalTiles = listOf("🦁", "🐼", "🦊", "🐨", "🐸", "🦄", "🐯", "🐰", "🐵", "🐘")
    private val fruitTiles = listOf("🍎", "🍇", "🥑", "🍓", "🍍", "🍉", "🍒", "🥭", "🍋", "🥝")
    private val symbolTiles = listOf("💎", "⚡", "🌟", "🍀", "🔮", "🪐", "👑", "🎯", "🔥", "🚀")
    private val snackTiles = listOf("🍕", "🍦", "🍩", "🍔", "🌮", "🍰", "🥨", "🍪", "🍿", "🥞")

    fun getLevel(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): TileMatchLevel {
        val totalTriples = when (difficulty) {
            GameDifficulty.EASY -> 3 // 9 tiles
            GameDifficulty.MEDIUM -> if (level <= 3) 4 else 5 // 12 or 15 tiles
            GameDifficulty.HARD -> if (level <= 3) 6 else 7 // 18 or 21 tiles
            GameDifficulty.EXPERT -> 8 // 24 tiles
        }

        // Mix tiles across categories based on level
        val pool = when (level % 4) {
            1 -> (animalTiles.shuffled().take(totalTriples / 2 + 1) + fruitTiles.shuffled().take(totalTriples / 2 + 1)).distinct().take(totalTriples)
            2 -> (symbolTiles.shuffled().take(totalTriples / 2 + 1) + snackTiles.shuffled().take(totalTriples / 2 + 1)).distinct().take(totalTriples)
            3 -> (animalTiles.shuffled().take(2) + fruitTiles.shuffled().take(2) + symbolTiles.shuffled().take(2) + snackTiles.shuffled().take(2)).distinct().take(totalTriples)
            else -> (fruitTiles.shuffled().take(3) + symbolTiles.shuffled().take(3) + animalTiles.shuffled().take(3)).distinct().take(totalTriples)
        }

        val allTilesList = mutableListOf<Pair<String, String>>()
        pool.forEach { sym ->
            val cat = when {
                animalTiles.contains(sym) -> "Animal"
                fruitTiles.contains(sym) -> "Fruit"
                symbolTiles.contains(sym) -> "Symbol"
                else -> "Snack"
            }
            repeat(3) {
                allTilesList.add(Pair(sym, cat))
            }
        }
        allTilesList.shuffle()

        val totalSlots = allTilesList.size
        val boardMap = mutableMapOf<Int, MatchTile>()
        allTilesList.forEachIndexed { slotIdx, (sym, cat) ->
            boardMap[slotIdx] = MatchTile(
                id = slotIdx + 1,
                symbol = sym,
                category = cat,
                slotIndex = slotIdx
            )
        }

        return TileMatchLevel(
            levelNumber = level,
            totalSlots = totalSlots,
            initialBoard = boardMap
        )
    }
}

@Composable
fun TileMatchGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val level = remember(levelNumber, difficulty) { TileMatchGenerator.getLevel(levelNumber, difficulty) }
    // Fixed board mapping slotIndex -> MatchTile? so tiles NEVER shift or jump when one is taken!
    var boardSlots by remember(levelNumber, difficulty) { mutableStateOf(level.initialBoard) }
    // Tray strictly limited to 4 items as requested!
    val maxTrayCapacity = 4
    var tray by remember(levelNumber, difficulty) { mutableStateOf(listOf<MatchTile>()) }
    var undoStack by remember(levelNumber, difficulty) {
        mutableStateOf(listOf<Pair<Map<Int, MatchTile>, List<MatchTile>>>())
    }
    var startTime by remember(levelNumber, difficulty) { mutableLongStateOf(System.currentTimeMillis()) }
    var clearedTriplesCount by remember(levelNumber, difficulty) { mutableIntStateOf(0) }
    var lastMatchedSymbol by remember(levelNumber, difficulty) { mutableStateOf<String?>(null) }
    var showTrayFullWarning by remember { mutableStateOf(false) }

    val remainingCount = boardSlots.size

    LaunchedEffect(onUndoRequested) {
        if (onUndoRequested && undoStack.isNotEmpty()) {
            val last = undoStack.last()
            undoStack = undoStack.dropLast(1)
            boardSlots = last.first
            tray = last.second
            showTrayFullWarning = false
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    LaunchedEffect(onHintRequested) {
        if (onHintRequested) {
            // Find a tile on board that matches one currently in tray, or a pair
            val inTraySymbols = tray.map { it.symbol }
            val matchingEntry = boardSlots.entries.firstOrNull { inTraySymbols.contains(it.value.symbol) }
                ?: boardSlots.entries.firstOrNull()

            if (matchingEntry != null && tray.size < maxTrayCapacity) {
                val tile = matchingEntry.value
                val slot = matchingEntry.key
                undoStack = undoStack + Pair(boardSlots, tray)

                val newBoard = boardSlots.toMutableMap()
                newBoard.remove(slot)
                val newTray = (tray + tile).sortedBy { it.symbol }
                boardSlots = newBoard

                // Check 3 match in tray
                val counts = newTray.groupingBy { it.symbol }.eachCount()
                val matchedSym = counts.entries.firstOrNull { it.value >= 3 }?.key

                if (matchedSym != null) {
                    tray = newTray.filter { it.symbol != matchedSym }
                    lastMatchedSymbol = matchedSym
                    clearedTriplesCount++
                    SoundManager.playMatchSuccess()
                    HapticManager.playSuccess()
                } else {
                    tray = newTray
                    SoundManager.playHint()
                }

                if (newBoard.isEmpty() && tray.isEmpty()) {
                    val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
                    val stars = if (elapsedSec < 35) 3 else if (elapsedSec < 60) 2 else 1
                    val score = maxOf(150, 1200 - (elapsedSec * 6))
                    SoundManager.playLevelComplete()
                    onWin(score, elapsedSec, stars)
                }
            }
            resetControlTriggers()
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            boardSlots = level.initialBoard
            tray = emptyList()
            undoStack = emptyList()
            clearedTriplesCount = 0
            lastMatchedSymbol = null
            showTrayFullWarning = false
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    fun onTileClick(slotIndex: Int, tile: MatchTile) {
        if (tray.size >= maxTrayCapacity) {
            // Tray is full!
            showTrayFullWarning = true
            SoundManager.playWrongMove()
            HapticManager.playError()
            return
        }

        showTrayFullWarning = false
        undoStack = undoStack + Pair(boardSlots, tray)

        // Remove from board while preserving all other slots in their exact positions
        val newBoard = boardSlots.toMutableMap()
        newBoard.remove(slotIndex)
        val newTray = (tray + tile).sortedBy { it.symbol }
        boardSlots = newBoard

        // Check if 3 matching tiles in tray
        val counts = newTray.groupingBy { it.symbol }.eachCount()
        val matchedSym = counts.entries.firstOrNull { it.value >= 3 }?.key

        if (matchedSym != null) {
            tray = newTray.filter { it.symbol != matchedSym }
            lastMatchedSymbol = matchedSym
            clearedTriplesCount++
            SoundManager.playMatchSuccess()
            HapticManager.playSuccess()
        } else {
            tray = newTray
            SoundManager.playTap()
            HapticManager.playLightTap()
        }

        // Check Win
        if (newBoard.isEmpty() && tray.isEmpty()) {
            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
            val stars = if (elapsedSec < 35) 3 else if (elapsedSec < 65) 2 else 1
            val score = maxOf(150, 1200 - (elapsedSec * 6))
            SoundManager.playLevelComplete()
            onWin(score, elapsedSec, stars)
        } else if (newTray.size == maxTrayCapacity && matchedSym == null) {
            showTrayFullWarning = true
        }
    }

    fun shuffleBoard() {
        if (boardSlots.isEmpty()) return
        val currentKeys = boardSlots.keys.toList()
        val currentTiles = boardSlots.values.toList().shuffled()
        val newBoard = mutableMapOf<Int, MatchTile>()
        currentKeys.forEachIndexed { i, key ->
            val t = currentTiles[i]
            newBoard[key] = t.copy(slotIndex = key)
        }
        boardSlots = newBoard
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
        // Status & Shuffle Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tiles Left: $remainingCount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (clearedTriplesCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = AccentEmerald.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "✨ $clearedTriplesCount Cleared",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                FilledTonalButton(
                    onClick = { shuffleBoard() },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shuffle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Fixed Spatial Tile Board (All remaining tiles stay anchored in their fixed grid positions!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            val columnsCount = 4
            val rowsCount = (level.totalSlots + columnsCount - 1) / columnsCount

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (r in 0 until rowsCount) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (c in 0 until columnsCount) {
                            val slotIndex = r * columnsCount + c
                            if (slotIndex < level.totalSlots) {
                                val tile = boardSlots[slotIndex]
                                if (tile != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        shadowElevation = 3.dp,
                                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clickable { onTileClick(slotIndex, tile) }
                                            .testTag("tile_slot_$slotIndex")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = tile.symbol,
                                                fontSize = 26.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    // Empty slot placeholder keeps the board layout completely fixed & unshifted!
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                                                RoundedCornerShape(14.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Match Tray - STRICTLY 4 SLOTS
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COLLECTION TRAY (${tray.size}/$maxTrayCapacity)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = if (tray.size == maxTrayCapacity) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showTrayFullWarning) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tray Full! Undo or match 3", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    width = if (tray.size == maxTrayCapacity) 2.dp else 1.dp,
                    color = if (tray.size == maxTrayCapacity) ErrorRed else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until maxTrayCapacity) {
                        val tileInSlot = tray.getOrNull(i)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (tileInSlot != null) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                            border = BorderStroke(
                                1.dp,
                                if (tileInSlot != null) PrimaryIndigo.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            shadowElevation = if (tileInSlot != null) 2.dp else 0.dp,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (tileInSlot != null) {
                                    Text(
                                        text = tileInSlot.symbol,
                                        fontSize = 28.sp,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "${i + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Collect 3 of the same symbol in the 4-slot tray to clear them!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
