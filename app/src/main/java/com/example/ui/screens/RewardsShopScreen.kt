package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.ui.components.CoinBadge
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsShopScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val userStats by viewModel.userStats.collectAsState()
    var mysteryBoxOpened by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                            text = "REWARDS & SHOP",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily Mystery Gift Banner
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = AccentPurpleBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurpleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PureWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurpleBorder),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (mysteryBoxOpened) "🎉" else "🎁", fontSize = 32.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (mysteryBoxOpened) "CLAIMED +100 COINS!" else "DAILY MYSTERY GIFT",
                            color = AccentPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (mysteryBoxOpened) "Come back tomorrow for another reward!" else "Tap to open your daily lucky bonus box!",
                            color = Slate600,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (!mysteryBoxOpened) {
                                    SoundManager.playCoinReward()
                                    mysteryBoxOpened = true
                                    viewModel.claimDailyBonus()
                                }
                            },
                            enabled = !mysteryBoxOpened,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Slate900,
                                disabledContainerColor = Slate300
                            ),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("open_mystery_btn")
                        ) {
                            Text(
                                text = if (mysteryBoxOpened) "OPENED ✓" else "OPEN GIFT",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Power-ups & Items Section
            item {
                Text(
                    text = "POWER-UPS & CUSTOMIZATIONS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = Slate500,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            val shopItems = listOf(
                Triple("💡 5x Power Hints", "Get instant solutions to any stuck puzzle step", 100),
                Triple("🔄 Infinite Undo", "Rewind any wrong moves without penalty", 150),
                Triple("🎨 Geometric Neon Theme", "Unlock vibrant color themes for all puzzle grids", 300),
                Triple("👑 Royal Crown Badge", "Equip premium golden profile banner", 500)
            )

            items(shopItems.size) { idx ->
                val item = shopItems[idx]
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Slate50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.first.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                            Text(
                                text = item.second,
                                fontSize = 12.sp,
                                color = Slate500,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                viewModel.spendCoins(item.third) { success ->
                                    if (success) {
                                        SoundManager.playCoinReward()
                                    } else {
                                        SoundManager.playWrongMove()
                                    }
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("${item.third} 🪙", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PureWhite)
                        }
                    }
                }
            }
        }
    }
}

