package com.example.mobile_handson_29

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val activityName: String,
    val duration: Int,
    val date: String
)