package com.smartstudent.planner.ui.tasks

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartstudent.planner.R
import com.smartstudent.planner.data.local.entities.TaskEntity
import com.smartstudent.planner.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onTaskClick: (TaskEntity) -> Unit,
    private val onTaskChecked: (TaskEntity, Boolean) -> Unit
) : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    private var fullList: List<TaskEntity> = emptyList()

    override fun submitList(list: List<TaskEntity>?) {
        fullList = list ?: emptyList()
        super.submitList(list)
    }

    fun filter(query: String) {
        if (query.isEmpty()) {
            super.submitList(fullList)
        } else {
            val filtered = fullList.filter { task ->
                task.title.contains(query, ignoreCase = true) ||
                task.subjectName.contains(query, ignoreCase = true)
            }
            super.submitList(filtered)
        }
    }

    fun getTaskAt(position: Int): TaskEntity = getItem(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskEntity) {
            binding.apply {
                tvTaskTitle.text = task.title
                cbCompleted.isChecked = task.isCompleted

                // Strike-through for completed
                tvTaskTitle.paintFlags = if (task.isCompleted) {
                    tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                // Subject chip
                if (task.subjectName.isNotEmpty()) {
                    chipSubject.visibility = View.VISIBLE
                    chipSubject.text = task.subjectName
                    try {
                        chipSubject.setChipBackgroundColorResource(android.R.color.transparent)
                        chipSubject.chipStrokeWidth = 1f
                    } catch (_: Exception) {}
                } else {
                    chipSubject.visibility = View.GONE
                }

                // Due date
                task.dueDate?.let { dueMs ->
                    tvDueDate.visibility = View.VISIBLE
                    val sdf = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
                    tvDueDate.text = sdf.format(Date(dueMs))
                    // Overdue highlight
                    val isOverdue = dueMs < System.currentTimeMillis() && !task.isCompleted
                    tvDueDate.setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            if (isOverdue) R.color.error else R.color.on_surface_variant
                        )
                    )
                } ?: run {
                    tvDueDate.visibility = View.GONE
                }

                // Priority indicator
                val priorityColor = when (task.priority) {
                    0 -> R.color.priority_low
                    1 -> R.color.priority_medium
                    2 -> R.color.priority_high
                    3 -> R.color.priority_critical
                    else -> R.color.priority_medium
                }
                viewPriorityIndicator.setBackgroundColor(
                    ContextCompat.getColor(root.context, priorityColor)
                )

                // Task type icon
                val typeIcon = when (task.taskType) {
                    "PROJECT" -> R.drawable.ic_project
                    "READING" -> R.drawable.ic_reading
                    "LAB" -> R.drawable.ic_lab
                    "ASSIGNMENT" -> R.drawable.ic_assignment
                    else -> R.drawable.ic_homework
                }
                ivTaskType.setImageResource(typeIcon)

                // Click listeners
                root.setOnClickListener { onTaskClick(task) }
                cbCompleted.setOnCheckedChangeListener(null)
                cbCompleted.isChecked = task.isCompleted
                cbCompleted.setOnCheckedChangeListener { _, checked ->
                    onTaskChecked(task, checked)
                }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity) =
            oldItem == newItem
    }
}
