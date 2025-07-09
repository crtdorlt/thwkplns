package com.theweek.plan.ui.statistics

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.theweek.plan.R
import com.theweek.plan.databinding.FragmentPerformanceBinding
import com.theweek.plan.model.Task
import com.theweek.plan.ui.tasks.TaskViewModel
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data class representing a streak level with all its properties
 */
data class StreakLevel(
    val level: Int,
    val phase: String,
    val name: String,
    val color: String,
    val icon: String,
    val description: String,
    val minDays: Int,
    val maxDays: Int
)

class StatisticsFragment : Fragment() {

    private var _binding: FragmentPerformanceBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var taskViewModel: TaskViewModel
    
    private val dayLabels = arrayOf("M", "T", "W", "T", "F", "S", "S")
    private val categoryColors = intArrayOf(
        Color.rgb(64, 89, 128),
        Color.rgb(149, 165, 124),
        Color.rgb(217, 184, 162),
        Color.rgb(191, 134, 134),
        Color.rgb(179, 48, 80)
    )
    
    private var currentStreak = 0
    private var bestStreak = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        
        // Setup task streak
        setupTaskStreak()
        
        // Setup task analytics
        setupTaskAnalytics()
        
        // Productivity score removed as requested
        
        // Setup weekly completion rate chart
        setupWeeklyCompletionChart()
        
