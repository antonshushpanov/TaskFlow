package com.example.taskflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.taskflow.data.Event
import com.example.taskflow.data.EventDatabase
import com.example.taskflow.repository.EventRepository
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository
    
    val allEvents: LiveData<List<Event>>
    
    init {
        val eventDao = EventDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        allEvents = repository.allEvents
    }
    
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): LiveData<List<Event>> {
        return repository.getEventsForDay(startOfDay, endOfDay)
    }
    
    suspend fun getEventById(id: Long): Event? {
        return repository.getEventById(id)
    }
    
    fun insertEvent(event: Event) {
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }
    
    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }
    
    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }
    
    fun deleteEventsByIds(eventIds: List<Long>) {
        viewModelScope.launch {
            repository.deleteEventsByIds(eventIds)
        }
    }
}

