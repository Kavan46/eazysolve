package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.models.GameDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "eazy_solve_settings")

enum class ThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Geometric Light"),
    DARK("Dark Balance")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    val challengeTimerEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.8f,
    val musicEnabled: Boolean = true,
    val musicVolume: Float = 0.5f,
    val notificationsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val seenTutorials: Set<String> = emptySet(),
    val completedDailyDates: Set<String> = emptySet(),
    val adaptiveDifficultyEnabled: Boolean = true
)

class AppSettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DIFFICULTY = stringPreferencesKey("game_difficulty")
        val CHALLENGE_TIMER_ENABLED = booleanPreferencesKey("challenge_timer_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val SEEN_TUTORIALS = stringSetPreferencesKey("seen_game_tutorials")
        val COMPLETED_DAILY_DATES = stringSetPreferencesKey("completed_daily_dates")
        val ADAPTIVE_DIFFICULTY_ENABLED = booleanPreferencesKey("adaptive_difficulty_enabled")
    }

    val appSettingsFlow: Flow<AppSettings> = context.appSettingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeModeStr)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }

            val difficultyStr = preferences[PreferencesKeys.DIFFICULTY] ?: GameDifficulty.MEDIUM.name
            val difficulty = try {
                GameDifficulty.valueOf(difficultyStr)
            } catch (e: Exception) {
                GameDifficulty.MEDIUM
            }

            AppSettings(
                themeMode = themeMode,
                difficulty = difficulty,
                challengeTimerEnabled = preferences[PreferencesKeys.CHALLENGE_TIMER_ENABLED] ?: false,
                soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                soundVolume = preferences[PreferencesKeys.SOUND_VOLUME] ?: 0.8f,
                musicEnabled = preferences[PreferencesKeys.MUSIC_ENABLED] ?: true,
                musicVolume = preferences[PreferencesKeys.MUSIC_VOLUME] ?: 0.5f,
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true,
                hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false,
                seenTutorials = preferences[PreferencesKeys.SEEN_TUTORIALS] ?: emptySet(),
                completedDailyDates = preferences[PreferencesKeys.COMPLETED_DAILY_DATES] ?: emptySet(),
                adaptiveDifficultyEnabled = preferences[PreferencesKeys.ADAPTIVE_DIFFICULTY_ENABLED] ?: true
            )
        }

    suspend fun updateAdaptiveDifficultyEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.ADAPTIVE_DIFFICULTY_ENABLED] = enabled
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun updateDifficulty(difficulty: GameDifficulty) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.DIFFICULTY] = difficulty.name
        }
    }

    suspend fun updateChallengeTimerEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.CHALLENGE_TIMER_ENABLED] = enabled
        }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateSoundVolume(volume: Float) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_VOLUME] = volume.coerceIn(0.0f, 1.0f)
        }
    }

    suspend fun updateMusicEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.MUSIC_ENABLED] = enabled
        }
    }

    suspend fun updateMusicVolume(volume: Float) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.MUSIC_VOLUME] = volume.coerceIn(0.0f, 1.0f)
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateHapticsEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun updateHasCompletedOnboarding(completed: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun markGameTutorialSeen(gameKey: String) {
        context.appSettingsDataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.SEEN_TUTORIALS] ?: emptySet()
            preferences[PreferencesKeys.SEEN_TUTORIALS] = current + gameKey
        }
    }

    suspend fun markDailyChallengeCompleted(dateKey: String) {
        context.appSettingsDataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.COMPLETED_DAILY_DATES] ?: emptySet()
            preferences[PreferencesKeys.COMPLETED_DAILY_DATES] = current + dateKey
        }
    }

    suspend fun resetAllSettings() {
        context.appSettingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
