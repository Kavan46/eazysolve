package com.example.games.patches

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateRight
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

data class PolyPiece(
    val id: Int,
    val name: String,
    val color: Color,
    val offsets: List<Pair<Int, Int>> // Relative (r, c)
)

data class PatchesLevel(
    val size: Int,
    val pieces: List<PolyPiece>
)

object PatchesGenerator {
    fun getLevel(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): PatchesLevel {
        val size = when (difficulty) {
            GameDifficulty.EASY -> 3
            GameDifficulty.MEDIUM -> 4
            GameDifficulty.HARD -> 4
            GameDifficulty.EXPERT -> 5
        }
        val pieces = when (difficulty) {
            GameDifficulty.EASY -> listOf(
                PolyPiece(1, "Square", AccentOrange, listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1))),
                PolyPiece(2, "L-Shape", AccentEmerald, listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0))),
                PolyPiece(3, "I-Shape", AccentSky, listOf(Pair(0, 0), Pair(0, 1)))
            )
            GameDifficulty.EXPERT -> listOf(
                PolyPiece(1, "Square", AccentOrange, listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1))),
                PolyPiece(2, "L-Shape", AccentEmerald, listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(2, 1))),
                PolyPiece(3, "I-Shape", AccentSky, listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3))),
                PolyPiece(4, "T-Shape", AccentViolet, listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 1))),
                PolyPiece(5, "Plus", AccentAmber, listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(1, 2), Pair(2, 1))),
                PolyPiece(6, "Z-Shape", AccentTeal, listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1), Pair(1, 2)))
            )
            else -> listOf(
                PolyPiece(1, "Square", AccentOrange, listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1))),
                PolyPiece(2, "L-Shape", AccentEmerald, listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(2, 1))),
                PolyPiece(3, "I-Shape", AccentSky, listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3))),
                PolyPiece(4, "T-Shape", AccentViolet, listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 1)))
            )
        }
        return PatchesLevel(size, pieces)
    }
}

