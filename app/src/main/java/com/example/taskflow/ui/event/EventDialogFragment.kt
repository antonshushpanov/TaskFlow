package com.example.taskflow.ui.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.taskflow.R
import com.example.taskflow.data.Event
import com.example.taskflow.viewmodel.EventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class EventDialogFragment : DialogFragment() {
    private lateinit var viewModel: EventViewModel
    private var eventId: Long? = null
    private var initialDate: Long? = null
    
    private lateinit var titleEdit: EditText
    private lateinit var startTimeButton: Button
    private lateinit var endTimeButton: Button
    private lateinit var locationEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var noteEdit: EditText
    private lateinit var fileButton: Button
    
    private var startTime: Long = System.currentTimeMillis()
    private var endTime: Long = System.currentTimeMillis() + 3600000 // +1 hour
    
    companion object {
        fun newInstance(eventId: Long? = null, initialDate: Long? = null): EventDialogFragment {
            val fragment = EventDialogFragment()
            fragment.arguments = Bundle().apply {
                eventId?.let { putLong("eventId", it) }
                initialDate?.let { putLong("initialDate", it) }
            }
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        
        arguments?.let {
            if (it.containsKey("eventId")) {
                eventId = it.getLong("eventId")
            }
            if (it.containsKey("initialDate")) {
                initialDate = it.getLong("initialDate")
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_event, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[EventViewModel::class.java]
        
        titleEdit = view.findViewById(R.id.edit_title)
        startTimeButton = view.findViewById(R.id.button_start_time)
        endTimeButton = view.findViewById(R.id.button_end_time)
        locationEdit = view.findViewById(R.id.edit_location)
        emailEdit = view.findViewById(R.id.edit_email)
        noteEdit = view.findViewById(R.id.edit_note)
        fileButton = view.findViewById(R.id.button_attach_file)
        
        val cancelButton: Button = view.findViewById(R.id.button_cancel)
        val saveButton: Button = view.findViewById(R.id.button_save)
        val dialogTitle: TextView = view.findViewById(R.id.dialog_title)
        
        dialogTitle.text = if (eventId != null) "Редактировать событие" else "Создать событие"
        
        if (initialDate != null) {
            startTime = initialDate!!
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = startTime
            calendar.add(Calendar.HOUR, 1)
            endTime = calendar.timeInMillis
        }
        
        updateTimeButtons()
        
        if (eventId != null) {
            loadEvent(eventId!!)
        }
        
        startTimeButton.setOnClickListener {
            showDateTimePicker(true)
        }
        
        endTimeButton.setOnClickListener {
            showDateTimePicker(false)
        }
        
        fileButton.setOnClickListener {
            // TODO: Implement file attachment
            Toast.makeText(requireContext(), "Функция прикрепления файлов в разработке", Toast.LENGTH_SHORT).show()
        }
        
        cancelButton.setOnClickListener {
            dismiss()
        }
        
        saveButton.setOnClickListener {
            saveEvent()
        }
    }
    
    private fun loadEvent(id: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val event = withContext(Dispatchers.IO) {
                viewModel.getEventById(id)
            }
            event?.let {
                titleEdit.setText(it.title)
                startTime = it.startTime
                endTime = it.endTime
                locationEdit.setText(it.location ?: "")
                emailEdit.setText(it.email ?: "")
                noteEdit.setText(it.note ?: "")
                updateTimeButtons()
            }
        }
    }
    
    private fun showDateTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = if (isStartTime) startTime else endTime
        
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        
                        if (isStartTime) {
                            startTime = calendar.timeInMillis
                            if (endTime <= startTime) {
                                endTime = startTime + 3600000
                            }
                        } else {
                            if (calendar.timeInMillis > startTime) {
                                endTime = calendar.timeInMillis
                            } else {
                                Toast.makeText(requireContext(), "Время окончания должно быть позже времени начала", Toast.LENGTH_SHORT).show()
                                return@TimePickerDialog
                            }
                        }
                        updateTimeButtons()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }
    
    private fun updateTimeButtons() {
        val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        startTimeButton.text = "Начало: ${dateFormat.format(Date(startTime))}"
        endTimeButton.text = "Окончание: ${dateFormat.format(Date(endTime))}"
    }
    
    private fun saveEvent() {
        val title = titleEdit.text.toString().trim()
        
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название события", Toast.LENGTH_SHORT).show()
            return
        }
        
        val event = Event(
            id = eventId ?: 0,
            title = title,
            startTime = startTime,
            endTime = endTime,
            location = locationEdit.text.toString().takeIf { it.isNotEmpty() },
            email = emailEdit.text.toString().takeIf { it.isNotEmpty() },
            note = noteEdit.text.toString().takeIf { it.isNotEmpty() },
            filePath = null
        )
        
        if (eventId != null) {
            viewModel.updateEvent(event)
        } else {
            viewModel.insertEvent(event)
        }
        
        dismiss()
    }
}

