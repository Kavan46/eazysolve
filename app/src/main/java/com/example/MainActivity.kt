package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ads.AdMobManager
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.datastore.ThemeMode
import com.example.data.models.GameType
import com.example.ui.screens.*
import com.example.ui.theme.EazySolveTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HapticManager.init(applicationContext)
        AdMobManager.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val appSettings by mainViewModel.appSettings.collectAsState()
            val isSystemDark = isSystemInDarkTheme()

            val isDarkTheme = when (appSettings.themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            EazySolveTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = mainViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SoundManager.onAppResume()
    }

    override fun onPause() {
        super.onPause()
        SoundManager.onAppPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.stopBackgroundMusic()
    }
}

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val GAME_HOST = "game/{gameType}/{levelNumber}"
    const val DAILY = "daily"
    const val ACHIEVEMENTS = "achievements"
    const val PROFILE = "profile"
    const val REWARDS = "rewards"
    const val SETTINGS = "settings"

    fun gameRoute(type: GameType, level: Int = 1) = "game/${type.name}/$level"
}

@Composable
fun AppNavigation(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val appSettings by viewModel.appSettings.collectAsState()
    val activeLevelNumber by viewModel.activeLevelNumber.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 320))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 200))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 320))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 200))
        }
    ) {
        composable(
            route = Routes.SPLASH,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            SplashScreen(
                onSplashFinished = {
                    val targetRoute = if (!appSettings.hasCompletedOnboarding) {
                        Routes.ONBOARDING
                    } else {
                        Routes.HOME
                    }
                    navController.navigate(targetRoute) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.ONBOARDING,
            enterTransition = { fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            OnboardingScreen(
                viewModel = viewModel,
                onFinishOnboarding = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.HOME,
            enterTransition = {
                fadeIn(animationSpec = tween(320)) + scaleIn(initialScale = 0.96f, animationSpec = tween(320))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            }
        ) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToGame = { gameType, level ->
                    navController.navigate(Routes.gameRoute(gameType, level))
                },
                onNavigateToDaily = {
                    navController.navigate(Routes.DAILY)
                },
                onNavigateToWeekly = {
                    navController.navigate(Routes.DAILY)
                },
                onNavigateToAchievements = {
                    navController.navigate(Routes.ACHIEVEMENTS)
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
                },
                onNavigateToRewards = {
                    navController.navigate(Routes.REWARDS)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(
            route = Routes.GAME_HOST,
            arguments = listOf(
                navArgument("gameType") { type = NavType.StringType },
                navArgument("levelNumber") { type = NavType.IntType }
            ),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight / 3 },
                    animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)) +
                scaleIn(initialScale = 0.92f, animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight / 3 },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 200)) +
                scaleOut(targetScale = 0.92f, animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.95f, animationSpec = tween(280))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight / 3 },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 200)) +
                scaleOut(targetScale = 0.92f, animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
            }
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("gameType") ?: GameType.ZIP.name
            val gameType = try {
                GameType.valueOf(typeStr)
            } catch (e: Exception) {
                GameType.ZIP
            }

            GameHostScreen(
                viewModel = viewModel,
                gameType = gameType,
                levelNumber = activeLevelNumber,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.DAILY) {
            DailyChallengeScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPlayDailyGame = { type ->
                    val level = viewModel.activeLevelNumber.value
                    navController.navigate(Routes.gameRoute(type, level))
                }
            )
        }

        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToRewards = { navController.navigate(Routes.REWARDS) }
            )
        }

        composable(Routes.REWARDS) {
            RewardsShopScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING)
                }
            )
        }
    }
}
