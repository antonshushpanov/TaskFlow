package com.example.taskflow.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.Date

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY startTime ASC")
    fun getAllEvents(): LiveData<List<Event>>
    
    @Query("SELECT * FROM events WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime ASC")
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): LiveData<List<Event>>
    
    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): Event?
    
    @Insert
    suspend fun insertEvent(event: Event): Long
    
    @Update
    suspend fun updateEvent(event: Event)
    
    @Delete
    suspend fun deleteEvent(event: Event)
    
    @Query("DELETE FROM events WHERE id IN (:eventIds)")
    suspend fun deleteEventsByIds(eventIds: List<Long>)
}

