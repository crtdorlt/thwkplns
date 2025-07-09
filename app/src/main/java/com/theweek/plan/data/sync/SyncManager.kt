package com.theweek.plan.data.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.theweek.plan.data.AppDatabase
import com.theweek.plan.data.repository.TaskSyncRepository
import com.theweek.plan.data.repository.UserRepository
import com.theweek.plan.data.remote.SupabaseClient
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manager for handling synchronization between local database and Supabase
 */
class SyncManager(private val context: Context) {

    private val TAG = "SyncManager"
    private val database = AppDatabase.getDatabase(context)
    private val taskDao = database.taskDao()
    private val taskSyncRepository: TaskSyncRepository by lazy { TaskSyncRepository(context) }
    private val userRepository: UserRepository by lazy { UserRepository(context) }
    
    private val syncJob = SupervisorJob()
    private val syncScope = CoroutineScope(Dispatchers.IO + syncJob)
    private val isSyncing = AtomicBoolean(false)
    private var realtimeSubscriptionJob: Job? = null
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    }
    
    /**
     * Initialize the sync manager
     */
    fun initialize() {
        Log.d(TAG, "Initializing sync manager")
        
        // Start real-time subscription if user is authenticated
        syncScope.launch {
            try {
                if (userRepository.isAuthenticated()) {
                    startRealtimeSubscription()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting realtime subscription", e)
            }
        }
    }
    
    /**
     * Start the sync process
     */
    fun startSync() {
        if (isSyncing.getAndSet(true)) {
            Log.d(TAG, "Sync already in progress")
            return
        }
        
        syncScope.launch {
            try {
                Log.d(TAG, "Starting sync process")
                
                if (!userRepository.isAuthenticated()) {
                    Log.d(TAG, "User not authenticated, skipping sync")
                    return@launch
                }
                
                // Perform bidirectional sync
                performSync()
                
                Log.d(TAG, "Sync completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync", e)
            } finally {
                isSyncing.set(false)
            }
        }
    }
    
    /**
     * Perform a full sync
     */
    suspend fun performSync(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting bidirectional sync...")
                
                if (!userRepository.isAuthenticated()) {
                    Log.d(TAG, "User not authenticated, cannot sync")
                    return@withContext false
                }
                
                // Get all local tasks
                val localTasks = taskDao.getAllTasks().value ?: emptyList()
                
                // Sync local changes to Supabase
                syncLocalToRemote(localTasks)
                
                // Fetch remote changes from Supabase
                syncRemoteToLocal()
                
                Log.d(TAG, "Bidirectional sync completed successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync", e)
                false
            }
        }
    }
    
    /**
     * Sync local changes to Supabase
     */
    private suspend fun syncLocalToRemote(localTasks: List<com.theweek.plan.model.Task>) {
        Log.d(TAG, "Syncing ${localTasks.size} local tasks to Supabase")
        
        val result = taskSyncRepository.syncTasksToSupabase(localTasks)
        result.fold(
            onSuccess = {
                Log.d(TAG, "Local tasks synced to Supabase successfully")
            },
            onFailure = { exception ->
                Log.e(TAG, "Failed to sync local tasks to Supabase", exception)
                throw exception
            }
        )
    }
    
    /**
     * Fetch remote changes from Supabase
     */
    private suspend fun syncRemoteToLocal() {
        Log.d(TAG, "Fetching remote changes from Supabase")
        
        val lastSyncTimestamp = getLastSyncTimestamp()
        val result = taskSyncRepository.fetchTasksFromSupabase(lastSyncTimestamp)
        
        result.fold(
            onSuccess = { remoteTasks ->
                Log.d(TAG, "Fetched ${remoteTasks.size} tasks from Supabase")
                
                // Update local database with remote tasks
                remoteTasks.forEach { task ->
                    taskDao.insertTask(task)
                }
                
                // Update last sync timestamp
                setLastSyncTimestamp(System.currentTimeMillis())
            },
            onFailure = { exception ->
                Log.e(TAG, "Failed to fetch tasks from Supabase", exception)
                throw exception
            }
        )
    }
    
    /**
     * Start real-time subscription for task updates
     */
    private suspend fun startRealtimeSubscription() {
        val currentUser = SupabaseClient.currentUserOrNull()
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user, cannot start realtime subscription")
            return
        }
        
        realtimeSubscriptionJob?.cancel()
        realtimeSubscriptionJob = syncScope.launch {
            try {
                Log.d(TAG, "Starting realtime subscription for user ${currentUser.id}")
                
                taskSyncRepository.subscribeToTaskUpdates(currentUser.id).collect { change ->
                    Log.d(TAG, "Received realtime update: $change")
                    // Handle realtime updates here
                    // You can trigger a partial sync or update specific tasks
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in realtime subscription", e)
            }
        }
    }
    
    /**
     * Sync a single task when it's created or updated locally
     */
    suspend fun syncSingleTask(task: com.theweek.plan.model.Task) {
        if (!userRepository.isAuthenticated()) {
            Log.d(TAG, "User not authenticated, cannot sync single task")
            return
        }
        
        syncScope.launch {
            try {
                Log.d(TAG, "Syncing single task: ${task.id}")
                
                val result = taskSyncRepository.syncSingleTaskToSupabase(task)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Single task synced successfully")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to sync single task", exception)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing single task", e)
            }
        }
    }
    
    /**
     * Delete a task from both local and remote
     */
    suspend fun deleteTask(taskId: String) {
        if (!userRepository.isAuthenticated()) {
            Log.d(TAG, "User not authenticated, cannot delete task from remote")
            return
        }
        
        syncScope.launch {
            try {
                Log.d(TAG, "Deleting task: $taskId")
                
                // Delete from Supabase
                val result = taskSyncRepository.deleteTaskFromSupabase(taskId)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Task deleted from Supabase successfully")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to delete task from Supabase", exception)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task", e)
            }
        }
    }
    
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
     * Stop the sync process and cancel all jobs
     */
    fun stopSync() {
        Log.d(TAG, "Stopping sync process")
        realtimeSubscriptionJob?.cancel()
        realtimeSubscriptionJob = null
        syncJob.cancel()
    }
    
    /**
     * Clean up resources when the app is terminated
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up sync manager")
        stopSync()
    }
}