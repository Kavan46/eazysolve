package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.datastore.ThemeMode
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {}
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    val extendedColors = LocalExtendedColors.current
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset All Settings?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This will restore theme, audio toggles, and onboarding status to default preferences stored in Android DataStore.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticManager.playHeavyClick(hapticFeedback)
                        viewModel.resetAllSettings()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("Reset", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        HapticManager.playLightTap(hapticFeedback)
                        showResetDialog = false
                    }
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = extendedColors.cardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    HapticManager.playLightTap(hapticFeedback)
                                    onNavigateBack()
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "SETTINGS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Persistent DataStore Indicator badge
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = AccentEmeraldBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentEmerald))
                            Text(
                                text = "DATASTORE ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = AccentEmerald
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Dark Mode Switch Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = extendedColors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (extendedColors.isDark) Color(0xFF1E293B) else PastelAmber,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (extendedColors.isDark) "🌙" else "☀️",
                                        fontSize = 22.sp
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Dark Mode Toggle",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (extendedColors.isDark) "Dark Balance theme active" else "Geometric Light theme active",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = appSettings.themeMode == ThemeMode.DARK,
                            onCheckedChange = { isDark ->
                                HapticManager.playHeavyClick(hapticFeedback)
                                viewModel.setThemeMode(if (isDark) ThemeMode.DARK else ThemeMode.LIGHT)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = PrimaryIndigo,
                                uncheckedThumbColor = PureWhite,
                                uncheckedTrackColor = Slate300
                            ),
                            modifier = Modifier.testTag("dark_mode_quick_switch")
                        )
                    }
                }
            }

            // Theme Mode Selection
            item {
                Text(
                    text = "THEME & APPEARANCE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            val isSelected = appSettings.themeMode == mode
                            val cardBg by animateColorAsState(
                                targetValue = if (isSelected) extendedColors.cardBackgroundElevated else Color.Transparent,
                                label = "themeCardBg"
                            )

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = cardBg,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryIndigo) else androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        HapticManager.playTap(hapticFeedback)
                                        viewModel.setThemeMode(mode)
                                    }
                                    .testTag("theme_mode_${mode.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = when (mode) {
                                                ThemeMode.SYSTEM -> "⚙️"
                                                ThemeMode.LIGHT -> "☀️"
                                                ThemeMode.DARK -> "🌙"
                                            },
                                            fontSize = 20.sp
                                        )
                                        Column {
                                            Text(
                                                text = mode.displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = when (mode) {
                                                    ThemeMode.SYSTEM -> "Follows device system theme settings"
                                                    ThemeMode.LIGHT -> "Clean white canvas with Slate accents"
                                                    ThemeMode.DARK -> "High contrast dark mode for night play"
                                                },
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = PrimaryIndigo,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = PureWhite,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Difficulty Level Selection
            item {
                Text(
                    text = "GAME DIFFICULTY & CHALLENGE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Select your preferred difficulty. This changes puzzle grid dimensions, time limits, clues, and score multipliers across all 10 games.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        com.example.data.models.GameDifficulty.entries.forEach { diff ->
                            val isSelected = appSettings.difficulty == diff
                            val cardBg by animateColorAsState(
                                targetValue = if (isSelected) extendedColors.cardBackgroundElevated else Color.Transparent,
                                label = "diffCardBg"
                            )

                            val borderColor = when (diff) {
                                com.example.data.models.GameDifficulty.EASY -> AccentEmerald
                                com.example.data.models.GameDifficulty.MEDIUM -> AccentAmber
                                com.example.data.models.GameDifficulty.HARD -> AccentRose
                                com.example.data.models.GameDifficulty.EXPERT -> AccentViolet
                            }

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = cardBg,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, borderColor) else androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        HapticManager.playTap(hapticFeedback)
                                        viewModel.setDifficulty(diff)
                                    }
                                    .testTag("difficulty_option_${diff.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(diff.badgeEmoji, fontSize = 24.sp)
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = diff.displayName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(100.dp),
                                                    color = borderColor.copy(alpha = 0.15f),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.4f))
                                                ) {
                                                    Text(
                                                        text = "${diff.scoreMultiplier}x XP",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = borderColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${diff.tagLine} • Time limit: ${diff.timeLimitSeconds}s",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = borderColor,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = PureWhite,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Haptic Feedback & Vibrations Section
            item {
                Text(
                    text = "HAPTIC FEEDBACK & TACTILE CONTROLS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Haptic Feedback", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Vibrate on taps, tile placements & game wins", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = appSettings.hapticsEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        HapticManager.hapticsEnabled = true
                                        HapticManager.playHeavyClick(hapticFeedback)
                                    }
                                    viewModel.toggleHaptics(enabled)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureWhite,
                                    checkedTrackColor = PrimaryIndigo,
                                    uncheckedThumbColor = PureWhite,
                                    uncheckedTrackColor = Slate300
                                ),
                                modifier = Modifier.testTag("haptics_master_toggle")
                            )
                        }

                        HorizontalDivider(color = extendedColors.subtleBorder.copy(alpha = 0.6f))

                        Text(
                            text = "TEST HAPTIC EFFECTS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.6.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    HapticManager.playLightTap(hapticFeedback)
                                },
                                shape = RoundedCornerShape(100.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                                modifier = Modifier.weight(1f).testTag("test_light_tap_btn")
                            ) {
                                Text("Light Tap", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            OutlinedButton(
                                onClick = {
                                    HapticManager.playTap(hapticFeedback)
                                },
                                shape = RoundedCornerShape(100.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                                modifier = Modifier.weight(1f).testTag("test_medium_tap_btn")
                            ) {
                                Text("Medium", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    HapticManager.playSuccess(hapticFeedback)
                                    SoundManager.playMatchSuccess()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.weight(1f).testTag("test_success_haptic_btn")
                            ) {
                                Text("Success 🎉", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }

                            Button(
                                onClick = {
                                    HapticManager.playError(hapticFeedback)
                                    SoundManager.playWrongMove()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.weight(1f).testTag("test_error_haptic_btn")
                            ) {
                                Text("Error ⚠️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }
                        }
                    }
                }
            }

            // Audio & Sound Controls Section
            item {
                Text(
                    text = "GLOBAL AUDIO & VOLUME CONTROLS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Sound Effects Switch & Controls
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = PrimaryIndigo.copy(alpha = 0.12f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🔊", fontSize = 20.sp)
                                        }
                                    }
                                    Column {
                                        Text("Sound Effects (SFX)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Cell taps, winning chimes & rewards", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = appSettings.soundEnabled,
                                    onCheckedChange = {
                                        HapticManager.playTap(hapticFeedback)
                                        viewModel.toggleSound(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = PrimaryIndigo,
                                        uncheckedThumbColor = PureWhite,
                                        uncheckedTrackColor = Slate300
                                    ),
                                    modifier = Modifier.testTag("sfx_master_toggle")
                                )
                            }

                            if (appSettings.soundEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Vol:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Slider(
                                        value = appSettings.soundVolume,
                                        onValueChange = { newVol ->
                                            viewModel.setSoundVolume(newVol)
                                        },
                                        valueRange = 0.0f..1.0f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = PrimaryIndigo,
                                            activeTrackColor = PrimaryIndigo,
                                            inactiveTrackColor = Slate200
                                        ),
                                        modifier = Modifier.weight(1f).testTag("sfx_volume_slider")
                                    )
                                    Text(
                                        text = "${(appSettings.soundVolume * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryIndigo,
                                        modifier = Modifier.width(38.dp),
                                        textAlign = TextAlign.End
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            HapticManager.playLightTap(hapticFeedback)
                                            viewModel.playSoundPreview()
                                        },
                                        shape = RoundedCornerShape(100.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.4f)),
                                        modifier = Modifier.height(32.dp).testTag("test_sfx_preview_btn")
                                    ) {
                                        Text("Play Test Sound 🎵", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PrimaryIndigo)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = extendedColors.subtleBorder.copy(alpha = 0.6f))

                        // Background Music Switch & Controls
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = AccentEmerald.copy(alpha = 0.12f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🎵", fontSize = 20.sp)
                                        }
                                    }
                                    Column {
                                        Text("Ambient Music (BGM)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Relaxing zen procedural chords", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = appSettings.musicEnabled,
                                    onCheckedChange = {
                                        HapticManager.playTap(hapticFeedback)
                                        viewModel.toggleMusic(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = AccentEmerald,
                                        uncheckedThumbColor = PureWhite,
                                        uncheckedTrackColor = Slate300
                                    ),
                                    modifier = Modifier.testTag("bgm_master_toggle")
                                )
                            }

                            if (appSettings.musicEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Vol:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Slider(
                                        value = appSettings.musicVolume,
                                        onValueChange = { newVol ->
                                            viewModel.setMusicVolume(newVol)
                                        },
                                        valueRange = 0.0f..1.0f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = AccentEmerald,
                                            activeTrackColor = AccentEmerald,
                                            inactiveTrackColor = Slate200
                                        ),
                                        modifier = Modifier.weight(1f).testTag("bgm_volume_slider")
                                    )
                                    Text(
                                        text = "${(appSettings.musicVolume * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentEmerald,
                                        modifier = Modifier.width(38.dp),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = extendedColors.subtleBorder.copy(alpha = 0.6f))

                        // Daily Notifications Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AccentAmber.copy(alpha = 0.12f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🔔", fontSize = 20.sp)
                                    }
                                }
                                Column {
                                    Text("Daily Challenge Notifications", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Get reminded to solve daily puzzles", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = appSettings.notificationsEnabled,
                                onCheckedChange = {
                                    HapticManager.playTap(hapticFeedback)
                                    viewModel.toggleNotifications(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureWhite,
                                    checkedTrackColor = PrimaryIndigo,
                                    uncheckedThumbColor = PureWhite,
                                    uncheckedTrackColor = Slate300
                                ),
                                modifier = Modifier.testTag("notifications_toggle")
                            )
                        }
                    }
                }
            }

            // Onboarding & Data Management
            item {
                Text(
                    text = "ONBOARDING & PREFERENCES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Onboarding Walkthrough", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = if (appSettings.hasCompletedOnboarding) "Status: Completed ✓" else "Status: Pending",
                                    fontSize = 12.sp,
                                    color = if (appSettings.hasCompletedOnboarding) AccentEmerald else AccentAmber
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    HapticManager.playTap(hapticFeedback)
                                    SoundManager.playTap()
                                    viewModel.resetOnboarding()
                                    onNavigateToOnboarding()
                                },
                                shape = RoundedCornerShape(100.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.testTag("replay_onboarding_btn")
                            ) {
                                Text("REPLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = extendedColors.subtleBorder.copy(alpha = 0.6f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Reset DataStore Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Restore defaults across all preferences", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(
                                onClick = {
                                    HapticManager.playTap(hapticFeedback)
                                    showResetDialog = true
                                },
                                modifier = Modifier.testTag("reset_settings_btn")
                            ) {
                                Text("RESET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                        }
                    }
                }
            }

            // AdMob Monetization & Publisher ID Configuration Guide
            item {
                Text(
                    text = "ADMOB SETUP & PUBLISHER ID GUIDE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = AccentAmber.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.3f)),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📢", fontSize = 22.sp)
                                }
                            }
                            Column {
                                Text(
                                    text = "AdMob Integration Guide",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "How to connect your AdMob account",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PrimaryIndigo.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "1. AdMob App ID (Manifest)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = PrimaryIndigo
                                )
                                Text(
                                    text = "File: AndroidManifest.xml\nUpdate meta-data 'com.google.android.gms.ads.APPLICATION_ID' to your AdMob App ID (format: ca-app-pub-XXXXXXXX~YYYYYYYY).",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = AccentEmerald.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "2. Ad Unit IDs (Banner & Rewarded)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AccentEmerald
                                )
                                Text(
                                    text = "File: com/example/ads/AdMobManager.kt\n• BANNER_AD_UNIT_ID (Home screen banner)\n• REWARDED_AD_UNIT_ID (Hint & Restart rewards)",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = extendedColors.cardBackgroundElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Active Ad Placements",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Home Banner + In-Game Rewarded Video",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = AccentEmeraldBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = AccentEmerald,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // About Section
            item {
                Text(
                    text = "ABOUT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("EAZY SOLVE GAMES", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Version 1.0.0 • Material 3 Dark & Light Modes Enabled", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Haptic tactile feedback and DataStore preferences fully active.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
