package com.theweek.plan.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.theweek.plan.R
import com.theweek.plan.databinding.CalendarDayBinding
import com.theweek.plan.databinding.CalendarMonthHeaderBinding
import com.theweek.plan.databinding.FragmentCalendarBinding
import com.theweek.plan.model.Task
import com.theweek.plan.ui.tasks.AddEditTaskFragment
import com.theweek.plan.ui.tasks.TaskAdapter
import com.theweek.plan.ui.tasks.TaskViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter
    
    private val selectedDate = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    
    private val today = LocalDate.now()
    private var selectedCalendarDay: CalendarDay? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup Calendar
        setupCalendar()
        
        // Display selected date tasks
        updateSelectedDateDisplay()
        loadTasksForSelectedDate()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onItemClick = { task -> navigateToEditTask(task) },
            onCheckboxClick = { task, isChecked -> updateTaskCompletionStatus(task, isChecked) },
            onEditClick = { task -> navigateToEditTask(task) },
            onDeleteClick = { task -> deleteTask(task) }
        )
        
        binding.recyclerDayTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(6)
        val lastMonth = currentMonth.plusMonths(6)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        
        binding.calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)
        
        // Setup day binder
        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view) { day ->
                selectDate(day)
            }
            
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.bind(day)
            }
        }
        
        // Setup month header binder
        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                container.bind(month)
            }
        }
    }

    private fun selectDate(day: CalendarDay) {
        if (day.owner == DayOwner.THIS_MONTH) {
            // Update selection
            selectedCalendarDay?.let {
                binding.calendarView.notifyDateChanged(it.date)
            }
            selectedCalendarDay = day
            binding.calendarView.notifyDateChanged(day.date)
            
            // Update selected date
            selectedDate.set(
                day.date.year,
                day.date.monthValue - 1,
                day.date.dayOfMonth
            )
            
            // Update UI
            updateSelectedDateDisplay()
            loadTasksForSelectedDate()
        }
    }

    private fun updateSelectedDateDisplay() {
        binding.textSelectedDate.text = dateFormatter.format(selectedDate.time)
    }

    private fun loadTasksForSelectedDate() {
        // Create start and end of day for the selected date
        val startCalendar = Calendar.getInstance().apply {
            time = selectedDate.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val endCalendar = Calendar.getInstance().apply {
            time = selectedDate.time
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        
        taskViewModel.getTasksByDateRange(startCalendar.time, endCalendar.time)
            .observe(viewLifecycleOwner) { tasks ->
                taskAdapter.submitList(tasks)
                updateEmptyView(tasks)
            }
    }

    private fun updateEmptyView(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            binding.textNoTasks.visibility = View.VISIBLE
            binding.recyclerDayTasks.visibility = View.GONE
        } else {
            binding.textNoTasks.visibility = View.GONE
            binding.recyclerDayTasks.visibility = View.VISIBLE
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

    // Calendar day view container
    inner class DayViewContainer(view: View, private val onDateClick: (CalendarDay) -> Unit) : ViewContainer(view) {
        private val binding = CalendarDayBinding.bind(view)
        private lateinit var day: CalendarDay
        
        init {
            view.setOnClickListener {
                onDateClick(day)
            }
        }
        
        fun bind(day: CalendarDay) {
            this.day = day
            binding.textDay.text = day.date.dayOfMonth.toString()
            
            if (day.owner == DayOwner.THIS_MONTH) {
                // Normal day
                binding.textDay.alpha = 1f
                
                // Highlight today
                if (day.date == today) {
                    binding.textDay.setBackgroundResource(com.google.android.material.R.drawable.abc_btn_colored_material)
                    binding.textDay.setTextColor(android.graphics.Color.WHITE)
                } else {
                    binding.textDay.background = null
                    binding.textDay.setTextColor(android.graphics.Color.BLACK)
                }
                
                // Highlight selected day
                if (selectedCalendarDay?.date == day.date) {
                    binding.textDay.setBackgroundResource(com.google.android.material.R.drawable.abc_btn_borderless_material)
                    binding.textDay.setTextColor(android.graphics.Color.BLUE)
                }
                
                // TODO: Show indicator for days with tasks
                
            } else {
                // Day from another month
                binding.textDay.alpha = 0.3f
                binding.textDay.background = null
                binding.textDay.setTextColor(android.graphics.Color.GRAY)
            }
        }
    }

    // Calendar month header view container
    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        private val binding = CalendarMonthHeaderBinding.bind(view)
        
        fun bind(month: CalendarMonth) {
            binding.textMonthYear.text = monthFormatter.format(month.yearMonth)
        }
    }
}
