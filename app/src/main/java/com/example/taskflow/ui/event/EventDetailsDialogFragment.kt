package com.example.taskflow.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.taskflow.R
import com.example.taskflow.viewmodel.EventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EventDetailsDialogFragment : DialogFragment() {
    private lateinit var viewModel: EventViewModel
    private var eventId: Long = 0
    
    companion object {
        fun newInstance(eventId: Long): EventDetailsDialogFragment {
            val fragment = EventDetailsDialogFragment()
            fragment.arguments = Bundle().apply {
                putLong("eventId", eventId)
            }
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        
        arguments?.let {
            eventId = it.getLong("eventId")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_event_details, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[EventViewModel::class.java]
        
        val titleText: TextView = view.findViewById(R.id.text_title)
        val timeText: TextView = view.findViewById(R.id.text_time)
        val locationLabel: TextView = view.findViewById(R.id.label_location)
        val locationText: TextView = view.findViewById(R.id.text_location)
        val emailLabel: TextView = view.findViewById(R.id.label_email)
        val emailText: TextView = view.findViewById(R.id.text_email)
        val noteLabel: TextView = view.findViewById(R.id.label_note)
        val noteText: TextView = view.findViewById(R.id.text_note)
        val editButton: Button = view.findViewById(R.id.button_edit)
        val deleteButton: Button = view.findViewById(R.id.button_delete)
        
        lifecycleScope.launch {
            val event = withContext(Dispatchers.IO) {
                viewModel.getEventById(eventId)
            }
            
            event?.let {
                titleText.text = it.title
                
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                timeText.text = "${dateFormat.format(Date(it.startTime))} - ${dateFormat.format(Date(it.endTime))}"
                
                if (!it.location.isNullOrEmpty()) {
                    locationText.text = it.location
                    locationLabel.visibility = View.VISIBLE
                    locationText.visibility = View.VISIBLE
                } else {
                    locationLabel.visibility = View.GONE
                    locationText.visibility = View.GONE
                }
                
                if (!it.email.isNullOrEmpty()) {
                    emailText.text = it.email
                    emailLabel.visibility = View.VISIBLE
                    emailText.visibility = View.VISIBLE
                } else {
                    emailLabel.visibility = View.GONE
                    emailText.visibility = View.GONE
                }
                
                if (!it.note.isNullOrEmpty()) {
                    noteText.text = it.note
                    noteLabel.visibility = View.VISIBLE
                    noteText.visibility = View.VISIBLE
                } else {
                    noteLabel.visibility = View.GONE
                    noteText.visibility = View.GONE
                }
                
                editButton.setOnClickListener {
                    dismiss()
                    EventDialogFragment.newInstance(eventId).show(
                        parentFragmentManager,
                        "EventDialog"
                    )
                }
                
                deleteButton.setOnClickListener {
                    viewModel.deleteEvent(event)
                    dismiss()
                }
            }
        }
    }
}

