package com.theweek.plan.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val category: String,
    val priority: Int, // 1 = Low, 2 = Medium, 3 = High
    val dueDate: Date,
    val dueTime: Date? = null,
    val isCompleted: Boolean = false,
    val productivityScore: Int? = null, // 1-5 score
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
