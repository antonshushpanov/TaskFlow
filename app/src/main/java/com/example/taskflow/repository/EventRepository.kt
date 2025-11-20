package com.example.taskflow.repository

import androidx.lifecycle.LiveData
import com.example.taskflow.data.Event
import com.example.taskflow.data.EventDao

class EventRepository(private val eventDao: EventDao) {
    val allEvents: LiveData<List<Event>> = eventDao.getAllEvents()
    
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): LiveData<List<Event>> {
        return eventDao.getEventsForDay(startOfDay, endOfDay)
    }
    
    suspend fun getEventById(id: Long): Event? {
        return eventDao.getEventById(id)
    }
    
    suspend fun insertEvent(event: Event): Long {
        return eventDao.insertEvent(event)
    }
    
    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }
    
    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }
    
    suspend fun deleteEventsByIds(eventIds: List<Long>) {
        eventDao.deleteEventsByIds(eventIds)
    }
}

