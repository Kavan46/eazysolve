package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.GameType
import com.example.ui.theme.*

@Composable
fun GameIconBadge(
    gameType: GameType,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    shape: Shape = RoundedCornerShape(18.dp)
) {
    val bgGradient = when (gameType) {
        GameType.ZIP -> Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4338CA)))
        GameType.SUDOKU -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
        GameType.TANGO -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
        GameType.QUEENS -> Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFBE123C)))
        GameType.CROSSCLIMB -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
        GameType.PINPOINT -> Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0369A1)))
        GameType.WEND -> Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0F766E)))
        GameType.PATCHES -> Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFC2410C)))
        GameType.BUBBLE_SORT -> Brush.linearGradient(listOf(Color(0xFFD946EF), Color(0xFFA21CAF)))
        GameType.BUBBLE_SHOOTER -> Brush.linearGradient(listOf(Color(0xFF84CC16), Color(0xFF4D7C0F)))
        GameType.TILE_MATCH -> Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0E7490)))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bgGradient)
            .border(1.dp, PureWhite.copy(alpha = 0.25f), shape),
        contentAlignment = Alignment.Center
    ) {
        GameIconVector(
            gameType = gameType,
            modifier = Modifier.size(size * 0.75f)
        )
    }
}

@Composable
fun GameIconVector(
    gameType: GameType,
    modifier: Modifier = Modifier
) {
    when (gameType) {
        GameType.ZIP -> ZipIconCanvas(modifier)
        GameType.SUDOKU -> SudokuIconCanvas(modifier)
        GameType.TANGO -> TangoIconCanvas(modifier)
        GameType.QUEENS -> QueensIconCanvas(modifier)
        GameType.CROSSCLIMB -> CrossclimbIconCanvas(modifier)
        GameType.PINPOINT -> PinpointIconCanvas(modifier)
        GameType.WEND -> WendIconCanvas(modifier)
        GameType.PATCHES -> PatchesIconCanvas(modifier)
        GameType.BUBBLE_SORT -> BubbleSortIconCanvas(modifier)
        GameType.BUBBLE_SHOOTER -> BubbleShooterIconCanvas(modifier)
        GameType.TILE_MATCH -> TileMatchIconCanvas(modifier)
    }
}

@Composable
fun ZipIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Path line
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.2f)
            lineTo(w * 0.8f, h * 0.2f)
            lineTo(w * 0.8f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.8f)
            lineTo(w * 0.8f, h * 0.8f)
        }
        drawPath(
            path = path,
            color = PureWhite.copy(alpha = 0.4f),
            style = Stroke(width = w * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Lightning Bolt overlay
        val bolt = Path().apply {
            moveTo(w * 0.55f, h * 0.12f)
            lineTo(w * 0.35f, h * 0.5f)
            lineTo(w * 0.52f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.88f)
            lineTo(w * 0.68f, h * 0.45f)
            lineTo(w * 0.52f, h * 0.45f)
            close()
        }
        drawPath(path = bolt, color = Color(0xFFFDE047), style = Fill)
        drawPath(path = bolt, color = PureWhite, style = Stroke(width = 1.5f))

        // Start node
        drawCircle(color = Color(0xFF4ADE80), radius = w * 0.08f, center = Offset(w * 0.2f, h * 0.2f))
        // End node
        drawCircle(color = Color(0xFFF87171), radius = w * 0.08f, center = Offset(w * 0.8f, h * 0.8f))
    }
}

