package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.models.GameInfo
import com.example.data.models.GameTutorialData
import com.example.data.models.GameType
import com.example.ui.theme.*

@Composable
fun GameRulesOverlay(
    gameInfo: GameInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val tutorial = remember(gameInfo.type) { GameTutorialData.getTutorial(gameInfo.type) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, gameInfo.accentColor.copy(alpha = 0.5f)),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .wrapContentHeight()
                    .testTag("game_rules_dialog_${gameInfo.type.name.lowercase()}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = gameInfo.accentColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, gameInfo.accentColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "HOW TO PLAY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = gameInfo.accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                HapticManager.playLightTap()
                                SoundManager.playTap()
                                onDismiss()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = extendedColors.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Big Emoji & Title
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = gameInfo.accentColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.5.dp, gameInfo.accentColor.copy(alpha = 0.4f)),
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(gameInfo.symbolEmoji, fontSize = 36.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = gameInfo.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = gameInfo.tagLine,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = gameInfo.accentColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Objective Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = extendedColors.cardBackground,
                        border = BorderStroke(1.dp, extendedColors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🎯", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "OBJECTIVE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp,
                                    color = extendedColors.textMuted
                                )
                                Text(
                                    text = tutorial.objective,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step-by-Step Rules
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tutorial.steps.forEachIndexed { index, step ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = extendedColors.cardBackground,
                                border = BorderStroke(1.dp, extendedColors.subtleBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = gameInfo.accentColor.copy(alpha = 0.15f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(step.icon, fontSize = 16.sp)
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = step.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = step.description,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pro Tip Banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AccentAmberBg,
                        border = BorderStroke(1.dp, AccentAmberBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("💡", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "PRO TIP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp,
                                    color = AccentAmber
                                )
                                Text(
                                    text = tutorial.proTip,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Got It / Start Game Button
                    Button(
                        onClick = {
                            HapticManager.playTap()
                            SoundManager.playTap()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = gameInfo.accentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("game_rules_got_it_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "GOT IT! LET'S PLAY",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
