package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.adaptive.AdaptiveDifficultyEngine
import com.example.data.adaptive.AdaptiveEngineState
import com.example.data.adaptive.GameResultRecord
import com.example.data.auth.AuthRepository
import com.example.data.auth.CloudSyncData
import com.example.data.auth.UserProfile
import com.example.data.datastore.AppSettings
import com.example.data.datastore.AppSettingsDataStore
import com.example.data.datastore.ThemeMode
import com.example.data.db.AchievementEntity
import com.example.data.db.AppDatabase
import com.example.data.db.GameProgressEntity
import com.example.data.db.UserStatsEntity
import com.example.data.models.DailyChallengeGenerator
import com.example.data.models.DailyChallengeInfo
import com.example.data.models.GameCategory
import com.example.data.models.GameDifficulty
import com.example.data.models.GameType
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repository = GameRepository(db)
    private val appSettingsDataStore = AppSettingsDataStore(application)
    val authRepository = AuthRepository(application)

    val currentUserProfile: StateFlow<UserProfile> = authRepository.currentUserState
    val cloudSyncStatus: StateFlow<String?> = authRepository.syncStatus

    val appSettings: StateFlow<AppSettings> = appSettingsDataStore.appSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val gameProgressList: StateFlow<List<GameProgressEntity>> = repository.allGameProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStatsEntity?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unclaimedAchievementsCount: StateFlow<Int> = achievements
        .map { list -> list.count { it.isUnlocked && !it.isRewardClaimed } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Adaptive Difficulty Engine State
    private val _adaptiveState = MutableStateFlow(AdaptiveEngineState())
    val adaptiveState: StateFlow<AdaptiveEngineState> = _adaptiveState.asStateFlow()

    // Daily Challenge System
    private val _todayDailyChallenge = MutableStateFlow(DailyChallengeGenerator.getChallengeForDate())
    val todayDailyChallenge: StateFlow<DailyChallengeInfo> = _todayDailyChallenge.asStateFlow()

    val isTodayDailyCompleted: StateFlow<Boolean> = appSettings
        .map { settings ->
            val todayKey = _todayDailyChallenge.value.dateKey
            settings.completedDailyDates.contains(todayKey)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // UI Filter & Search
    private val _selectedCategory = MutableStateFlow(GameCategory.ALL)
    val selectedCategory: StateFlow<GameCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Active Game State
    private val _activeGameType = MutableStateFlow(GameType.ZIP)
    val activeGameType: StateFlow<GameType> = _activeGameType.asStateFlow()

    private val _activeLevelNumber = MutableStateFlow(1)
    val activeLevelNumber: StateFlow<Int> = _activeLevelNumber.asStateFlow()

    private val _isDailyMode = MutableStateFlow(false)
    val isDailyMode: StateFlow<Boolean> = _isDailyMode.asStateFlow()

    init {
        HapticManager.init(application)
        viewModelScope.launch {
            repository.initializeDefaults()
        }
        viewModelScope.launch {
            appSettings.collect { settings ->
                SoundManager.updateSettings(
                    soundEnabled = settings.soundEnabled,
                    soundVolume = settings.soundVolume,
                    musicEnabled = settings.musicEnabled,
                    musicVolume = settings.musicVolume
                )
                HapticManager.hapticsEnabled = settings.hapticsEnabled
                _adaptiveState.value = _adaptiveState.value.copy(isEnabled = settings.adaptiveDifficultyEnabled)
            }
        }
    }

    fun recordGamePerformance(
        gameType: GameType,
        level: Int,
        isWin: Boolean,
        stars: Int,
        timeSec: Int,
        parTimeSec: Int = 90,
        hintsUsed: Int = 0,
        mistakes: Int = 0
    ) {
        val record = GameResultRecord(
            gameType = gameType,
            levelNumber = level,
            isWin = isWin,
            stars = stars,
            timeSeconds = timeSec,
            parTimeSeconds = parTimeSec,
            hintsUsed = hintsUsed,
            mistakes = mistakes
        )
        _adaptiveState.value = AdaptiveDifficultyEngine.calculateNewState(_adaptiveState.value, record)
    }

    fun recordGameRestart(gameType: GameType, level: Int) {
        recordGamePerformance(
            gameType = gameType,
            level = level,
            isWin = false,
            stars = 0,
            timeSec = 0,
            hintsUsed = 0,
            mistakes = 1
        )
    }

    fun toggleAdaptiveDifficulty(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsDataStore.updateAdaptiveDifficultyEnabled(enabled)
            _adaptiveState.value = _adaptiveState.value.copy(isEnabled = enabled)
        }
    }

    fun refreshDailyChallenge() {
        _todayDailyChallenge.value = DailyChallengeGenerator.getChallengeForDate()
    }

    fun signInWithGoogle(activityContext: android.content.Context? = null, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activityContext)
            if (result.isSuccess) {
                // Fetch cloud data and merge
                val cloudData = authRepository.fetchCloudUserData()
                if (cloudData != null) {
                    if (cloudData.totalCoins > 0) {
                        repository.addCoins(cloudData.totalCoins)
                    }
                    cloudData.completedDailyDates.forEach { dateKey ->
                        appSettingsDataStore.markDailyChallengeCompleted(dateKey)
                    }
                } else {
                    syncToCloud()
                }
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun syncToCloud() {
        viewModelScope.launch {
            val stats = userStats.value
            val progress = gameProgressList.value
            val settings = appSettings.value
            val totalLevels = progress.sumOf { (it.highestLevel - 1).coerceAtLeast(0) }
            val totalStars = progress.sumOf { it.totalStars }

            val syncData = CloudSyncData(
                email = currentUserProfile.value.email,
                displayName = currentUserProfile.value.displayName,
                totalCoins = stats?.coins ?: 100,
                totalXp = stats?.xp ?: 50,
                currentStreak = stats?.currentStreak ?: 1,
                longestStreak = stats?.longestStreak ?: 1,
                completedDailyDates = settings.completedDailyDates.toList(),
                totalLevelsCleared = totalLevels,
                totalStars = totalStars
            )
            authRepository.syncUserDataToCloud(syncData)
        }
    }

    fun setCategory(category: GameCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun launchGame(type: GameType, level: Int = 1, isDaily: Boolean = false) {
        _activeGameType.value = type
        _activeLevelNumber.value = level
        _isDailyMode.value = isDaily
    }

    fun toggleFavorite(type: GameType) {
        viewModelScope.launch {
            repository.toggleFavorite(type)
        }
    }

    fun onGameWon(gameType: GameType, level: Int, score: Int, timeSec: Int, stars: Int, hintsUsed: Int = 0, mistakes: Int = 0) {
        viewModelScope.launch {
            recordGamePerformance(
                gameType = gameType,
                level = level,
                isWin = true,
                stars = stars,
                timeSec = timeSec,
                hintsUsed = hintsUsed,
                mistakes = mistakes
            )
            repository.recordLevelCompleted(gameType, level, score, timeSec, stars)
            if (_isDailyMode.value) {
                completeDailyChallenge()
            }
            syncToCloud()
        }
    }

    fun nextLevel() {
        _activeLevelNumber.value += 1
    }

    fun claimAchievement(id: String) {
        viewModelScope.launch {
            repository.claimAchievementReward(id)
            SoundManager.playCoinReward()
            syncToCloud()
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            appSettingsDataStore.updateThemeMode(themeMode)
        }
    }

    fun setDifficulty(difficulty: GameDifficulty) {
        viewModelScope.launch {
            appSettingsDataStore.updateDifficulty(difficulty)
        }
    }

    fun toggleChallengeTimer(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsDataStore.updateChallengeTimerEnabled(enabled)
        }
    }

    fun toggleSound(enabled: Boolean) {
        SoundManager.soundEnabled = enabled
        viewModelScope.launch {
            appSettingsDataStore.updateSoundEnabled(enabled)
            repository.toggleSound(enabled)
        }
    }

    fun setSoundVolume(volume: Float) {
        SoundManager.soundVolume = volume
        viewModelScope.launch {
            appSettingsDataStore.updateSoundVolume(volume)
        }
    }

    fun toggleMusic(enabled: Boolean) {
        SoundManager.musicEnabled = enabled
        viewModelScope.launch {
            appSettingsDataStore.updateMusicEnabled(enabled)
            repository.toggleMusic(enabled)
        }
    }

    fun setMusicVolume(volume: Float) {
        SoundManager.musicVolume = volume
        viewModelScope.launch {
            appSettingsDataStore.updateMusicVolume(volume)
        }
    }

    fun playSoundPreview() {
        SoundManager.playMatchSuccess()
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsDataStore.updateNotificationsEnabled(enabled)
            repository.toggleNotifications(enabled)
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsDataStore.updateHapticsEnabled(enabled)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            appSettingsDataStore.updateHasCompletedOnboarding(true)
        }
    }

    fun markGameTutorialSeen(gameType: GameType) {
        viewModelScope.launch {
            appSettingsDataStore.markGameTutorialSeen(gameType.key)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            appSettingsDataStore.updateHasCompletedOnboarding(false)
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            appSettingsDataStore.resetAllSettings()
        }
    }

    fun claimDailyBonus() {
        viewModelScope.launch {
            repository.addCoins(50)
            SoundManager.playCoinReward()
            syncToCloud()
        }
    }

    fun completeDailyChallenge() {
        viewModelScope.launch {
            val dateKey = _todayDailyChallenge.value.dateKey
            appSettingsDataStore.markDailyChallengeCompleted(dateKey)
            repository.markDailyChallengeCompleted()
            SoundManager.playCoinReward()
            syncToCloud()
        }
    }

    fun addCoins(amount: Int) {
        viewModelScope.launch {
            repository.addCoins(amount)
            SoundManager.playCoinReward()
            syncToCloud()
        }
    }

    fun spendCoins(amount: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.spendCoins(amount)
            if (success) {
                syncToCloud()
            }
            onComplete(success)
        }
    }
}
