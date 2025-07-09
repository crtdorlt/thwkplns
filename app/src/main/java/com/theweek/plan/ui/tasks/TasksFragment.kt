package com.theweek.plan.ui.tasks

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.theweek.plan.R
import com.theweek.plan.databinding.FragmentTasksBinding
import com.theweek.plan.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter
    
    private val calendar = Calendar.getInstance()
    private var selectedDate = Date() // Default to today
    private var selectedDayButton: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        
        // Setup week number and navigation
        setupWeekDisplay()
        
        // Setup day navigation buttons
        setupDayButtons()
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Observe tasks
        observeTasks()
    }
    
    private fun setupWeekDisplay() {
        // Set the current week number and year
        updateWeekNumber()
        
        // Setup week navigation buttons
        binding.btnPreviousWeek.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeekNumber()
            updateDayButtons()
            updateTasksForSelectedDate()
        }
        
        binding.btnNextWeek.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeekNumber()
            updateDayButtons()
            updateTasksForSelectedDate()
        }
    }
    
    private fun updateWeekNumber() {
        val weekFormat = SimpleDateFormat("w", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val weekNumber = weekFormat.format(calendar.time)
        val year = yearFormat.format(calendar.time)
        
        binding.textWeekNumber.text = "Week $weekNumber, $year"
    }
    
    private fun setupDayButtons() {
        // Get all day buttons
        val dayButtons = listOf(
            binding.btnDayMon,
            binding.btnDayTue,
            binding.btnDayWed,
            binding.btnDayThu,
            binding.btnDayFri,
            binding.btnDaySat,
            binding.btnDaySun
        )
        
        // Set click listeners for each day button
        dayButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectDayButton(button)
                
                // Calculate the date for this day button
                val tempCalendar = Calendar.getInstance()
                tempCalendar.time = calendar.time
                tempCalendar.set(Calendar.DAY_OF_WEEK, index + Calendar.MONDAY)
                selectedDate = tempCalendar.time
                
                // Update tasks for the selected date
                updateTasksForSelectedDate()
            }
        }
        
        // Initialize with today or the closest day in the current week
        updateDayButtons()
    }
    
    private fun updateDayButtons() {
        // Get all day buttons
        val dayButtons = listOf(
            binding.btnDayMon,
            binding.btnDayTue,
            binding.btnDayWed,
            binding.btnDayThu,
            binding.btnDayFri,
            binding.btnDaySat,
            binding.btnDaySun
        )
        
        // Reset all buttons to default style first
        dayButtons.forEach { button ->
            button.apply {
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                backgroundTintList = null
                setBackgroundResource(R.drawable.btn_day_background)
            }
        }
        
        // Get current day of week (1 = Sunday, 2 = Monday, etc.)
        val today = Calendar.getInstance()
        val currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
        
        // Convert to our index (0 = Monday, 1 = Tuesday, etc.)
        val todayIndex = (currentDayOfWeek + 5) % 7
        
        // Check if today is in the currently displayed week
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val todayWeek = today.get(Calendar.WEEK_OF_YEAR)
        
        if (currentWeek == todayWeek && calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            // If we're viewing the current week, select today
            selectDayButton(dayButtons[todayIndex])
            
            // Highlight today with special background
            dayButtons[todayIndex].apply {
                setBackgroundResource(R.drawable.current_day_background)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            
            // Set selected date to today
            selectedDate = Date()
        } else {
            // Otherwise select Monday of the displayed week
            selectDayButton(dayButtons[0])
            
            // Set selected date to Monday of the displayed week
            val tempCalendar = Calendar.getInstance()
            tempCalendar.time = calendar.time
            tempCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            selectedDate = tempCalendar.time
        }
        
        // Update tasks for the selected date
        updateTasksForSelectedDate()
    }
    
    private fun selectDayButton(button: MaterialButton) {
        // Get today's index
        val today = Calendar.getInstance()
        val currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
        val todayIndex = (currentDayOfWeek + 5) % 7
        
        // Get all day buttons
        val dayButtons = listOf(
            binding.btnDayMon,
            binding.btnDayTue,
            binding.btnDayWed,
            binding.btnDayThu,
            binding.btnDayFri,
            binding.btnDaySat,
            binding.btnDaySun
        )
        
        // Check if we're in the current week
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val todayWeek = today.get(Calendar.WEEK_OF_YEAR)
        val isCurrentWeek = currentWeek == todayWeek && calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        
        // Reset previous selection
        selectedDayButton?.apply {
            // Don't reset today's button if it's in the current week
            if (!(isCurrentWeek && this == dayButtons[todayIndex])) {
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setBackgroundResource(R.drawable.btn_day_background)
            }
        }
        
        // Apply selection to the new button
        button.apply {
            // If this is today's button in the current week, keep the special background
            if (isCurrentWeek && this == dayButtons[todayIndex]) {
                setBackgroundResource(R.drawable.current_day_background)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            }
        }
        
        // Update selected button reference
        selectedDayButton = button
    }
    
    private fun updateTasksForSelectedDate() {
        // Format the date for display
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(selectedDate)
        
        // Filter tasks for the selected date
        taskViewModel.getTasksByDate(selectedDate).observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
            
            // Show empty view if no tasks
            binding.textEmpty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onItemClick = { task -> navigateToEditTask(task) },
            onCheckboxClick = { task, isChecked -> updateTaskCompletionStatus(task, isChecked) },
            onEditClick = { task -> navigateToEditTask(task) },
            onDeleteClick = { task -> deleteTask(task) }
        )
        
        binding.recyclerTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun observeTasks() {
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
            updateEmptyView(tasks)
        }
    }

    private fun updateEmptyView(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            binding.textEmpty.visibility = View.VISIBLE
            binding.recyclerTasks.visibility = View.GONE
        } else {
            binding.textEmpty.visibility = View.GONE
            binding.recyclerTasks.visibility = View.VISIBLE
        }
    }

    private fun navigateToEditTask(task: Task) {
        val fragment = AddEditTaskFragment().apply {
            arguments = Bundle().apply {
                putString("task_id", task.id)
            }
        }
        fragment.show(parentFragmentManager, "edit_task")
    }

    private fun updateTaskCompletionStatus(task: Task, isCompleted: Boolean) {
        taskViewModel.updateTaskCompletionStatus(task.id, isCompleted)
        
        val message = if (isCompleted) {
            getString(R.string.task_completed)
        } else {
            getString(R.string.task_pending)
        }
        
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun deleteTask(task: Task) {
        // Show confirmation dialog before deleting
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(getString(R.string.confirm_delete_message, task.title))
            .setPositiveButton(R.string.delete) { _, _ ->
                // Delete the task if confirmed
                taskViewModel.deleteTask(task)
                Snackbar.make(binding.root, getString(R.string.task_deleted), Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