        // Setup category distribution chart
        setupCategoryChart()
    }
    
    private fun setupTaskStreak() {
        // Observe tasks to calculate streak data
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            // Calculate current and best streak based on completed tasks
            val streakData = calculateStreaks(tasks)
            currentStreak = streakData.first
            bestStreak = streakData.second
            
            // Get streak level information
            val streakLevel = getStreakLevel(currentStreak)
            
            // Update UI with streak information
            binding.textCurrentStreak.text = "$currentStreak days"
            binding.textBestStreak.text = "$bestStreak days"
            
            // Update level and phase information
            binding.textLevel.text = streakLevel.name
            binding.textPhase.text = "Phase: ${streakLevel.phase.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
            
            // Set streak emoji
            binding.textStreakEmoji.text = streakLevel.icon
            
            // Set streak message
            binding.textStreakSubtitle.text = streakLevel.description
            
            // Set gradient background based on streak level color
            val streakBackground = binding.streakBackground
            val gradientColors = getGradientColors(streakLevel.color)
            
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                gradientColors
            )
            gradient.cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
            streakBackground.background = gradient
        }
    }
    

    
    /**
     * Calculate current and best streaks based on completed tasks
     * @return Pair of (currentStreak, bestStreak)
     */
    private fun calculateStreaks(tasks: List<Task>): Pair<Int, Int> {
        if (tasks.isEmpty()) {
            return Pair(0, 0)
        }
        
        // Sort tasks by due date
        val sortedTasks = tasks.sortedBy { it.dueDate }
        
        // Group tasks by date
        val tasksByDate = sortedTasks.groupBy { task ->
            val calendar = Calendar.getInstance()
            calendar.time = task.dueDate
            // Reset time to get just the date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.time
        }
        
        // Calculate current streak
        var currentStreak = 0
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val cal = Calendar.getInstance()
        cal.time = today.time
        
        // Check previous days until we find a day with no completed tasks
        while (true) {
            val dayTasks = tasksByDate[cal.time] ?: emptyList()
            
            // If there are no tasks for this day, or less than 50% of tasks are completed, break the streak
            if (dayTasks.isEmpty()) {
                // If this is today and there are no tasks, don't break the streak yet
                if (cal.time == today.time) {
                    // Continue to previous day
                } else {
                    break
                }
            } else {
                // Calculate completion percentage
                val completedTasks = dayTasks.count { it.isCompleted }
                val completionPercentage = (completedTasks.toFloat() / dayTasks.size) * 100
                
                // Check if at least 50% of tasks are completed
                if (completionPercentage >= 50) {
                    // If 50% or more tasks are completed, increment the streak
                    currentStreak++
                } else {
                    // Less than 50% of tasks completed, break the streak
                    break
                }
            }
            
            // Move to the previous day
            cal.add(Calendar.DAY_OF_YEAR, -1)
            
            // Limit the streak calculation to the last 90 days to avoid infinite loops
            if (currentStreak > 90) {
                break
            }
        }
        
        // For new users, the best streak should be the same as current streak
        // until they've used the app long enough to have a different best streak
        var bestStreak = currentStreak
        
        // Only calculate historical best streak if user has been using the app for a while
        // This prevents showing an incorrect best streak for new users
        if (tasks.isNotEmpty() && tasks.any { it.createdAt.time < today.timeInMillis - (7 * 24 * 60 * 60 * 1000) }) {
            var tempStreak = 0
            
            // Iterate through all days with tasks
            val allDates = tasksByDate.keys.sorted()
            for (i in allDates.indices) {
                val date = allDates[i]
                val dayTasks = tasksByDate[date] ?: emptyList()
                
                // Calculate completion percentage for this day
                val completedTasks = dayTasks.count { it.isCompleted }
                val completionPercentage = if (dayTasks.isNotEmpty()) {
                    (completedTasks.toFloat() / dayTasks.size) * 100
                } else {
                    0f
                }
                
                // If at least 50% of tasks are completed on this day
                if (completionPercentage >= 50) {
                    tempStreak++
                    
                    // Check if this is the last date or if the next date is not consecutive
                    if (i == allDates.size - 1 || !isConsecutiveDay(date, allDates[i + 1])) {
                        // Update best streak if current temp streak is better
                        if (tempStreak > bestStreak) {
                            bestStreak = tempStreak
                        }
                        tempStreak = 0
                    }
                } else {
                    // Less than 50% of tasks completed on this day, reset temp streak
                    if (tempStreak > bestStreak) {
                        bestStreak = tempStreak
                    }
                    tempStreak = 0
                }
            }
        }
        
        return Pair(currentStreak, bestStreak)
    }
    
    /**
     * Check if two dates are consecutive days
     */
    private fun isConsecutiveDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        
        cal1.add(Calendar.DAY_OF_YEAR, 1)
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Get streak level information based on streak days
     * @return StreakLevel object containing level, phase, name, color, icon, and description
     */
    private fun getStreakLevel(streak: Int): StreakLevel {
        return when {
            streak >= 7301 -> StreakLevel(
                level = 12,
                phase = "transcendence",
                name = "Infinite",
                color = "purple-pink-red",
                icon = "🌈",
                description = "Achieved legendary status!",
                minDays = 7301,
                maxDays = 999999 // Effectively infinity
            )
            streak >= 3651 -> StreakLevel(
                level = 11,
                phase = "transcendence",
                name = "Ethereal",
                color = "yellow",
                icon = "✨",
                description = "Mastery beyond comprehension",
                minDays = 3651,
                maxDays = 7300
            )
            streak >= 1826 -> StreakLevel(
                level = 10,
                phase = "transcendence",
                name = "Prism",
                color = "white",
                icon = "💎",
                description = "A beacon of consistency",
                minDays = 1826,
                maxDays = 3650
            )
            streak >= 1096 -> StreakLevel(
                level = 9,
                phase = "transcendence",
                name = "Aurora",
                color = "pink",
                icon = "🌌",
                description = "Dancing with greatness",
                minDays = 1096,
                maxDays = 1825
            )
            streak >= 731 -> StreakLevel(
                level = 8,
                phase = "transcendence",
                name = "Cosmos",
                color = "purple",
                icon = "🔮",
                description = "Reaching for the stars",
                minDays = 731,
                maxDays = 1095
            )
            streak >= 366 -> StreakLevel(
                level = 7,
                phase = "transformation",
                name = "Star",
                color = "indigo",
                icon = "⭐",
                description = "Shining bright!",
                minDays = 366,
                maxDays = 730
            )
            streak >= 181 -> StreakLevel(
                level = 6,
                phase = "transformation",
                name = "Sky",
                color = "blue",
                icon = "🌌",
                description = "Soaring high!",
                minDays = 181,
                maxDays = 365
            )
            streak >= 91 -> StreakLevel(
                level = 5,
                phase = "transformation",
                name = "Wave",
                color = "cyan",
                icon = "🌊",
                description = "Riding the momentum!",
                minDays = 91,
                maxDays = 180
            )
            streak >= 42 -> StreakLevel(
                level = 4,
                phase = "transformation",
                name = "Flame",
                color = "green",
                icon = "🔥",
                description = "Burning bright!",
                minDays = 42,
                maxDays = 90
            )
            streak >= 21 -> StreakLevel(
                level = 3,
                phase = "foundation",
                name = "Bloom",
                color = "yellow",
                icon = "🌼",
                description = "Growing stronger!",
                minDays = 21,
                maxDays = 41
            )
            streak >= 7 -> StreakLevel(
                level = 2,
                phase = "foundation",
                name = "Sprout",
                color = "orange",
                icon = "🌱",
                description = "Breaking through!",
                minDays = 7,
                maxDays = 20
            )
            else -> StreakLevel(
                level = 1,
                phase = "foundation",
                name = "Seed",
                color = "red",
                icon = "💭",
                description = "Beginning your journey!",
                minDays = 0,
                maxDays = 6
            )
        }
    }
    
    /**
     * Get gradient colors based on streak level color name
     */
    private fun getGradientColors(colorName: String): IntArray {
        return when (colorName) {
            "purple-pink-red" -> intArrayOf(
                Color.parseColor("#a855f7"),
                Color.parseColor("#ec4899"),
                Color.parseColor("#ef4444")
            )
            "yellow" -> intArrayOf(
                Color.parseColor("#fcd34d"),
                Color.parseColor("#f59e0b")
            )
            "white" -> intArrayOf(
                Color.parseColor("#f3f4f6"),
                Color.parseColor("#d1d5db")
            )
            "pink" -> intArrayOf(
                Color.parseColor("#f472b6"),
                Color.parseColor("#db2777")
            )
            "purple" -> intArrayOf(
                Color.parseColor("#a855f7"),
                Color.parseColor("#7e22ce")
            )
            "indigo" -> intArrayOf(
                Color.parseColor("#818cf8"),
                Color.parseColor("#4f46e5")
            )
            "blue" -> intArrayOf(
                Color.parseColor("#60a5fa"),
                Color.parseColor("#2563eb")
            )
            "cyan" -> intArrayOf(
                Color.parseColor("#22d3ee"),
                Color.parseColor("#0891b2")
            )
            "green" -> intArrayOf(
                Color.parseColor("#4ade80"),
                Color.parseColor("#16a34a")
            )
            "orange" -> intArrayOf(
                Color.parseColor("#fb923c"),
                Color.parseColor("#ea580c")
            )
            "red" -> intArrayOf(
                Color.parseColor("#f87171"),
                Color.parseColor("#dc2626")
            )
            else -> intArrayOf(
                Color.parseColor("#f87171"),
                Color.parseColor("#dc2626")
            )
        }
    }
    
    private fun setupTaskAnalytics() {
        // Observe completed tasks count
        taskViewModel.completedTasksCount.observe(viewLifecycleOwner) { completedCount ->
            binding.textCompletedCount.text = completedCount.toString()
        }
        
        // Observe pending tasks count
        taskViewModel.pendingTasksCount.observe(viewLifecycleOwner) { pendingCount ->
            binding.textPendingCount.text = pendingCount.toString()
        }
    }
    
    // Productivity score section removed as requested
    
    private fun setupWeeklyCompletionChart() {
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            // Group tasks by day of the week
            val tasksByDay = groupTasksByDayOfWeek(tasks)
            
            // Calculate completion rates for each day
            val entries = ArrayList<BarEntry>()
            for (i in 0 until 7) {
                val dayTasks = tasksByDay[i] ?: emptyList()
                val completionRate = if (dayTasks.isNotEmpty()) {
                    val completed = dayTasks.count { it.isCompleted }
                    completed.toFloat() / dayTasks.size
                } else {
                    0f
                }
                entries.add(BarEntry(i.toFloat(), completionRate))
            }
            
            val dataSet = BarDataSet(entries, "Completion Rate")
            dataSet.color = ContextCompat.getColor(requireContext(), R.color.primary)
            
            val data = BarData(dataSet)
            data.setValueTextSize(10f)
            data.setValueFormatter(PercentFormatter())
            
            // Configure chart
            with(binding.chartWeekly) {
                this.data = data
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setScaleEnabled(false)
                setPinchZoom(false)
                
                // X-axis customization
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
                xAxis.granularity = 1f
                
                // Y-axis customization
                axisLeft.setDrawGridLines(true)
                axisLeft.axisMinimum = 0f
                axisLeft.axisMaximum = 1f
                axisLeft.valueFormatter = PercentFormatter()
                axisRight.isEnabled = false
                
                // Animate chart
                animateY(1000)
                invalidate()
            }
        }
    }
    
    private fun setupCategoryChart() {
        taskViewModel.taskCountByCategory.observe(viewLifecycleOwner) { categoryList ->
            if (categoryList.isNotEmpty()) {
                val entries = ArrayList<PieEntry>()
                
                for (category in categoryList) {
                    entries.add(PieEntry(category.count.toFloat(), category.category))
                }
                
                val dataSet = PieDataSet(entries, "")
                dataSet.colors = categoryColors.toList()
                dataSet.sliceSpace = 3f
                dataSet.selectionShift = 5f
                dataSet.valueTextSize = 14f
                dataSet.valueTextColor = Color.WHITE
                dataSet.valueFormatter = PercentFormatter()
                dataSet.iconsOffset = MPPointF(0f, 40f)
                
                val data = PieData(dataSet)
                
                // Configure chart
                with(binding.chartCategories) {
                    this.data = data
                    description.isEnabled = false
                    setUsePercentValues(true)
                    setExtraOffsets(5f, 10f, 5f, 5f)
                    dragDecelerationFrictionCoef = 0.95f
                    isDrawHoleEnabled = true
                    setHoleColor(Color.WHITE)
                    setTransparentCircleColor(Color.WHITE)
                    setTransparentCircleAlpha(110)
                    holeRadius = 58f
                    transparentCircleRadius = 61f
                    setDrawCenterText(true)
                    rotationAngle = 0f
                    isRotationEnabled = true
                    isHighlightPerTapEnabled = true
                    centerText = "Categories"
                    setCenterTextSize(16f)
                    
                    // Configure legend
                    legend.isEnabled = true
                    legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                    legend.orientation = Legend.LegendOrientation.VERTICAL
                    legend.setDrawInside(false)
                    legend.textSize = 12f
                    
                    // Animate chart
                    animateY(1400, Easing.EaseInOutQuad)
                    invalidate()
                }
            }
        }
    }
    
    private fun groupTasksByDayOfWeek(tasks: List<Task>): Map<Int, List<Task>> {
        val calendar = Calendar.getInstance()
        val now = Date()
        val startOfWeek = calendar.apply {
            time = now
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val tasksByDay = mutableMapOf<Int, MutableList<Task>>()
        
        // Initialize empty lists for each day
        for (i in 0 until 7) {
            tasksByDay[i] = mutableListOf()
        }
        
        // Group tasks by day of the week
        for (task in tasks) {
            val taskCalendar = Calendar.getInstance().apply { time = task.dueDate }
            val dayOfWeek = (taskCalendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
            tasksByDay[dayOfWeek]?.add(task)
        }
        
        return tasksByDay
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
