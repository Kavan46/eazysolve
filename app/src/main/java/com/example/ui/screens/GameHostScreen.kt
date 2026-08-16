package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ads.AdMobManager
import com.example.audio.SoundManager
import com.example.data.adaptive.AdaptiveDifficultyEngine
import com.example.data.models.GameCatalog
import com.example.data.models.GameType
import com.example.games.bubbleshooter.BubbleShooterGameScreen
import com.example.games.bubblesort.BubbleSortGameScreen
import com.example.games.crossclimb.CrossclimbGameScreen
import com.example.games.patches.PatchesGameScreen
import com.example.games.pinpoint.PinpointGameScreen
import com.example.games.queens.QueensGameScreen
import com.example.games.sudoku.SudokuGameScreen
import com.example.games.tango.TangoGameScreen
import com.example.games.tilematch.TileMatchGameScreen
import com.example.games.wend.WendGameScreen
import com.example.games.zip.ZipGameScreen
import com.example.ui.components.BottomGameControls
import com.example.ui.components.GameRulesOverlay
import com.example.ui.components.GameTopBar
import com.example.ui.components.PauseDialog
import com.example.ui.components.WinCelebrationDialog
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun GameHostScreen(
    viewModel: MainViewModel,
    gameType: GameType,
    levelNumber: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val gameInfo = remember(gameType) { GameCatalog.getGame(gameType) }
    val userStats by viewModel.userStats.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val adaptiveState by viewModel.adaptiveState.collectAsState()
    val difficulty = appSettings.difficulty

    val adaptiveParams = remember(gameType, difficulty, adaptiveState) {
        AdaptiveDifficultyEngine.getAdaptiveLevelParameters(gameType, difficulty, adaptiveState)
    }

    var freeHintsRemaining by remember(gameType, levelNumber, adaptiveParams.freeHints) {
        mutableIntStateOf(adaptiveParams.freeHints)
    }

    val hasSeenTutorial = remember(appSettings.seenTutorials, gameType) {
        appSettings.seenTutorials.contains(gameType.key)
    }
    var showRulesOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(appSettings.seenTutorials, gameType) {
        if (!appSettings.seenTutorials.contains(gameType.key)) {
            showRulesOverlay = true
        }
    }

    var timerSeconds by remember(gameType, levelNumber) { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }

    // Win details
    var winScore by remember { mutableIntStateOf(0) }
    var winTime by remember { mutableIntStateOf(0) }
    var winStars by remember { mutableIntStateOf(3) }
    var coinsReward by remember { mutableIntStateOf(30) }

    // Control Triggers
    var undoRequested by remember { mutableStateOf(false) }
    var hintRequested by remember { mutableStateOf(false) }
    var restartRequested by remember { mutableStateOf(false) }

    // Timer loop - pauses when rules overlay or pause dialog or win dialog is active
    LaunchedEffect(isPaused, showWinDialog, showRulesOverlay, gameType, levelNumber) {
        timerSeconds = 0
        while (!isPaused && !showWinDialog && !showRulesOverlay) {
            delay(1000)
            timerSeconds++
        }
    }

    fun handleWin(score: Int, timeSec: Int, stars: Int) {
        val effectiveMultiplier = if (adaptiveState.isEnabled) adaptiveParams.scoreMultiplier else difficulty.scoreMultiplier
        winScore = (score * effectiveMultiplier).toInt()
        winTime = timeSec
        winStars = stars
        coinsReward = (20 * stars * effectiveMultiplier).toInt()
        showWinDialog = true
        SoundManager.playLevelComplete()
        viewModel.onGameWon(gameType, levelNumber, winScore, timeSec, stars)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GameTopBar(
                title = gameInfo.name,
                levelNumber = levelNumber,
                timerSeconds = timerSeconds,
                gameType = gameType,
                difficulty = difficulty,
                challengeTimerEnabled = true,
                adaptiveTier = if (adaptiveState.isEnabled) adaptiveState.currentTier else null,
                adaptiveScoreMultiplier = if (adaptiveState.isEnabled) adaptiveParams.scoreMultiplier else null,
                timeLimitOverrideSeconds = if (adaptiveState.isEnabled) adaptiveParams.timeLimitSeconds else null,
                onBack = onNavigateBack,
                onPause = { isPaused = true },
                onHelpClick = { showRulesOverlay = true },
                onDifficultyClick = { isPaused = true }
            )
        },
        bottomBar = {
            BottomGameControls(
                onUndo = {
                    SoundManager.playTap()
                    undoRequested = true
                },
                onHint = {
                    val activity = context as? Activity
                    if (activity != null) {
                        AdMobManager.showRewardedAd(
                            activity = activity,
                            onUserEarnedReward = { _, _ ->
                                SoundManager.playHint()
                                hintRequested = true
                            },
                            onAdClosedOrSkipped = {
                                SoundManager.playHint()
                                hintRequested = true
                            }
                        )
                    } else {
                        SoundManager.playHint()
                        hintRequested = true
                    }
                },
                onRestart = {
                    viewModel.recordGameRestart(gameType, levelNumber)
                    val activity = context as? Activity
                    if (activity != null) {
                        AdMobManager.showRewardedAd(
                            activity = activity,
                            onUserEarnedReward = { _, _ ->
                                restartRequested = true
                            },
                            onAdClosedOrSkipped = {
                                restartRequested = true
                            }
                        )
                    } else {
                        restartRequested = true
                    }
                },
                coins = userStats?.coins ?: 100,
                freeHintsAvailable = freeHintsRemaining
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (gameType) {
                GameType.ZIP -> ZipGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
                GameType.SUDOKU -> SudokuGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
                GameType.TANGO -> TangoGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
                GameType.QUEENS -> QueensGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
                GameType.CROSSCLIMB -> CrossclimbGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
                GameType.PINPOINT -> PinpointGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
                GameType.WEND -> WendGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
                GameType.PATCHES -> PatchesGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
                GameType.BUBBLE_SORT -> BubbleSortGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
                GameType.BUBBLE_SHOOTER -> BubbleShooterGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
                GameType.TILE_MATCH -> TileMatchGameScreen(
                    levelNumber = levelNumber,
                    difficulty = difficulty,
                    onWin = ::handleWin,
                    onUndoRequested = undoRequested,
                    onHintRequested = hintRequested,
                    onRestartRequested = restartRequested,
                    resetControlTriggers = {
                        undoRequested = false
                        hintRequested = false
                        restartRequested = false
                    }
                )
            }
        }

        // Game Rules Overlay (shows on first open or when '?' is tapped)
        if (showRulesOverlay) {
            GameRulesOverlay(
                gameInfo = gameInfo,
                onDismiss = {
                    showRulesOverlay = false
                    viewModel.markGameTutorialSeen(gameType)
                }
            )
        }

        // Pause Dialog
        if (isPaused) {
            PauseDialog(
                onResume = { isPaused = false },
                onRestart = {
                    isPaused = false
                    restartRequested = true
                },
                onHome = {
                    isPaused = false
                    onNavigateBack()
                },
                soundEnabled = userStats?.soundEnabled ?: true,
                onToggleSound = {
                    viewModel.toggleSound(!(userStats?.soundEnabled ?: true))
                },
                difficulty = difficulty,
                onSelectDifficulty = { newDiff ->
                    viewModel.setDifficulty(newDiff)
                    restartRequested = true
                }
            )
        }

        // Win Dialog
        if (showWinDialog) {
            WinCelebrationDialog(
                score = winScore,
                timeSeconds = winTime,
                stars = winStars,
                coinsEarned = coinsReward,
                xpEarned = (50 * difficulty.scoreMultiplier).toInt(),
                difficulty = difficulty,
                onNextLevel = {
                    showWinDialog = false
                    viewModel.nextLevel()
                },
                onRetry = {
                    showWinDialog = false
                    restartRequested = true
                },
                onHome = {
                    showWinDialog = false
                    onNavigateBack()
                },
                onShare = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "I just completed Level $levelNumber (${difficulty.displayName}) in ${gameInfo.name} with $winStars ⭐ and score $winScore on Eazy Solve Games!"
                        )
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Score"))
                }
            )
        }
    }
}
