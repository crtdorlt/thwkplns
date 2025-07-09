package com.theweek.plan.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.theweek.plan.model.Task
import java.util.Date

/**
 * Data class to hold category count results
 */
data class CategoryCount(
    val category: String,
    val count: Int
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, priority DESC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: String): LiveData<Task>

    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC, priority DESC")
    fun getTasksByDateRange(startDate: Date, endDate: Date): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY dueDate ASC, priority DESC")
    fun getTasksByCompletionStatus(isCompleted: Boolean): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY dueDate ASC, priority DESC")
    fun getTasksByCategory(category: String): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskCompletionStatus(taskId: String, isCompleted: Boolean, updatedAt: Date = Date())

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    fun getCompletedTasksCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getPendingTasksCount(): LiveData<Int>

    @Query("SELECT AVG(productivityScore) FROM tasks WHERE productivityScore IS NOT NULL")
    fun getAverageProductivityScore(): LiveData<Float>

    @Query("SELECT category, COUNT(*) as count FROM tasks GROUP BY category ORDER BY count DESC")
    fun getTaskCountByCategory(): LiveData<List<CategoryCount>>
}