@Composable
fun PatchesGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val level = remember(levelNumber, difficulty) { PatchesGenerator.getLevel(levelNumber, difficulty) }
    val size = level.size

    // Placed pieces on board: Map of Cell to PieceId
    var boardState by remember(levelNumber) {
        mutableStateOf(mapOf<Pair<Int, Int>, Int>())
    }
    var placedPieces by remember(levelNumber) { mutableStateOf(setOf<Int>()) }
    var selectedPieceId by remember(levelNumber) { mutableStateOf<Int?>(level.pieces.firstOrNull()?.id) }
    var pieceRotations by remember(levelNumber) {
        mutableStateOf(level.pieces.associate { it.id to 0 })
    }
    var history by remember(levelNumber) { mutableStateOf(listOf<Pair<Int, List<Pair<Int, Int>>>>()) }
    var startTime by remember(levelNumber) { mutableLongStateOf(System.currentTimeMillis()) }

    fun getRotatedOffsets(piece: PolyPiece, rotation: Int): List<Pair<Int, Int>> {
        var cur = piece.offsets
        for (i in 0 until (rotation % 4)) {
            cur = cur.map { Pair(it.second, -it.first) }
        }
        val minR = cur.minOf { it.first }
        val minC = cur.minOf { it.second }
        return cur.map { Pair(it.first - minR, it.second - minC) }
    }

    LaunchedEffect(onUndoRequested) {
        if (onUndoRequested && history.isNotEmpty()) {
            val last = history.last()
            history = history.dropLast(1)
            val removedPieceId = last.first
            placedPieces = placedPieces - removedPieceId
            boardState = boardState.filterValues { it != removedPieceId }
            selectedPieceId = removedPieceId
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    LaunchedEffect(onHintRequested) {
        if (onHintRequested) {
            // Auto place next unplaced piece at a pre-computed valid spot
            val unplaced = level.pieces.firstOrNull { !placedPieces.contains(it.id) }
            if (unplaced != null) {
                // Find first valid placement
                for (r in 0 until size) {
                    for (c in 0 until size) {
                        val offsets = getRotatedOffsets(unplaced, pieceRotations[unplaced.id] ?: 0)
                        val targetCells = offsets.map { Pair(r + it.first, c + it.second) }
                        val inBounds = targetCells.all { it.first in 0 until size && it.second in 0 until size }
                        val noOverlap = targetCells.none { boardState.containsKey(it) }
                        if (inBounds && noOverlap) {
                            val newBoard = boardState.toMutableMap()
                            targetCells.forEach { newBoard[it] = unplaced.id }
                            boardState = newBoard
                            placedPieces = placedPieces + unplaced.id
                            history = history + Pair(unplaced.id, targetCells)
                            SoundManager.playHint()
                            checkWin(newBoard, size, startTime, onWin)
                            break
                        }
                    }
                    if (placedPieces.contains(unplaced.id)) break
                }
            }
            resetControlTriggers()
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            boardState = emptyMap()
            placedPieces = emptySet()
            selectedPieceId = level.pieces.firstOrNull()?.id
            history = emptyList()
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    fun onCellTap(r: Int, c: Int) {
        val selId = selectedPieceId ?: return
        if (placedPieces.contains(selId)) return
        val piece = level.pieces.firstOrNull { it.id == selId } ?: return
        val offsets = getRotatedOffsets(piece, pieceRotations[selId] ?: 0)
        val targetCells = offsets.map { Pair(r + it.first, c + it.second) }

        val inBounds = targetCells.all { it.first in 0 until size && it.second in 0 until size }
        val noOverlap = targetCells.none { boardState.containsKey(it) }

        if (inBounds && noOverlap) {
            val newBoard = boardState.toMutableMap()
            targetCells.forEach { newBoard[it] = piece.id }
            boardState = newBoard
            placedPieces = placedPieces + piece.id
            history = history + Pair(piece.id, targetCells)
            selectedPieceId = level.pieces.firstOrNull { !placedPieces.contains(it.id) && it.id != piece.id }?.id
            SoundManager.playTap()
            checkWin(newBoard, size, startTime, onWin)
        } else {
            SoundManager.playWrongMove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status & Rotate Action
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Placed: ${placedPieces.size} / ${level.pieces.size}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = {
                        val selId = selectedPieceId ?: return@Button
                        val curRot = pieceRotations[selId] ?: 0
                        pieceRotations = pieceRotations + (selId to (curRot + 1))
                        SoundManager.playTap()
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rotate", fontSize = 12.sp)
                }
            }
        }

        // Board Grid
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
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
                            val pieceId = boardState[Pair(r, c)]
                            val piece = level.pieces.firstOrNull { it.id == pieceId }

                            val bg by animateColorAsState(
                                targetValue = piece?.color ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                label = "patches_cell"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .clickable { onCellTap(r, c) }
                                    .testTag("patches_cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (piece != null) {
                                    Text(
                                        text = "${piece.id}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Piece Selection Tray
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            level.pieces.forEach { piece ->
                val isPlaced = placedPieces.contains(piece.id)
                val isSelected = selectedPieceId == piece.id

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) piece.color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) piece.color else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .clickable(enabled = !isPlaced) {
                            selectedPieceId = piece.id
                            SoundManager.playTap()
                        }
                        .testTag("patches_piece_${piece.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isPlaced) Color.Gray else piece.color)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPlaced) "Placed" else piece.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaced) Color.Gray else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun checkWin(
    board: Map<Pair<Int, Int>, Int>,
    size: Int,
    startTime: Long,
    onWin: (Int, Int, Int) -> Unit
) {
    if (board.size == size * size) {
        val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
        val stars = if (elapsedSec < 40) 3 else if (elapsedSec < 70) 2 else 1
        val score = maxOf(100, 1000 - (elapsedSec * 6))
        SoundManager.playLevelComplete()
        onWin(score, elapsedSec, stars)
    }
}
