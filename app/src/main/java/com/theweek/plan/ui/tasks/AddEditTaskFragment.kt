package com.theweek.plan.ui.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.theweek.plan.R
import com.theweek.plan.databinding.FragmentAddEditTaskBinding
import com.theweek.plan.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddEditTaskFragment : DialogFragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel
    private var taskId: String? = null
    private var task: Task? = null
    
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_TheWeekPlan)
        
        // Get task ID from arguments if editing
        arguments?.let {
            taskId = it.getString("task_id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        
        // Setup category spinner
        setupCategorySpinner()
        
        // Setup date and time pickers
        setupDateTimePickers()
        
        // Load task data if editing
        loadTaskData()
        
        // Setup save button
        binding.buttonSave.setOnClickListener {
            saveTask()
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            getString(R.string.work),
            getString(R.string.personal),
            getString(R.string.health),
            getString(R.string.education),
            getString(R.string.other)
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDateTimePickers() {
        // Set default date and time to current time
        updateDateDisplay()
        updateTimeDisplay()
        
        // Setup date picker
        binding.textSelectedDate.setOnClickListener {
            showDatePicker()
        }
        
        // Setup time picker
        binding.textSelectedTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateTimeDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    private fun updateDateDisplay() {
        binding.textSelectedDate.text = dateFormatter.format(calendar.time)
    }

    private fun updateTimeDisplay() {
        binding.textSelectedTime.text = timeFormatter.format(calendar.time)
    }

    private fun loadTaskData() {
        taskId?.let { id ->
            taskViewModel.getTaskById(id).observe(viewLifecycleOwner) { existingTask ->
                existingTask?.let {
                    task = it
                    populateUI(it)
                }
            }
        }
    }

    private fun populateUI(task: Task) {
        binding.apply {
            editTitle.setText(task.title)
            editDescription.setText(task.description)
            
            // Set category
            val categoryPosition = when (task.category) {
                getString(R.string.work) -> 0
                getString(R.string.personal) -> 1
                getString(R.string.health) -> 2
                getString(R.string.education) -> 3
                else -> 4
            }
            spinnerCategory.setSelection(categoryPosition)
            
            // Set priority
            when (task.priority) {
                3 -> radioHigh.isChecked = true
                2 -> radioMedium.isChecked = true
                else -> radioLow.isChecked = true
            }
            
            // Set date and time
            calendar.time = task.dueDate
            updateDateDisplay()
            
            task.dueTime?.let {
                calendar.time = it
                updateTimeDisplay()
            }
        }
    }

    private fun saveTask() {
        val title = binding.editTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.inputLayoutTitle.error = "Title is required"
            return
        }
        
        val description = binding.editDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        
        val priority = when {
            binding.radioHigh.isChecked -> 3
            binding.radioMedium.isChecked -> 2
            else -> 1
        }
        
        val dueDate = calendar.time
        val dueTime = calendar.time
        
        val newTask = if (task != null) {
            task!!.copy(
                title = title,
                description = description,
                category = category,
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime,
                updatedAt = Date()
            )
        } else {
            Task(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                category = category,
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime
            )
        }
        
        if (task != null) {
            taskViewModel.updateTask(newTask)
            Snackbar.make(binding.root, getString(R.string.task_updated), Snackbar.LENGTH_SHORT).show()
        } else {
            taskViewModel.insertTask(newTask)
            Snackbar.make(binding.root, getString(R.string.task_added), Snackbar.LENGTH_SHORT).show()
        }
        
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