@Composable
fun SudokuIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 3x3 Outer Grid
        drawRoundRect(
            color = PureWhite.copy(alpha = 0.9f),
            topLeft = Offset(w * 0.1f, h * 0.1f),
            size = Size(w * 0.8f, h * 0.8f),
            cornerRadius = CornerRadius(w * 0.08f),
            style = Stroke(width = w * 0.06f)
        )

        // Internal dividing lines
        val cellW = w * 0.8f / 3f
        val cellH = h * 0.8f / 3f

        for (i in 1..2) {
            drawLine(
                color = PureWhite.copy(alpha = 0.7f),
                start = Offset(w * 0.1f + i * cellW, h * 0.1f),
                end = Offset(w * 0.1f + i * cellW, h * 0.9f),
                strokeWidth = w * 0.035f
            )
            drawLine(
                color = PureWhite.copy(alpha = 0.7f),
                start = Offset(w * 0.1f, h * 0.1f + i * cellH),
                end = Offset(w * 0.9f, h * 0.1f + i * cellH),
                strokeWidth = w * 0.035f
            )
        }

        // Highlight cells with colorful squares
        drawRoundRect(
            color = Color(0xFFFDE047),
            topLeft = Offset(w * 0.1f + cellW * 0.15f, h * 0.1f + cellH * 0.15f),
            size = Size(cellW * 0.7f, cellH * 0.7f),
            cornerRadius = CornerRadius(cellW * 0.15f)
        )

        drawRoundRect(
            color = Color(0xFF6EE7B7),
            topLeft = Offset(w * 0.1f + cellW * 1.15f, h * 0.1f + cellH * 1.15f),
            size = Size(cellW * 0.7f, cellH * 0.7f),
            cornerRadius = CornerRadius(cellW * 0.15f)
        )

        drawRoundRect(
            color = Color(0xFF93C5FD),
            topLeft = Offset(w * 0.1f + cellW * 2.15f, h * 0.1f + cellH * 2.15f),
            size = Size(cellW * 0.7f, cellH * 0.7f),
            cornerRadius = CornerRadius(cellW * 0.15f)
        )
    }
}

@Composable
fun TangoIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)

        // Sun (Left side)
        drawCircle(
            color = Color(0xFFFDE047),
            radius = w * 0.22f,
            center = Offset(w * 0.35f, h * 0.5f)
        )

        // Sun rays
        for (i in 0 until 8) {
            val angle = Math.toRadians(i * 45.0)
            val startR = w * 0.25f
            val endR = w * 0.32f
            val start = Offset(
                (w * 0.35f + Math.cos(angle) * startR).toFloat(),
                (h * 0.5f + Math.sin(angle) * startR).toFloat()
            )
            val end = Offset(
                (w * 0.35f + Math.cos(angle) * endR).toFloat(),
                (h * 0.5f + Math.sin(angle) * endR).toFloat()
            )
            drawLine(
                color = Color(0xFFFEF08A),
                start = start,
                end = end,
                strokeWidth = w * 0.04f,
                cap = StrokeCap.Round
            )
        }

        // Moon Crescent (Right side)
        val moonCenter = Offset(w * 0.65f, h * 0.5f)
        drawCircle(color = PureWhite, radius = w * 0.22f, center = moonCenter)
        drawCircle(color = Color(0xFFD97706), radius = w * 0.18f, center = Offset(w * 0.58f, h * 0.44f))

        // Intersecting link symbol (=)
        drawLine(
            color = PureWhite,
            start = Offset(w * 0.44f, h * 0.45f),
            end = Offset(w * 0.56f, h * 0.45f),
            strokeWidth = w * 0.035f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = PureWhite,
            start = Offset(w * 0.44f, h * 0.55f),
            end = Offset(w * 0.56f, h * 0.55f),
            strokeWidth = w * 0.035f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun QueensIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Checker base
        drawRoundRect(
            color = PureWhite.copy(alpha = 0.25f),
            topLeft = Offset(w * 0.15f, h * 0.72f),
            size = Size(w * 0.7f, h * 0.14f),
            cornerRadius = CornerRadius(w * 0.04f)
        )

        // Crown Path
        val crown = Path().apply {
            moveTo(w * 0.18f, h * 0.72f)
            lineTo(w * 0.12f, h * 0.32f) // Left point
            lineTo(w * 0.35f, h * 0.52f) // Dip 1
            lineTo(w * 0.5f, h * 0.2f)   // Center high point
            lineTo(w * 0.65f, h * 0.52f) // Dip 2
            lineTo(w * 0.88f, h * 0.32f) // Right point
            lineTo(w * 0.82f, h * 0.72f)
            close()
        }

        drawPath(path = crown, color = Color(0xFFFDE047), style = Fill)
        drawPath(path = crown, color = PureWhite, style = Stroke(width = w * 0.035f, join = StrokeJoin.Round))

        // Jewels on Crown tips
        drawCircle(color = Color(0xFF38BDF8), radius = w * 0.05f, center = Offset(w * 0.12f, h * 0.32f))
        drawCircle(color = Color(0xFFF43F5E), radius = w * 0.065f, center = Offset(w * 0.5f, h * 0.2f))
        drawCircle(color = Color(0xFF38BDF8), radius = w * 0.05f, center = Offset(w * 0.88f, h * 0.32f))

        // Crown band jewel
        drawCircle(color = Color(0xFF34D399), radius = w * 0.04f, center = Offset(w * 0.5f, h * 0.64f))
    }
}

