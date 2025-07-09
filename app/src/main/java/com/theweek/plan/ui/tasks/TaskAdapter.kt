package com.theweek.plan.ui.tasks

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.theweek.plan.R
import com.theweek.plan.databinding.ItemTaskBinding
import com.theweek.plan.model.Task
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onCheckboxClick: (Task, Boolean) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                // Set task title and description
                textTitle.text = task.title
                textDescription.text = task.description
                
                // Set checkbox state
                checkboxCompleted.isChecked = task.isCompleted
                
                // Apply strikethrough if task is completed
                if (task.isCompleted) {
                    textTitle.paintFlags = textTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    textTitle.paintFlags = textTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                
                // Set category chip
                chipCategory.text = task.category
                
                // Set date
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                textDate.text = dateFormat.format(task.dueDate)
                
                // Set priority indicator color
                val priorityColor = when (task.priority) {
                    3 -> R.color.red
                    2 -> R.color.orange
                    else -> R.color.green
                }
                viewPriorityIndicator.setBackgroundResource(priorityColor)
                
                // Set click listeners
                root.setOnClickListener { onItemClick(task) }
                
                checkboxCompleted.setOnClickListener {
                    onCheckboxClick(task, checkboxCompleted.isChecked)
                }
                
                // Setup menu button
                buttonMenu.setOnClickListener { view ->
                    showPopupMenu(view, task)
                }
            }
        }
    }

    private fun showPopupMenu(view: View, task: Task) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.menu_task_options)
        
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    onEditClick(task)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick(task)
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
    }
    
    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
