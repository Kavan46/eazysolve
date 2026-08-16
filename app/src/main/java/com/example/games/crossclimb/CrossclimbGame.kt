package com.example.games.crossclimb

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.models.GameDifficulty
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryIndigo

data class LadderRung(
    val word: String,
    val clue: String
)

data class CrossclimbLevel(
    val levelNumber: Int,
    val rungs: List<LadderRung>
)

object CrossclimbGenerator {
    private val levels = listOf(
        CrossclimbLevel(
            levelNumber = 1,
            rungs = listOf(
                LadderRung("COLD", "Low temperature (Given)"),
                LadderRung("CORD", "A flexible rope or string"),
                LadderRung("CARD", "Used to play poker or pay at checkout"),
                LadderRung("WARD", "Hospital care division"),
                LadderRung("WARM", "Pleasantly high temperature"),
                LadderRung("WORM", "Crawling earth creature")
            )
        ),
        CrossclimbLevel(
            levelNumber = 2,
            rungs = listOf(
                LadderRung("SHIP", "Large ocean-going vessel"),
                LadderRung("SHOP", "Store where goods are sold"),
                LadderRung("SHOT", "Fired from a cannon or camera"),
                LadderRung("SLOT", "A narrow opening or coin aperture"),
                LadderRung("SLOW", "Not moving quickly"),
                LadderRung("GLOW", "Emit a steady, soft light")
            )
        ),
        CrossclimbLevel(
            levelNumber = 3,
            rungs = listOf(
                LadderRung("LEAD", "First place in a race"),
                LadderRung("HEAD", "Upper part of the human body"),
                LadderRung("HEAR", "Perceive sound with ears"),
                LadderRung("BEAR", "Large furry forest mammal"),
                LadderRung("BEAT", "Rhythmic pulse in music"),
                LadderRung("BOAT", "Small water craft")
            )
        ),
        CrossclimbLevel(
            levelNumber = 4,
            rungs = listOf(
                LadderRung("DARK", "Absence of light"),
                LadderRung("DART", "Small pointed missile"),
                LadderRung("DIRT", "Soil on the ground"),
                LadderRung("DIRE", "Extremely urgent or serious"),
                LadderRung("FIRE", "Combustion and flame"),
                LadderRung("FINE", "Satisfactory or good quality")
            )
        ),
        CrossclimbLevel(
            levelNumber = 5,
            rungs = listOf(
                LadderRung("MOON", "Earth's natural satellite"),
                LadderRung("SOON", "In a short time from now"),
                LadderRung("SEEN", "Observed with eyesight"),
                LadderRung("SEED", "Source of a growing plant"),
                LadderRung("FEED", "Give food to an animal"),
                LadderRung("FEET", "Plural of foot")
            )
        ),
        CrossclimbLevel(
            levelNumber = 6,
            rungs = listOf(
                LadderRung("PEAK", "Pointed top of a mountain"),
                LadderRung("BEAK", "Bird's bill or snout"),
                LadderRung("BEAM", "Ray of radiant sunshine"),
                LadderRung("SEAM", "Stitched line in clothing"),
                LadderRung("SLAM", "Shut a door with force"),
                LadderRung("SLIM", "Gracefully slender")
            )
        )
    )

    fun getLevel(level: Int, difficulty: GameDifficulty = GameDifficulty.MEDIUM): CrossclimbLevel {
        val base = levels[(level - 1).coerceAtLeast(0) % levels.size]
        val rungCount = when (difficulty) {
            GameDifficulty.EASY -> 3
            GameDifficulty.MEDIUM -> 4
            GameDifficulty.HARD -> 5
            GameDifficulty.EXPERT -> 6
        }
        return base.copy(rungs = base.rungs.take(rungCount))
    }
}