@Composable
fun CrossclimbIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Ladder Rails
        drawLine(
            color = PureWhite,
            start = Offset(w * 0.25f, h * 0.15f),
            end = Offset(w * 0.25f, h * 0.85f),
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = PureWhite,
            start = Offset(w * 0.75f, h * 0.15f),
            end = Offset(w * 0.75f, h * 0.85f),
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )

        // Ladder Rungs with letter tiles
        val rungY = listOf(0.3f, 0.5f, 0.7f)
        val colors = listOf(Color(0xFFC084FC), Color(0xFFF472B6), Color(0xFF60A5FA))

        rungY.forEachIndexed { index, ry ->
            drawLine(
                color = PureWhite.copy(alpha = 0.8f),
                start = Offset(w * 0.25f, h * ry),
                end = Offset(w * 0.75f, h * ry),
                strokeWidth = w * 0.045f,
                cap = StrokeCap.Round
            )
            // Step block
            drawRoundRect(
                color = colors[index % colors.size],
                topLeft = Offset(w * 0.38f, h * ry - h * 0.08f),
                size = Size(w * 0.24f, h * 0.16f),
                cornerRadius = CornerRadius(w * 0.03f)
            )
            drawRoundRect(
                color = PureWhite,
                topLeft = Offset(w * 0.38f, h * ry - h * 0.08f),
                size = Size(w * 0.24f, h * 0.16f),
                cornerRadius = CornerRadius(w * 0.03f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

@Composable
fun PinpointIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)

        // Target Rings
        drawCircle(color = PureWhite.copy(alpha = 0.25f), radius = w * 0.42f, center = c)
        drawCircle(color = PureWhite, radius = w * 0.42f, center = c, style = Stroke(width = w * 0.04f))

        drawCircle(color = Color(0xFF38BDF8), radius = w * 0.28f, center = c)
        drawCircle(color = PureWhite, radius = w * 0.28f, center = c, style = Stroke(width = w * 0.03f))

        drawCircle(color = Color(0xFFEF4444), radius = w * 0.14f, center = c)
        drawCircle(color = PureWhite, radius = w * 0.05f, center = c)

        // Crosshairs
        drawLine(
            color = PureWhite.copy(alpha = 0.8f),
            start = Offset(w * 0.5f, h * 0.04f),
            end = Offset(w * 0.5f, h * 0.96f),
            strokeWidth = w * 0.03f
        )
        drawLine(
            color = PureWhite.copy(alpha = 0.8f),
            start = Offset(w * 0.04f, h * 0.5f),
            end = Offset(w * 0.96f, h * 0.5f),
            strokeWidth = w * 0.03f
        )
    }
}

