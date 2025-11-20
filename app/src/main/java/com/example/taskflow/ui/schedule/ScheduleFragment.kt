package com.example.taskflow.ui.schedule

import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
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

class ScheduleFragment : Fragment() {
    private lateinit var viewModel: EventViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private var isMultiSelectMode = false
    private val selectedEvents = mutableSetOf<Long>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[EventViewModel::class.java]
        
        recyclerView = view.findViewById(R.id.events_recycler_view)
        adapter = EventAdapter(
            onItemClick = { event -> showEventDetails(event) },
            onItemDoubleClick = { event -> toggleEventCompletion(event) },
            onItemLongClick = { event -> enterMultiSelectMode(event) },
            onCheckboxClick = { event, isChecked ->
                if (isMultiSelectMode) {
                    if (isChecked) selectedEvents.add(event.id)
                    else selectedEvents.remove(event.id)
                    adapter.notifyDataSetChanged()
                } else {
                    toggleEventCompletion(event)
                }
            },
            onDeleteClick = { event -> deleteEvent(event) },
            isMultiSelectMode = { isMultiSelectMode },
            isSelected = { eventId -> selectedEvents.contains(eventId) }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        viewModel.allEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
        }
        
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_event)
        fab.setOnClickListener {
            EventDialogFragment.newInstance(null).show(
                parentFragmentManager,
                "EventDialog"
            )
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (isMultiSelectMode) {
            inflater.inflate(R.menu.menu_multi_select, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_selected -> {
                deleteSelectedEvents()
                true
            }
            R.id.action_cancel_selection -> {
                exitMultiSelectMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showEventDetails(event: Event) {
        if (!isMultiSelectMode) {
            EventDetailsDialogFragment.newInstance(event.id).show(
                parentFragmentManager,
                "EventDetailsDialog"
            )
        }
    }
    
    private fun toggleEventCompletion(event: Event) {
        val updatedEvent = event.copy(isCompleted = !event.isCompleted)
        viewModel.updateEvent(updatedEvent)
    }
    
    private fun enterMultiSelectMode(event: Event) {
        isMultiSelectMode = true
        selectedEvents.add(event.id)
        requireActivity().invalidateOptionsMenu()
        adapter.notifyDataSetChanged()
    }
    
    private fun exitMultiSelectMode() {
        isMultiSelectMode = false
        selectedEvents.clear()
        requireActivity().invalidateOptionsMenu()
        adapter.notifyDataSetChanged()
    }
    
    private fun deleteEvent(event: Event) {
        viewModel.deleteEvent(event)
        Toast.makeText(requireContext(), "Событие удалено", Toast.LENGTH_SHORT).show()
    }
    
    private fun deleteSelectedEvents() {
        if (selectedEvents.isNotEmpty()) {
            viewModel.deleteEventsByIds(selectedEvents.toList())
            Toast.makeText(
                requireContext(),
                "Удалено событий: ${selectedEvents.size}",
                Toast.LENGTH_SHORT
            ).show()
            exitMultiSelectMode()
        }
    }
    
    inner class EventAdapter(
        private val onItemClick: (Event) -> Unit,
        private val onItemDoubleClick: (Event) -> Unit,
        private val onItemLongClick: (Event) -> Unit,
        private val onCheckboxClick: (Event, Boolean) -> Unit,
        private val onDeleteClick: (Event) -> Unit,
        private val isMultiSelectMode: () -> Boolean,
        private val isSelected: (Long) -> Boolean
    ) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
        private var events: List<Event> = emptyList()
        private var lastClickTime = 0L
        private var lastClickedPosition = -1
        
        fun submitList(newEvents: List<Event>) {
            events = newEvents
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_event, parent, false)
            return EventViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            holder.bind(events[position])
        }
        
        override fun getItemCount() = events.size
        
        inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val checkbox: CheckBox = itemView.findViewById(R.id.event_checkbox)
            private val titleText: TextView = itemView.findViewById(R.id.event_title)
            private val deleteButton: View = itemView.findViewById(R.id.delete_button)
            
            fun bind(event: Event) {
                checkbox.isChecked = event.isCompleted || (isMultiSelectMode() && isSelected(event.id))
                titleText.text = event.title
                titleText.alpha = if (event.isCompleted) 0.5f else 1.0f
                
                checkbox.setOnCheckedChangeListener(null)
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    onCheckboxClick(event, isChecked)
                }
                
                deleteButton.setOnClickListener {
                    onDeleteClick(event)
                }
                
                itemView.setOnClickListener {
                    val currentTime = System.currentTimeMillis()
                    if (lastClickedPosition == adapterPosition && 
                        currentTime - lastClickTime < 500) {
                        // Double click
                        onItemDoubleClick(event)
                        lastClickTime = 0
                        lastClickedPosition = -1
                    } else {
                        // Single click
                        onItemClick(event)
                        lastClickTime = currentTime
                        lastClickedPosition = adapterPosition
                    }
                }
                
                itemView.setOnLongClickListener {
                    onItemLongClick(event)
                    true
                }
                
                if (isMultiSelectMode()) {
                    itemView.setBackgroundColor(
                        if (isSelected(event.id)) 
                            0x3300FF00.toInt() 
                        else 
                            0x00000000.toInt()
                    )
                } else {
                    itemView.setBackgroundColor(0x00000000.toInt())
                }
            }
        }
    }
}