@Composable
fun CrossclimbGameScreen(
    levelNumber: Int,
    difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    onWin: (score: Int, timeSec: Int, stars: Int) -> Unit,
    onUndoRequested: Boolean,
    onHintRequested: Boolean,
    onRestartRequested: Boolean,
    resetControlTriggers: () -> Unit
) {
    val level = remember(levelNumber, difficulty) { CrossclimbGenerator.getLevel(levelNumber, difficulty) }
    var currentRungIndex by remember(levelNumber, difficulty) { mutableIntStateOf(1) } // 0 is given
    var inputChars by remember(levelNumber, difficulty) { mutableStateOf(List(4) { "" }) }
    var solvedRungs by remember(levelNumber, difficulty) {
        mutableStateOf(listOf(level.rungs[0].word))
    }
    var selectedCharIdx by remember(levelNumber, difficulty) { mutableIntStateOf(0) }
    var startTime by remember(levelNumber, difficulty) { mutableLongStateOf(System.currentTimeMillis()) }
    var isWordWrong by remember(levelNumber, difficulty) { mutableStateOf(false) }

    // Device soft keyboard controller & Focus Requester
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var hiddenFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    // Request keyboard on load
    LaunchedEffect(currentRungIndex) {
        try {
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (_: Exception) {}
    }

    LaunchedEffect(onUndoRequested) {
        if (onUndoRequested) {
            inputChars = inputChars.mapIndexed { idx, s -> if (idx == selectedCharIdx) "" else s }
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    LaunchedEffect(onHintRequested) {
        if (onHintRequested && currentRungIndex < level.rungs.size) {
            val target = level.rungs[currentRungIndex].word
            val targetChar = target[selectedCharIdx].toString()
            inputChars = inputChars.mapIndexed { idx, s -> if (idx == selectedCharIdx) targetChar else s }
            SoundManager.playHint()
            HapticManager.playTap()
            resetControlTriggers()
        }
    }

    LaunchedEffect(onRestartRequested) {
        if (onRestartRequested) {
            currentRungIndex = 1
            inputChars = List(4) { "" }
            solvedRungs = listOf(level.rungs[0].word)
            selectedCharIdx = 0
            isWordWrong = false
            startTime = System.currentTimeMillis()
            SoundManager.playTap()
            resetControlTriggers()
        }
    }

    fun submitWordIfReady(currentInputs: List<String>) {
        val entered = currentInputs.joinToString("")
        if (entered.length == 4 && currentRungIndex < level.rungs.size) {
            val target = level.rungs[currentRungIndex].word
            if (entered.equals(target, ignoreCase = true)) {
                isWordWrong = false
                SoundManager.playMatchSuccess()
                HapticManager.playSuccess()
                solvedRungs = solvedRungs + target
                if (currentRungIndex + 1 < level.rungs.size) {
                    currentRungIndex++
                    inputChars = List(4) { "" }
                    selectedCharIdx = 0
                } else {
                    // Won!
                    val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
                    val stars = if (elapsedSec < 45) 3 else if (elapsedSec < 80) 2 else 1
                    val score = maxOf(100, 1000 - (elapsedSec * 6))
                    SoundManager.playLevelComplete()
                    onWin(score, elapsedSec, stars)
                }
            } else {
                isWordWrong = true
                SoundManager.playWrongMove()
                HapticManager.playError()
            }
        }
    }

    fun onKeyPress(key: String) {
        isWordWrong = false
        val newInputs = inputChars.mapIndexed { idx, s -> if (idx == selectedCharIdx) key.uppercase() else s }
        inputChars = newInputs
        selectedCharIdx = (selectedCharIdx + 1).coerceAtMost(3)
        SoundManager.playTap()
        HapticManager.playLightTap()
        submitWordIfReady(newInputs)
    }

    fun onBackspace() {
        isWordWrong = false
        if (inputChars[selectedCharIdx].isNotEmpty()) {
            inputChars = inputChars.mapIndexed { idx, s -> if (idx == selectedCharIdx) "" else s }
        } else if (selectedCharIdx > 0) {
            selectedCharIdx--
            inputChars = inputChars.mapIndexed { idx, s -> if (idx == selectedCharIdx) "" else s }
        }
        SoundManager.playTap()
        HapticManager.playTap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable {
                focusRequester.requestFocus()
                keyboardController?.show()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Hidden BasicTextField capturing real mobile device keyboard input
        BasicTextField(
            value = hiddenFieldValue,
            onValueChange = { newValue ->
                hiddenFieldValue = newValue
                if (newValue.text.isNotEmpty()) {
                    val lastChar = newValue.text.last()
                    if (lastChar.isLetter()) {
                        onKeyPress(lastChar.toString())
                    }
                    hiddenFieldValue = TextFieldValue("")
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    val entered = inputChars.joinToString("")
                    if (entered.length == 4) submitWordIfReady(inputChars)
                }
            ),
            modifier = Modifier
                .size(1.dp)
                .alpha(0.01f)
                .focusRequester(focusRequester),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent)
        )

        // Ladder Rungs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            level.rungs.forEachIndexed { idx, rung ->
                val isSolved = idx < solvedRungs.size
                val isCurrent = idx == currentRungIndex

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        isSolved -> AccentEmerald.copy(alpha = 0.15f)
                        isCurrent -> PrimaryIndigo.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isSolved) AccentEmerald.copy(alpha = 0.6f) else if (isCurrent) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 4.dp)
                        .clickable {
                            if (isCurrent) {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSolved) solvedRungs[idx] else if (isCurrent) "Step ${idx + 1}" else "• • • •",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 3.sp,
                            color = if (isSolved) AccentEmerald else if (isCurrent) PrimaryIndigo else MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = if (isCurrent || isSolved) rung.clue else "Locked Rung",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Input Slots (Clicking opens device keyboard and focuses slot)
            if (currentRungIndex < level.rungs.size) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in 0 until 4) {
                            val isSelected = selectedCharIdx == i
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = when {
                                    isWordWrong -> ErrorRed.copy(alpha = 0.12f)
                                    isSelected -> PrimaryIndigo.copy(alpha = 0.16f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.5.dp else 1.5.dp,
                                    color = when {
                                        isWordWrong -> ErrorRed
                                        isSelected -> PrimaryIndigo
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }
                                ),
                                shadowElevation = if (isSelected) 3.dp else 1.dp,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clickable {
                                        selectedCharIdx = i
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    }
                                    .testTag("crossclimb_slot_$i")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = inputChars[i],
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        color = if (isWordWrong) ErrorRed else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.clickable {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    ) {
                        Icon(Icons.Default.Keyboard, contentDescription = "Keyboard", tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Tap any box to type with your device keyboard",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryIndigo
                        )
                    }
                }
            }
        }

        // On-screen Quick Keyboard as convenience
        val rows = listOf(
            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            listOf("Z", "X", "C", "V", "B", "N", "M")
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEachIndexed { rIdx, keyRow ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    keyRow.forEach { char ->
                        FilledTonalButton(
                            onClick = { onKeyPress(char) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(char, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    if (rIdx == 2) {
                        FilledTonalIconButton(
                            onClick = { onBackspace() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Default.Backspace, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
