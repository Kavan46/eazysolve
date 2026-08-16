package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.ui.components.CoinBadge
import com.example.ui.components.StreakBadge
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val avatar: String,
    val score: Int,
    val league: String,
    val isCurrentUser: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRewards: () -> Unit
) {
    val userStats by viewModel.userStats.collectAsState()
    val progressList by viewModel.gameProgressList.collectAsState()
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val syncStatus by viewModel.cloudSyncStatus.collectAsState()
    val extendedColors = LocalExtendedColors.current

    val totalLevelsCleared = progressList.sumOf { (it.highestLevel - 1).coerceAtLeast(0) }
    val totalStars = progressList.sumOf { it.totalStars }

    val isGoogleSignedIn = userProfile.isSignedIn
    val googleAccountEmail = userProfile.email.ifEmpty { "kmatrixstudio@gmail.com" }
    val googleDisplayName = userProfile.displayName.ifEmpty { "Puzzle Champion" }
    var showSignInDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    val sampleLeaderboard = remember(totalStars, isGoogleSignedIn, googleDisplayName) {
        listOf(
            LeaderboardEntry(1, "Alex_Solver", "🦁", 14200, "Diamond 💎"),
            LeaderboardEntry(2, "MindMaster99", "⚡", 12850, "Diamond 💎"),
            LeaderboardEntry(3, "NovaPuzzle", "🪐", 11400, "Platinum 🏆"),
            LeaderboardEntry(4, if (isGoogleSignedIn) googleDisplayName else "You (Guest)", "🧩", 4500 + totalStars * 150, "Gold 🥇", true),
            LeaderboardEntry(5, "Elena_Logic", "🦊", 3800, "Gold 🥇"),
            LeaderboardEntry(6, "ZenPuzzler", "🌸", 3200, "Silver 🥈"),
            LeaderboardEntry(7, "CodeWalker", "🚀", 2900, "Silver 🥈")
        )
    }

    if (showSignInDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSigningIn) showSignInDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("G", fontWeight = FontWeight.Black, fontSize = 24.sp, color = PrimaryIndigo)
                    Text(if (isGoogleSignedIn) "Google Account" else "Sign in with Google", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isGoogleSignedIn) {
                        Text("Connected Account:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(googleAccountEmail, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryIndigo)
                        Text("Cloud sync status: ${syncStatus ?: "Active"}", fontSize = 12.sp, color = AccentEmerald, fontWeight = FontWeight.SemiBold)
                        Text("Your scores, stars, and daily streaks are securely synced with Firebase Cloud Firestore.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("Connect your Google Account to automatically back up progress, sync across devices, and compete on Global Leaderboards!", fontSize = 13.sp)
                        errorMessage?.let {
                            Text(it, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isGoogleSignedIn) {
                            viewModel.signOut()
                            showSignInDialog = false
                            SoundManager.playTap()
                            HapticManager.playTap()
                        } else {
                            isSigningIn = true
                            errorMessage = null
                            viewModel.signInWithGoogle { success, error ->
                                isSigningIn = false
                                if (success) {
                                    showSignInDialog = false
                                    SoundManager.playMatchSuccess()
                                    HapticManager.playSuccess()
                                } else {
                                    errorMessage = error
                                    HapticManager.playError()
                                }
                            }
                        }
                    },
                    enabled = !isSigningIn,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isGoogleSignedIn) ErrorRed else PrimaryIndigo)
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isGoogleSignedIn) "Sign Out" else "Sign In with Google")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignInDialog = false },
                    enabled = !isSigningIn
                ) {
                    Text("Cancel")
                }
            }
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
                    .border(1.dp, extendedColors.cardBorder)
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
                            border = BorderStroke(1.dp, extendedColors.subtleBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(onClick = {
                                HapticManager.playLightTap()
                                SoundManager.playTap()
                                onNavigateBack()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(
                            text = "PLAYER PROFILE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = extendedColors.cardBackground,
                        border = BorderStroke(1.dp, extendedColors.subtleBorder),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(
                            onClick = {
                                HapticManager.playTap()
                                SoundManager.playTap()
                                onNavigateToSettings()
                            }
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
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
            // Profile Card with Avatar & Google Account status
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.cardBackground,
                    border = BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = if (isGoogleSignedIn) AccentEmerald.copy(alpha = 0.15f) else PrimaryIndigo.copy(alpha = 0.12f),
                            border = BorderStroke(1.5.dp, if (isGoogleSignedIn) AccentEmerald else PrimaryIndigo.copy(alpha = 0.4f)),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (isGoogleSignedIn) "🏆" else "🧩", fontSize = 40.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isGoogleSignedIn) googleDisplayName else "PUZZLE MASTER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isGoogleSignedIn) googleAccountEmail else "Guest Account • Level ${(userStats?.xp ?: 0) / 100 + 1}",
                            fontSize = 12.sp,
                            color = if (isGoogleSignedIn) AccentEmerald else PrimaryIndigo,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Google Sign In / Account Config Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isGoogleSignedIn) AccentEmerald.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.5.dp,
                                if (isGoogleSignedIn) AccentEmerald.copy(alpha = 0.5f) else extendedColors.cardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticManager.playTap()
                                    SoundManager.playTap()
                                    showSignInDialog = true
                                }
                                .testTag("profile_google_signin_btn")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isGoogleSignedIn) AccentEmerald else PrimaryIndigo,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = if (isGoogleSignedIn) "Google Connected" else "Sign in with Google",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isGoogleSignedIn) "Cloud Sync Active • Tap to manage" else "Sync progress across devices",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    if (isGoogleSignedIn) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = if (isGoogleSignedIn) AccentEmerald else extendedColors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinBadge(
                                coins = userStats?.coins ?: 100,
                                onClick = {
                                    HapticManager.playTap()
                                    SoundManager.playTap()
                                    onNavigateToRewards()
                                }
                            )
                            StreakBadge(streakDays = userStats?.currentStreak ?: 1)
                        }
                    }
                }
            }

            // SHOP & REWARDS Quick Access Section
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = extendedColors.cardBackground,
                    border = BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticManager.playTap()
                            SoundManager.playTap()
                            onNavigateToRewards()
                        }
                        .testTag("profile_shop_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = AccentAmberBg,
                                border = BorderStroke(1.dp, AccentAmberBorder),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🛍️", fontSize = 28.sp)
                                }
                            }
                            Column {
                                Text(
                                    text = "REWARDS & COIN SHOP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Unlock themes, power-ups & daily gifts",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Shop",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // LEADERBOARDS Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GLOBAL LEADERBOARDS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp,
                        color = extendedColors.textMuted
                    )
                    Surface(
                        shape = CircleShape,
                        color = AccentPurpleBg,
                        border = BorderStroke(1.dp, AccentPurpleBorder)
                    ) {
                        Text(
                            text = "🏆 Gold League #4",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentPurple,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Leaderboard list card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = extendedColors.cardBackground,
                    border = BorderStroke(1.dp, extendedColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sampleLeaderboard.forEach { entry ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (entry.isCurrentUser) PrimaryIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (entry.isCurrentUser) PrimaryIndigo else extendedColors.subtleBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Rank badge
                                        Surface(
                                            shape = CircleShape,
                                            color = when (entry.rank) {
                                                1 -> AccentAmber
                                                2 -> Color(0xFF94A3B8)
                                                3 -> Color(0xFFB45309)
                                                else -> extendedColors.cardBorder
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${entry.rank}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (entry.rank <= 3) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        Text(entry.avatar, fontSize = 20.sp)

                                        Column {
                                            Text(
                                                text = entry.name,
                                                fontWeight = if (entry.isCurrentUser) FontWeight.Black else FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (entry.isCurrentUser) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = entry.league,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${entry.score} pts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (entry.isCurrentUser) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stats Grid
            item {
                Text(
                    text = "CAREER STATISTICS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = extendedColors.textMuted,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Levels Solved",
                        value = "$totalLevelsCleared",
                        emoji = "🏆",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Stars Earned",
                        value = "$totalStars ⭐",
                        emoji = "✨",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Best Streak",
                        value = "${userStats?.longestStreak ?: 1} Days",
                        emoji = "🔥",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Games",
                        value = "${userStats?.totalGamesPlayed ?: 0}",
                        emoji = "🎯",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = extendedColors.cardBackground,
        border = BorderStroke(1.dp, extendedColors.cardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = extendedColors.textMuted
            )
        }
    }
}
