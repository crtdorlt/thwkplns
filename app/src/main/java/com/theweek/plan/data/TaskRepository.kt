package com.theweek.plan.data

import androidx.lifecycle.LiveData
import com.theweek.plan.data.sync.SyncManager
import com.theweek.plan.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * Repository class that acts as a mediator between the ViewModel and the data sources.
 * Now includes Supabase synchronization
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val syncManager: SyncManager? = null
) {

    // Get all tasks
    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()
    
    // Get completed tasks count
    val completedTasksCount: LiveData<Int> = taskDao.getCompletedTasksCount()
    
    // Get pending tasks count
    val pendingTasksCount: LiveData<Int> = taskDao.getPendingTasksCount()
    
    // Get average productivity score
    val averageProductivityScore: LiveData<Float> = taskDao.getAverageProductivityScore()
    
    // Get task count by category
    val taskCountByCategory: LiveData<List<CategoryCount>> = taskDao.getTaskCountByCategory()

    // Get a specific task by ID
    fun getTaskById(taskId: String): LiveData<Task> {
        return taskDao.getTaskById(taskId)
    }

    // Get tasks for a specific date range
    fun getTasksByDateRange(startDate: Date, endDate: Date): LiveData<List<Task>> {
        return taskDao.getTasksByDateRange(startDate, endDate)
    }

    // Get tasks by completion status
    fun getTasksByCompletionStatus(isCompleted: Boolean): LiveData<List<Task>> {
        return taskDao.getTasksByCompletionStatus(isCompleted)
    }

    // Get tasks by category
    fun getTasksByCategory(category: String): LiveData<List<Task>> {
        return taskDao.getTasksByCategory(category)
    }
    
    // Get tasks for a specific date
    fun getTasksByDate(date: Date): LiveData<List<Task>> {
        // Create a calendar to manipulate the date
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        // Set time to start of day (00:00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        // Set time to end of day (23:59:59)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time
        
        // Use the existing date range method
        return taskDao.getTasksByDateRange(startDate, endDate)
    }

    // Insert a new task
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
        // Sync to Supabase if available
        syncManager?.syncSingleTask(task)
    }

    // Update an existing task
    suspend fun updateTask(task: Task) {
        val updatedTask = task.copy(updatedAt = Date())
        taskDao.updateTask(updatedTask)
        // Sync to Supabase if available
        syncManager?.syncSingleTask(updatedTask)
    }

    // Delete a task
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        // Delete from Supabase if available
        syncManager?.deleteTask(task.id)
    }

    // Delete a task by ID
    suspend fun deleteTaskById(taskId: String) {
        taskDao.deleteTaskById(taskId)
        // Delete from Supabase if available
        syncManager?.deleteTask(taskId)
    }

    // Update task completion status
    suspend fun updateTaskCompletionStatus(taskId: String, isCompleted: Boolean) {
        taskDao.updateTaskCompletionStatus(taskId, isCompleted, Date())
        
        // Sync the updated task to Supabase if available
        syncManager?.let { sync ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Get the updated task and sync it
                    val task = taskDao.getTaskById(taskId).value
                    task?.let { 
                        val updatedTask = it.copy(isCompleted = isCompleted, updatedAt = Date())
                        sync.syncSingleTask(updatedTask)
                    }
                } catch (e: Exception) {
                    // Log error but don't crash
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Trigger a full sync with Supabase
     */
    suspend fun performFullSync(): Boolean {
        return syncManager?.performSync() ?: false
    }

    /**
     * Start automatic sync
     */
    fun startSync() {
        syncManager?.startSync()
    }
}