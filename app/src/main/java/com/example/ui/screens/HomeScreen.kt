package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.BannerAdView
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.adaptive.AdaptiveTier
import com.example.data.models.GameCatalog
import com.example.data.models.GameCategory
import com.example.data.models.GameInfo
import com.example.data.models.GameType
import com.example.ui.components.CoinBadge
import com.example.ui.components.EnhancedBottomBar
import com.example.ui.components.NavTab
import com.example.ui.components.StreakBadge
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToGame: (GameType, Int) -> Unit,
    onNavigateToDaily: () -> Unit,
    onNavigateToWeekly: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val progressList by viewModel.gameProgressList.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val todayDaily by viewModel.todayDailyChallenge.collectAsState()
    val isTodayDailyCompleted by viewModel.isTodayDailyCompleted.collectAsState()
    val adaptiveState by viewModel.adaptiveState.collectAsState()
    val unclaimedBadgesCount by viewModel.unclaimedAchievementsCount.collectAsState()
    val extendedColors = LocalExtendedColors.current

    var showAdaptiveDialog by remember { mutableStateOf(false) }

    if (showAdaptiveDialog) {
        AlertDialog(
            onDismissRequest = { showAdaptiveDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(adaptiveState.currentTier.badgeEmoji, fontSize = 22.sp)
                    Column {
                        Text(
                            text = "Adaptive Difficulty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "AI Dynamic Calibration",
                            fontSize = 11.sp,
                            color = extendedColors.textMuted
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status Banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when (adaptiveState.currentTier) {
                            AdaptiveTier.MASTER_FLOW -> AccentOrangeBg
                            AdaptiveTier.CHALLENGER -> PastelAmber
                            AdaptiveTier.OPTIMAL -> PrimaryIndigo.copy(alpha = 0.1f)
                            AdaptiveTier.SUPPORTIVE -> AccentEmeraldBg
                            AdaptiveTier.RECOVERY -> PastelViolet
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (adaptiveState.currentTier) {
                                AdaptiveTier.MASTER_FLOW -> AccentOrange
                                AdaptiveTier.CHALLENGER -> AccentAmber
                                AdaptiveTier.OPTIMAL -> PrimaryIndigo.copy(alpha = 0.3f)
                                AdaptiveTier.SUPPORTIVE -> AccentEmerald
                                AdaptiveTier.RECOVERY -> AccentViolet
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = adaptiveState.currentTier.title.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp,
                                    color = when (adaptiveState.currentTier) {
                                        AdaptiveTier.MASTER_FLOW -> AccentOrange
                                        AdaptiveTier.CHALLENGER -> Color(0xFFB45309)
                                        AdaptiveTier.OPTIMAL -> PrimaryIndigo
                                        AdaptiveTier.SUPPORTIVE -> AccentEmerald
                                        AdaptiveTier.RECOVERY -> AccentViolet
                                    }
                                )
                                Text(
                                    text = "${adaptiveState.adaptiveScore}/100 Mastery",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = adaptiveState.currentTier.subtitle,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Dynamic Metrics Grid
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = extendedColors.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("WIN STREAK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = extendedColors.textMuted)
                                Text("${adaptiveState.winStreak} 🔥", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AccentOrange)
                            }
                            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = extendedColors.subtleBorder)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ACCURACY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = extendedColors.textMuted)
                                Text("${(adaptiveState.recentSuccessRate * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AccentEmerald)
                            }
                            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = extendedColors.subtleBorder)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SCORE BOOST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = extendedColors.textMuted)
                                Text(
                                    if (adaptiveState.scoreBonusPercent > 0) "+${adaptiveState.scoreBonusPercent}%" else "1.0x",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryIndigo
                                )
                            }
                        }
                    }

                    // Enable/Disable Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(extendedColors.cardBackground)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Adaptive Scaling", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Auto-tunes timers, hints & score", fontSize = 10.sp, color = extendedColors.textMuted)
                        }
                        Switch(
                            checked = adaptiveState.isEnabled,
                            onCheckedChange = {
                                HapticManager.playTap()
                                viewModel.toggleAdaptiveDifficulty(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryIndigo
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticManager.playLightTap()
                        showAdaptiveDialog = false
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Got It", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    val progressMap = remember(progressList) {
        progressList.associateBy { it.gameKey }
    }

    val totalLevelsCleared = remember(progressList) {
        progressList.sumOf { (it.highestLevel - 1).coerceAtLeast(0) }
    }
    val totalStars = remember(progressList) {
        progressList.sumOf { it.totalStars }
    }

    val filteredGames = remember(selectedCategory, searchQuery, progressList) {
        GameCatalog.games.filter { game ->
            val matchesCategory = when (selectedCategory) {
                GameCategory.ALL -> true
                GameCategory.FAVORITES -> progressMap[game.type.key]?.isFavorite == true
                else -> game.category == selectedCategory
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                game.name.contains(searchQuery, ignoreCase = true) ||
                        game.description.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, extendedColors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Geometric Brand Mark with Mascot Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryIndigo,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_eazy_solve_logo),
                                contentDescription = "Eazy Solve Games Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Text(
                                text = "Eazy Solve",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "GAMES",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 2.sp,
                                color = PrimaryIndigo
                            )
                        }
                    }

                    // Top Actions: Streak, Coins & Profile Avatar / Settings
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoinBadge(
                            coins = userStats?.coins ?: 100,
                            onClick = {
                                HapticManager.playTap()
                                SoundManager.playTap()
                                onNavigateToRewards()
                            },
                            modifier = Modifier.testTag("home_coin_badge")
                        )
                        StreakBadge(
                            streakDays = userStats?.currentStreak ?: 1,
                            onClick = {
                                HapticManager.playTap()
                                SoundManager.playTap()
                                onNavigateToDaily()
                            },
                            modifier = Modifier.testTag("home_streak_badge")
                        )
                        Surface(
                            shape = CircleShape,
                            color = extendedColors.cardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                            modifier = Modifier
                                .size(38.dp)
                                .clickable {
                                    HapticManager.playTap()
                                    SoundManager.playTap()
                                    onNavigateToSettings()
                                }
                                .testTag("home_settings_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                BannerAdView(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
                EnhancedBottomBar(
                    currentTab = NavTab.HOME,
                    onTabSelected = { tab ->
                        when (tab) {
                            NavTab.HOME -> {}
                            NavTab.DAILY -> onNavigateToDaily()
                            NavTab.BADGES -> onNavigateToAchievements()
                            NavTab.PROFILE -> onNavigateToProfile()
                        }
                    },
                    isDailyCompleted = isTodayDailyCompleted,
                    unclaimedBadgesCount = unclaimedBadgesCount
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stats Snapshot Bar (Brain IQ, Total Solved, Total Stars, Streak)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = extendedColors.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🧠 BRAIN IQ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.textMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${120 + ((userStats?.xp ?: 0) / 25)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryIndigo
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(1.dp),
                                color = extendedColors.subtleBorder
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🏆 SOLVED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.textMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$totalLevelsCleared",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentEmerald
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(1.dp),
                                color = extendedColors.subtleBorder
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⭐ STARS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.textMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$totalStars",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentAmber
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(1.dp),
                                color = extendedColors.subtleBorder
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🔥 STREAK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.textMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${userStats?.currentStreak ?: 1}d",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentOrange
                                )
                            }
                        }
                    }

                    // Adaptive Difficulty Live Status Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when (adaptiveState.currentTier) {
                            AdaptiveTier.MASTER_FLOW -> AccentOrangeBg
                            AdaptiveTier.CHALLENGER -> PastelAmber
                            AdaptiveTier.OPTIMAL -> extendedColors.cardBackground
                            AdaptiveTier.SUPPORTIVE -> AccentEmeraldBg
                            AdaptiveTier.RECOVERY -> PastelViolet
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (adaptiveState.currentTier) {
                                AdaptiveTier.MASTER_FLOW -> AccentOrange.copy(alpha = 0.4f)
                                AdaptiveTier.CHALLENGER -> AccentAmber.copy(alpha = 0.4f)
                                AdaptiveTier.OPTIMAL -> extendedColors.subtleBorder
                                AdaptiveTier.SUPPORTIVE -> AccentEmerald.copy(alpha = 0.4f)
                                AdaptiveTier.RECOVERY -> AccentViolet.copy(alpha = 0.4f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticManager.playTap()
                                SoundManager.playTap()
                                showAdaptiveDialog = true
                            }
                            .testTag("home_adaptive_flow_pill")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(adaptiveState.currentTier.badgeEmoji, fontSize = 16.sp)
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "ADAPTIVE ENGINE: ${adaptiveState.currentTier.title.uppercase()}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.4.sp,
                                            color = when (adaptiveState.currentTier) {
                                                AdaptiveTier.MASTER_FLOW -> AccentOrange
                                                AdaptiveTier.CHALLENGER -> Color(0xFFB45309)
                                                AdaptiveTier.OPTIMAL -> PrimaryIndigo
                                                AdaptiveTier.SUPPORTIVE -> AccentEmerald
                                                AdaptiveTier.RECOVERY -> AccentViolet
                                            }
                                        )
                                        if (adaptiveState.scoreBonusPercent > 0) {
                                            Surface(
                                                shape = CircleShape,
                                                color = PrimaryIndigo.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "+${adaptiveState.scoreBonusPercent}% BOOST",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = adaptiveState.currentTier.subtitle,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Adaptive Info",
                                tint = extendedColors.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search 11 brain puzzles...", color = extendedColors.textMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = extendedColors.textMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                HapticManager.playLightTap()
                                viewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = extendedColors.textMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = extendedColors.cardBackground,
                        unfocusedContainerColor = extendedColors.cardBackground,
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = extendedColors.cardBorder,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar")
                )
            }

            // Category Filter Pills
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(GameCategory.values()) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) PrimaryIndigo else extendedColors.cardBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) PrimaryIndigo else extendedColors.subtleBorder
                            ),
                            modifier = Modifier
                                .clickable {
                                    HapticManager.playLightTap()
                                    SoundManager.playTap()
                                    viewModel.setCategory(cat)
                                }
                                .testTag("cat_chip_${cat.name.lowercase()}")
                        ) {
                            Text(
                                text = when (cat) {
                                    GameCategory.ALL -> "✨ All (11)"
                                    GameCategory.LOGIC -> "🧩 Logic"
                                    GameCategory.WORDS -> "🔤 Words"
                                    GameCategory.MATCHING -> "🎯 Matching"
                                    GameCategory.ACTION -> "⚡ Action"
                                    GameCategory.DAILY -> "📅 Daily"
                                    GameCategory.FAVORITES -> "❤️ Favorites"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Daily Challenge Feature Card
            item {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = extendedColors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isTodayDailyCompleted) AccentEmerald.copy(alpha = 0.6f) else extendedColors.cardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticManager.playTap()
                            SoundManager.playTap()
                            onNavigateToDaily()
                        }
                        .testTag("home_daily_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isTodayDailyCompleted) AccentEmeraldBg else (if (extendedColors.isDark) Color(0xFF431407) else AccentOrangeBg),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (isTodayDailyCompleted) "🌟" else "☀️", fontSize = 30.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = todayDaily.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (isTodayDailyCompleted) AccentEmeraldBg else (if (extendedColors.isDark) Color(0xFF271306) else PureWhite),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isTodayDailyCompleted) AccentEmeraldBorder else AccentOrangeBorder
                                    )
                                ) {
                                    Text(
                                        text = if (isTodayDailyCompleted) "COMPLETED ✅" else "+100 🪙 BONUS",
                                        color = if (isTodayDailyCompleted) AccentEmerald else AccentOrange,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isTodayDailyCompleted) "Daily challenge solved! Streak protected & rewards claimed." else todayDaily.constraintDescription,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    HapticManager.playTap()
                                    SoundManager.playTap()
                                    onNavigateToDaily()
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTodayDailyCompleted) AccentEmerald else PrimaryIndigo
                                ),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (isTodayDailyCompleted) "VIEW DAILY BADGE" else "PLAY DAILY CHALLENGE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Game Cards List
            items(filteredGames, key = { it.type.name }) { game ->
                val progress = progressMap[game.type.key]
                val level = progress?.highestLevel ?: 1
                val isFav = progress?.isFavorite ?: false
                val streak = progress?.currentStreak ?: 0
                val stars = progress?.totalStars ?: 0

                GeometricGameCardItem(
                    game = game,
                    level = level,
                    streakDays = streak,
                    totalStars = stars,
                    isFavorite = isFav,
                    onPlay = {
                        HapticManager.playTap()
                        SoundManager.playTap()
                        viewModel.launchGame(game.type, level)
                        onNavigateToGame(game.type, level)
                    },
                    onToggleFavorite = {
                        HapticManager.playLightTap()
                        SoundManager.playTap()
                        viewModel.toggleFavorite(game.type)
                    }
                )
            }
        }
    }
}

