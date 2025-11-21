package com.example.mobile_handson_29

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActivityDao {
    @Insert
    suspend fun insert(activity: ActivityEntity)

    @Query("SELECT * FROM fitness_activities ORDER BY date DESC")
    suspend fun getAll(): List<ActivityEntity>

    @Query("SELECT * FROM fitness_activities WHERE date = :filterDate")
    suspend fun getByDate(filterDate: String): List<ActivityEntity>

    @Query("SELECT SUM(duration) FROM fitness_activities WHERE date = :filterDate")
    suspend fun getTotalDuration(filterDate: String): Int?
}