@Composable
fun WendIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)

        // Compass Rose Outer Rim
        drawCircle(
            color = PureWhite.copy(alpha = 0.3f),
            radius = w * 0.42f,
            center = c,
            style = Stroke(width = w * 0.04f)
        )

        // Compass 4-point star
        val starNorth = Path().apply {
            moveTo(c.x, c.y)
            lineTo(c.x, h * 0.1f)
            lineTo(c.x + w * 0.1f, c.y)
            close()
        }
        val starNorthWest = Path().apply {
            moveTo(c.x, c.y)
            lineTo(c.x, h * 0.1f)
            lineTo(c.x - w * 0.1f, c.y)
            close()
        }
        val starSouth = Path().apply {
            moveTo(c.x, c.y)
            lineTo(c.x, h * 0.9f)
            lineTo(c.x - w * 0.1f, c.y)
            close()
        }
        val starEast = Path().apply {
            moveTo(c.x, c.y)
            lineTo(w * 0.9f, c.y)
            lineTo(c.x, c.y + h * 0.1f)
            close()
        }
        val starWest = Path().apply {
            moveTo(c.x, c.y)
            lineTo(w * 0.1f, c.y)
            lineTo(c.x, c.y - h * 0.1f)
            close()
        }

        drawPath(path = starNorth, color = Color(0xFFF43F5E))
        drawPath(path = starNorthWest, color = Color(0xFFFB7185))
        drawPath(path = starSouth, color = Color(0xFFFDE047))
        drawPath(path = starEast, color = Color(0xFFFDE047))
        drawPath(path = starWest, color = Color(0xFFFDE047))

        drawCircle(color = PureWhite, radius = w * 0.08f, center = c)
        drawCircle(color = Color(0xFF0F766E), radius = w * 0.04f, center = c)
    }
}

@Composable
fun PatchesIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Square Block (Top-Left)
        drawRoundRect(
            color = Color(0xFFFB923C),
            topLeft = Offset(w * 0.12f, h * 0.12f),
            size = Size(w * 0.35f, h * 0.35f),
            cornerRadius = CornerRadius(w * 0.06f)
        )

        // L-Shape Block (Top-Right & Mid)
        val lPiece = Path().apply {
            moveTo(w * 0.52f, h * 0.12f)
            lineTo(w * 0.88f, h * 0.12f)
            lineTo(w * 0.88f, h * 0.88f)
            lineTo(w * 0.52f, h * 0.88f)
            lineTo(w * 0.52f, h * 0.52f)
            lineTo(w * 0.52f, h * 0.52f)
            close()
        }
        drawPath(path = lPiece, color = Color(0xFF34D399))

        // T-Block (Bottom-Left)
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(w * 0.12f, h * 0.52f),
            size = Size(w * 0.35f, h * 0.35f),
            cornerRadius = CornerRadius(w * 0.06f)
        )

        // Outlines
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(w * 0.12f, h * 0.12f),
            size = Size(w * 0.35f, h * 0.35f),
            cornerRadius = CornerRadius(w * 0.06f),
            style = Stroke(width = w * 0.025f)
        )
        drawPath(path = lPiece, color = PureWhite, style = Stroke(width = w * 0.025f))
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(w * 0.12f, h * 0.52f),
            size = Size(w * 0.35f, h * 0.35f),
            cornerRadius = CornerRadius(w * 0.06f),
            style = Stroke(width = w * 0.025f)
        )
    }
}

@Composable
fun BubbleSortIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Tube 1
        val tube1Left = w * 0.18f
        val tubeW = w * 0.28f
        val tubeH = h * 0.72f
        val tubeTop = h * 0.14f

        drawRoundRect(
            color = PureWhite.copy(alpha = 0.2f),
            topLeft = Offset(tube1Left, tubeTop),
            size = Size(tubeW, tubeH),
            cornerRadius = CornerRadius(tubeW / 2f)
        )
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(tube1Left, tubeTop),
            size = Size(tubeW, tubeH),
            cornerRadius = CornerRadius(tubeW / 2f),
            style = Stroke(width = w * 0.035f)
        )

        // Bubbles in Tube 1
        drawCircle(color = Color(0xFFEF4444), radius = tubeW * 0.38f, center = Offset(tube1Left + tubeW / 2f, tubeTop + tubeH * 0.8f))
        drawCircle(color = Color(0xFF3B82F6), radius = tubeW * 0.38f, center = Offset(tube1Left + tubeW / 2f, tubeTop + tubeH * 0.48f))

        // Tube 2
        val tube2Left = w * 0.54f
        drawRoundRect(
            color = PureWhite.copy(alpha = 0.2f),
            topLeft = Offset(tube2Left, tubeTop),
            size = Size(tubeW, tubeH),
            cornerRadius = CornerRadius(tubeW / 2f)
        )
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(tube2Left, tubeTop),
            size = Size(tubeW, tubeH),
            cornerRadius = CornerRadius(tubeW / 2f),
            style = Stroke(width = w * 0.035f)
        )

        // Bubbles in Tube 2 (Sorted)
        drawCircle(color = Color(0xFF10B981), radius = tubeW * 0.38f, center = Offset(tube2Left + tubeW / 2f, tubeTop + tubeH * 0.8f))
        drawCircle(color = Color(0xFF10B981), radius = tubeW * 0.38f, center = Offset(tube2Left + tubeW / 2f, tubeTop + tubeH * 0.48f))
    }
}

