package com.example.taskflow.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskflow.R
import com.example.taskflow.data.Event
import com.example.taskflow.ui.event.EventDetailsDialogFragment
import com.example.taskflow.ui.event.EventDialogFragment
import com.example.taskflow.viewmodel.EventViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class CalendarFragment : Fragment() {
    private lateinit var viewModel: EventViewModel
    private lateinit var calendarView: CalendarView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventsAdapter: EventsAdapter
    private var selectedDate: Long = System.currentTimeMillis()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[EventViewModel::class.java]
        
        calendarView = view.findViewById(R.id.calendar_view)
        eventsRecyclerView = view.findViewById(R.id.events_list)
        
        eventsAdapter = EventsAdapter { event ->
            EventDetailsDialogFragment.newInstance(event.id).show(
                parentFragmentManager,
                "EventDetailsDialog"
            )
        }
        
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        eventsRecyclerView.adapter = eventsAdapter
        
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            selectedDate = calendar.timeInMillis
            
            loadEventsForDate(selectedDate)
        }
        
        loadEventsForDate(selectedDate)
        
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_event)
        fab.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            EventDialogFragment.newInstance(null, selectedDate).show(
                parentFragmentManager,
                "EventDialog"
            )
        }
    }
    
    private fun loadEventsForDate(date: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis
        
        viewModel.getEventsForDay(startOfDay, endOfDay).observe(viewLifecycleOwner) { events ->
            eventsAdapter.submitList(events)
        }
    }
    
    inner class EventsAdapter(
        private val onEventClick: (Event) -> Unit
    ) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {
        private var events: List<Event> = emptyList()
        
        fun submitList(newEvents: List<Event>) {
            events = newEvents
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_event_simple, parent, false)
            return EventViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            holder.bind(events[position])
        }
        
        override fun getItemCount() = events.size
        
        inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleText: TextView = itemView.findViewById(R.id.event_title)
            private val timeText: TextView = itemView.findViewById(R.id.event_time)
            
            fun bind(event: Event) {
                titleText.text = event.title
                
                val startTime = Date(event.startTime)
                val endTime = Date(event.endTime)
                val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
                timeText.text = "${timeFormat.format(startTime)} - ${timeFormat.format(endTime)}"
                
                itemView.setOnClickListener {
                    onEventClick(event)
                }
            }
        }
    }
}

