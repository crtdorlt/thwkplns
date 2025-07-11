package com.theweek.plan.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.theweek.plan.data.remote.SupabaseClient
import com.theweek.plan.model.Task
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.Date

/**
 * Repository for syncing tasks with Supabase
 */
class TaskSyncRepository(private val context: Context) {
    private val TAG = "TaskSyncRepository"

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    }

    @Serializable
    data class TaskRow(
        val id: String,
        val user_id: String,
        val title: String,
        val description: String = "",
        val category: String,
        val priority: Int,
        val due_date: Long,
        val due_time: Long? = null,
        val is_completed: Boolean = false,
        val productivity_score: Int? = null,
        val created_at: Long,
        val updated_at: Long
    )

    /**
     * Get the last sync timestamp
     */
    private fun getLastSyncTimestamp(): Long {
        return sharedPreferences.getLong("last_sync_timestamp", 0)
    }

    /**
     * Set the last sync timestamp
     */
    private fun setLastSyncTimestamp(timestamp: Long) {
        sharedPreferences.edit().putLong("last_sync_timestamp", timestamp).apply()
    }

    /**
     * Sync local tasks to Supabase
     */
    suspend fun syncTasksToSupabase(localTasks: List<Task>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = SupabaseClient.currentUserOrNull()
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Syncing ${localTasks.size} tasks to Supabase")
            
            // Convert tasks to TaskRow format
            val taskRows = localTasks.map { task ->
                TaskRow(
                    id = task.id,
                    user_id = currentUser.id,
                    title = task.title,
                    description = task.description,
                    category = task.category,
                    priority = task.priority,
                    due_date = task.dueDate.time,
                    due_time = task.dueTime?.time,
                    is_completed = task.isCompleted,
                    productivity_score = task.productivityScore,
                    created_at = task.createdAt.time,
                    updated_at = task.updatedAt.time
                )
            }
            
            // Upsert tasks to Supabase
            if (taskRows.isNotEmpty()) {
                SupabaseClient.database
                    .from("tasks")
                    .upsert(taskRows)
            }
            
            // Update last sync timestamp
            setLastSyncTimestamp(System.currentTimeMillis())
            
            Log.d(TAG, "Tasks synced successfully to Supabase")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing tasks to Supabase", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch tasks from Supabase
     */
    suspend fun fetchTasksFromSupabase(since: Long? = null): Result<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val currentUser = SupabaseClient.currentUserOrNull()
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Fetching tasks from Supabase since $since")
            
            val query = SupabaseClient.database
                .from("tasks")
                .select()
                .eq("user_id", currentUser.id)
            
            // Add timestamp filter if provided
            val response = if (since != null) {
                query.gt("updated_at", since).decodeList<TaskRow>()
            } else {
                query.decodeList<TaskRow>()
            }
            
            // Convert TaskRow to Task
            val tasks = response.map { taskRow ->
                Task(
                    id = taskRow.id,
                    title = taskRow.title,
                    description = taskRow.description,
                    category = taskRow.category,
                    priority = taskRow.priority,
                    dueDate = Date(taskRow.due_date),
                    dueTime = taskRow.due_time?.let { Date(it) },
                    isCompleted = taskRow.is_completed,
                    productivityScore = taskRow.productivity_score,
                    createdAt = Date(taskRow.created_at),
                    updatedAt = Date(taskRow.updated_at)
                )
            }
            
            // Update last sync timestamp
            setLastSyncTimestamp(System.currentTimeMillis())
            
            Log.d(TAG, "Fetched ${tasks.size} tasks from Supabase")
            Result.success(tasks)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching tasks from Supabase", e)
            Result.failure(e)
        }
    }

    /**
     * Subscribe to real-time task updates
     */
    fun subscribeToTaskUpdates(userId: String): Flow<String> = flow {
        try {
            Log.d(TAG, "Subscribing to real-time updates for user $userId")
            
            val channel = SupabaseClient.realtime.channel("tasks")
            
            // Subscribe to changes
            channel.subscribe()
            
            // Emit a simple message for now
            emit("Subscribed to real-time updates")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to real-time updates", e)
            emit("Error: ${e.message}")
        }
    }

    /**
     * Delete a task from Supabase
     */
    suspend fun deleteTaskFromSupabase(taskId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = SupabaseClient.currentUserOrNull()
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Deleting task $taskId from Supabase")
            
            SupabaseClient.database
                .from("tasks")
                .delete()
                .eq("id", taskId)
                .eq("user_id", currentUser.id)
            
            Log.d(TAG, "Task deleted successfully from Supabase")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task from Supabase", e)
            Result.failure(e)
        }
    }

    /**
     * Sync a single task to Supabase
     */
    suspend fun syncSingleTaskToSupabase(task: Task): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = SupabaseClient.currentUserOrNull()
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Syncing single task ${task.id} to Supabase")
            
            val taskRow = TaskRow(
                id = task.id,
                user_id = currentUser.id,
                title = task.title,
                description = task.description,
                category = task.category,
                priority = task.priority,
                due_date = task.dueDate.time,
                due_time = task.dueTime?.time,
                is_completed = task.isCompleted,
                productivity_score = task.productivityScore,
                created_at = task.createdAt.time,
                updated_at = task.updatedAt.time
            )
            
            SupabaseClient.database
                .from("tasks")
                .upsert(taskRow)
            
            Log.d(TAG, "Single task synced successfully to Supabase")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing single task to Supabase", e)
            Result.failure(e)
        }
    }
}