@Composable
fun BubbleShooterIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Top Bubble Cluster
        drawCircle(color = Color(0xFFF43F5E), radius = w * 0.12f, center = Offset(w * 0.3f, h * 0.22f))
        drawCircle(color = Color(0xFF38BDF8), radius = w * 0.12f, center = Offset(w * 0.55f, h * 0.22f))
        drawCircle(color = Color(0xFFFDE047), radius = w * 0.12f, center = Offset(w * 0.8f, h * 0.22f))
        drawCircle(color = Color(0xFF34D399), radius = w * 0.12f, center = Offset(w * 0.42f, h * 0.42f))
        drawCircle(color = Color(0xFFA855F7), radius = w * 0.12f, center = Offset(w * 0.68f, h * 0.42f))

        // Pop shine on bubbles
        drawCircle(color = PureWhite.copy(alpha = 0.7f), radius = w * 0.035f, center = Offset(w * 0.27f, h * 0.19f))
        drawCircle(color = PureWhite.copy(alpha = 0.7f), radius = w * 0.035f, center = Offset(w * 0.52f, h * 0.19f))

        // Aiming cannon & projectile
        drawLine(
            color = PureWhite.copy(alpha = 0.6f),
            start = Offset(w * 0.5f, h * 0.85f),
            end = Offset(w * 0.5f, h * 0.6f),
            strokeWidth = w * 0.04f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // Shooting Bubble
        drawCircle(color = Color(0xFF34D399), radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.82f))
        drawCircle(color = PureWhite, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.82f), style = Stroke(width = w * 0.03f))
        drawCircle(color = PureWhite.copy(alpha = 0.8f), radius = w * 0.04f, center = Offset(w * 0.46f, h * 0.78f))
    }
}

@Composable
fun TileMatchIconCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 3 Layered isometric tiles
        val tileW = w * 0.44f
        val tileH = h * 0.44f

        // Tile 1 (Bottom layer)
        drawRoundRect(
            color = Color(0xFF0891B2),
            topLeft = Offset(w * 0.12f, h * 0.38f),
            size = Size(tileW, tileH),
            cornerRadius = CornerRadius(w * 0.08f)
        )
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(w * 0.12f, h * 0.38f),
            size = Size(tileW, tileH),
            cornerRadius = CornerRadius(w * 0.08f),
            style = Stroke(width = w * 0.03f)
        )

        // Tile 2 (Mid layer)
        drawRoundRect(
            color = Color(0xFF06B6D4),
            topLeft = Offset(w * 0.44f, h * 0.38f),
            size = Size(tileW, tileH),
            cornerRadius = CornerRadius(w * 0.08f)
        )
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(w * 0.44f, h * 0.38f),
            size = Size(tileW, tileH),
            cornerRadius = CornerRadius(w * 0.08f),
            style = Stroke(width = w * 0.03f)
        )

        // Tile 3 (Top layer)
        drawRoundRect(
            color = Color(0xFF67E8F9),
            topLeft = Offset(w * 0.28f, h * 0.14f),
            size = Size(tileW, tileH),
            cornerRadius = CornerRadius(w * 0.08f)
        )
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(w * 0.28f, h * 0.14f),
            size = Size(tileW, tileH),
            cornerRadius = CornerRadius(w * 0.08f),
            style = Stroke(width = w * 0.035f)
        )

        // Triple Match Star glyph
        val starC = Offset(w * 0.5f, h * 0.36f)
        drawCircle(color = Color(0xFFFDE047), radius = w * 0.12f, center = starC)
        drawCircle(color = PureWhite, radius = w * 0.05f, center = starC)
    }
}
