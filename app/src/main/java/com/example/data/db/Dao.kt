package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameProgressDao {
    @Query("SELECT * FROM game_progress")
    fun getAllProgress(): Flow<List<GameProgressEntity>>

    @Query("SELECT * FROM game_progress WHERE gameKey = :key LIMIT 1")
    suspend fun getProgressByKey(key: String): GameProgressEntity?

    @Query("SELECT * FROM game_progress WHERE gameKey = :key LIMIT 1")
    fun observeProgressByKey(key: String): Flow<GameProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: GameProgressEntity)

    @Update
    suspend fun update(progress: GameProgressEntity)
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStatsEntity)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Update
    suspend fun update(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun getAchievement(id: String): AchievementEntity?
}
