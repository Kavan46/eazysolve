package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.db.AchievementEntity
import com.example.ui.components.CoinBadge
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val achievements by viewModel.achievements.collectAsState()
    val userStats by viewModel.userStats.collectAsState()

    Scaffold(
        containerColor = PureWhite,
        topBar = {
            Surface(
                color = PureWhite,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate100)
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
                            color = Slate50,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800, modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(
                            text = "ACHIEVEMENTS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp,
                            color = Slate900
                        )
                    }

                    CoinBadge(coins = userStats?.coins ?: 100)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PureWhite),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements, key = { it.id }) { ach ->
                val isUnlocked = ach.isUnlocked
                val isClaimed = ach.isRewardClaimed
                val progressFraction = (ach.currentValue.toFloat() / ach.targetValue.toFloat()).coerceIn(0f, 1f)

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = if (isUnlocked) PureWhite else Slate50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnlocked) Slate200 else Slate100),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ach_item_${ach.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isUnlocked) AccentAmberBg else Slate100,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnlocked) AccentAmberBorder else Slate200),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isUnlocked) ach.iconEmoji else "🔒",
                                    fontSize = 28.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ach.title.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                            Text(
                                text = ach.description,
                                fontSize = 12.sp,
                                color = Slate500,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = if (isUnlocked) AccentEmerald else PrimaryIndigo,
                                trackColor = Slate100
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${ach.currentValue} / ${ach.targetValue}",
                                fontSize = 11.sp,
                                color = Slate400,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        if (isUnlocked && !isClaimed) {
                            Button(
                                onClick = {
                                    SoundManager.playTap()
                                    viewModel.claimAchievement(ach.id)
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("claim_btn_${ach.id}")
                            ) {
                                Text("+${ach.rewardCoins} 🪙", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PureWhite)
                            }
                        } else if (isClaimed) {
                            Surface(
                                shape = CircleShape,
                                color = AccentEmeraldBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmeraldBorder)
                            ) {
                                Text(
                                    text = "CLAIMED ✓",
                                    color = AccentEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

