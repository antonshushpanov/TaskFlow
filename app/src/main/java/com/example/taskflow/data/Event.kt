package com.example.taskflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startTime: Long, // timestamp
    val endTime: Long, // timestamp
    val location: String? = null,
    val email: String? = null,
    val note: String? = null,
    val filePath: String? = null,
    val isCompleted: Boolean = false
)

