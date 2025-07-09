package com.theweek.plan.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.theweek.plan.data.AppDatabase
import com.theweek.plan.data.CategoryCount
import com.theweek.plan.data.TaskRepository
import com.theweek.plan.data.sync.SyncManager
import com.theweek.plan.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val syncManager: SyncManager
    val allTasks: LiveData<List<Task>>
    val completedTasksCount: LiveData<Int>
    val pendingTasksCount: LiveData<Int>
    val averageProductivityScore: LiveData<Float>
    val taskCountByCategory: LiveData<List<CategoryCount>>

    init {
        val taskDao = AppDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        syncManager = SyncManager(application)
        allTasks = repository.allTasks
        completedTasksCount = repository.completedTasksCount
        pendingTasksCount = repository.pendingTasksCount
        averageProductivityScore = repository.averageProductivityScore
        taskCountByCategory = repository.taskCountByCategory
    }

    fun getTaskById(taskId: String): LiveData<Task> {
        return repository.getTaskById(taskId)
    }

    fun getTasksByDateRange(startDate: Date, endDate: Date): LiveData<List<Task>> {
        return repository.getTasksByDateRange(startDate, endDate)
    }

    fun getTasksByCompletionStatus(isCompleted: Boolean): LiveData<List<Task>> {
        return repository.getTasksByCompletionStatus(isCompleted)
    }

    fun getTasksByCategory(category: String): LiveData<List<Task>> {
        return repository.getTasksByCategory(category)
    }
    
    fun getTasksByDate(date: Date): LiveData<List<Task>> {
        return repository.getTasksByDate(date)
    }

    fun insertTask(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTask(task)
        // Sync to Supabase
        syncManager.syncSingleTask(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTask(task)
        // Sync to Supabase
        syncManager.syncSingleTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTask(task)
        // Delete from Supabase
        syncManager.deleteTask(task.id)
    }

    fun deleteTaskById(taskId: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTaskById(taskId)
        // Delete from Supabase
        syncManager.deleteTask(taskId)
    }

    fun updateTaskCompletionStatus(taskId: String, isCompleted: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTaskCompletionStatus(taskId, isCompleted)
        // Sync the updated task to Supabase
        val task = repository.getTaskById(taskId).value
        task?.let { 
            val updatedTask = it.copy(isCompleted = isCompleted, updatedAt = Date())
            syncManager.syncSingleTask(updatedTask)
        }
    }
}