@Composable
fun GeometricGameCardItem(
    game: GameInfo,
    level: Int,
    streakDays: Int,
    totalStars: Int,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val (iconBg, badgeBorder, badgeText) = when (game.type) {
        GameType.ZIP -> Triple(if (extendedColors.isDark) Color(0xFF431407) else AccentOrangeBg, AccentOrangeBorder, AccentOrange)
        GameType.SUDOKU -> Triple(if (extendedColors.isDark) Color(0xFF1E3A8A) else AccentBlueBg, AccentBlueBorder, AccentBlue)
        GameType.TANGO -> Triple(if (extendedColors.isDark) Color(0xFF064E3B) else AccentEmeraldBg, AccentEmeraldBorder, AccentEmerald)
        GameType.QUEENS -> Triple(if (extendedColors.isDark) Color(0xFF4C0519) else AccentRoseBg, AccentRoseBorder, AccentRose)
        GameType.CROSSCLIMB -> Triple(if (extendedColors.isDark) Color(0xFF3B0764) else AccentPurpleBg, AccentPurpleBorder, AccentPurple)
        GameType.PINPOINT -> Triple(if (extendedColors.isDark) Color(0xFF1E3A8A) else AccentBlueBg, AccentBlueBorder, AccentBlue)
        GameType.WEND -> Triple(if (extendedColors.isDark) Color(0xFF451A03) else AccentAmberBg, AccentAmberBorder, AccentAmber)
        GameType.PATCHES -> Triple(if (extendedColors.isDark) Color(0xFF431407) else AccentOrangeBg, AccentOrangeBorder, AccentOrange)
        GameType.BUBBLE_SORT -> Triple(if (extendedColors.isDark) Color(0xFF3B0764) else AccentPurpleBg, AccentPurpleBorder, AccentPurple)
        GameType.BUBBLE_SHOOTER -> Triple(if (extendedColors.isDark) Color(0xFF064E3B) else AccentEmeraldBg, AccentEmeraldBorder, AccentEmerald)
        GameType.TILE_MATCH -> Triple(if (extendedColors.isDark) Color(0xFF1E3A8A) else AccentBlueBg, AccentBlueBorder, AccentBlue)
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = extendedColors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_card_${game.type.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Squircle Icon Container
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = iconBg,
                shadowElevation = 0.5.dp,
                modifier = Modifier.size(62.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = game.symbolEmoji, fontSize = 30.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Right Content Column
            Column(modifier = Modifier.weight(1f)) {
                // Top Row: Title + Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = game.name.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = CircleShape,
                        color = if (extendedColors.isDark) extendedColors.cardBackgroundElevated else PureWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = if (streakDays > 0) "🔥 ${streakDays}D" else if (level > 1) "⭐ LVL $level" else "NEW",
                            color = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = game.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Row: [PLAY] [FAV]
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPlay,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("play_btn_${game.type.name.lowercase()}")
                    ) {
                        Text(
                            text = if (level > 1) "LEVEL $level" else "PLAY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onToggleFavorite,
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = extendedColors.cardBackground,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.subtleBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) HeartRed else extendedColors.textMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFavorite) "SAVED" else "FAV",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
