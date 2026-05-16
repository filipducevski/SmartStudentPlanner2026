package com.smartstudent.planner.ui.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.smartstudent.planner.R
import com.smartstudent.planner.data.local.entities.SubjectEntity
import com.smartstudent.planner.data.local.entities.TaskEntity
import com.smartstudent.planner.databinding.ActivityTaskDetailBinding
import com.smartstudent.planner.util.UiState
import com.smartstudent.planner.viewmodel.SubjectViewModel
import com.smartstudent.planner.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()

    private var selectedDueDate: Long? = null
    private var selectedDueTime: String? = null
    private var selectedSubject: SubjectEntity? = null
    private var selectedPriority = TaskEntity.Priority.MEDIUM
    private var selectedTaskType = TaskEntity.TaskType.HOMEWORK
    private var selectedReminderOffset: Long? = null
    private var editingTask: TaskEntity? = null
    private var subjectList: List<SubjectEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editingTask = taskViewModel.selectedTask.value

        if (editingTask != null) {
            supportActionBar?.title = getString(R.string.edit_task)
            populateFields(editingTask!!)
        } else {
            supportActionBar?.title = getString(R.string.add_task)
        }

        setupPriorityChips()
        setupTaskTypeChips()
        setupReminderSpinner()
        observeSubjects()
        setupClickListeners()
        observeOperationState()
    }

    private fun populateFields(task: TaskEntity) {
        binding.etTaskTitle.setText(task.title)
        binding.etDescription.setText(task.description)
        binding.etNotes.setText(task.notes)
        selectedDueDate = task.dueDate
        selectedDueTime = task.dueTime
        task.dueDate?.let {
            val sdf = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            binding.btnSelectDate.text = sdf.format(Date(it))
        }
        task.dueTime?.let { binding.btnSelectTime.text = it }
        selectedPriority = TaskEntity.Priority.fromValue(task.priority)
        selectedTaskType = try { TaskEntity.TaskType.valueOf(task.taskType) } catch (_: Exception) { TaskEntity.TaskType.HOMEWORK }
        selectedReminderOffset = task.reminderOffsetMinutes
    }

    private fun setupPriorityChips() {
        val priorities = listOf(
            binding.chipLow to TaskEntity.Priority.LOW,
            binding.chipMedium to TaskEntity.Priority.MEDIUM,
            binding.chipHigh to TaskEntity.Priority.HIGH,
            binding.chipCritical to TaskEntity.Priority.CRITICAL
        )
        priorities.forEach { (chip, priority) ->
            chip.setOnClickListener {
                selectedPriority = priority
                priorities.forEach { (c, _) -> c.isChecked = false }
                chip.isChecked = true
            }
        }
        // Default selection
        binding.chipMedium.isChecked = true
    }

    private fun setupTaskTypeChips() {
        val types = listOf(
            binding.chipHomework to TaskEntity.TaskType.HOMEWORK,
            binding.chipProject to TaskEntity.TaskType.PROJECT,
            binding.chipReading to TaskEntity.TaskType.READING,
            binding.chipAssignment to TaskEntity.TaskType.ASSIGNMENT,
            binding.chipLab to TaskEntity.TaskType.LAB,
            binding.chipOther to TaskEntity.TaskType.OTHER
        )
        types.forEach { (chip, type) ->
            chip.setOnClickListener {
                selectedTaskType = type
                types.forEach { (c, _) -> c.isChecked = false }
                chip.isChecked = true
            }
        }
        binding.chipHomework.isChecked = true
    }

    private fun setupReminderSpinner() {
        val options = arrayOf(
            getString(R.string.remind_none),
            getString(R.string.remind_1_hour),
            getString(R.string.remind_3_hours),
            getString(R.string.remind_1_day),
            getString(R.string.remind_2_days),
            getString(R.string.remind_1_week)
        )
        val offsets = arrayOf<Long?>(null, 60, 180, 1440, 2880, 10080)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerReminder.adapter = spinnerAdapter
        binding.spinnerReminder.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedReminderOffset = offsets[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun observeSubjects() {
        subjectViewModel.subjects.observe(this) { subjects ->
            subjectList = subjects
            val names = listOf(getString(R.string.select_subject)) + subjects.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSubject.adapter = adapter

            // Pre-select if editing
            editingTask?.subjectId?.let { sid ->
                val index = subjects.indexOfFirst { it.id == sid }
                if (index >= 0) binding.spinnerSubject.setSelection(index + 1)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSelectTime.setOnClickListener { showTimePicker() }
        binding.btnSave.setOnClickListener { saveTask() }
        binding.btnDelete.visibility = if (editingTask != null) View.VISIBLE else View.GONE
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        selectedDueDate?.let { cal.timeInMillis = it }
        DatePickerDialog(
            this,
            { _, year, month, day ->
                cal.set(year, month, day)
                selectedDueDate = cal.timeInMillis
                val sdf = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                binding.btnSelectDate.text = sdf.format(cal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedDueTime = String.format("%02d:%02d", hour, minute)
                binding.btnSelectTime.text = selectedDueTime
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveTask() {
        val title = binding.etTaskTitle.text?.toString()?.trim() ?: ""
        if (title.isEmpty()) {
            binding.tilTaskTitle.error = getString(R.string.error_empty_field)
            return
        }
        binding.tilTaskTitle.error = null

        val subjectIndex = binding.spinnerSubject.selectedItemPosition
        val subject = if (subjectIndex > 0) subjectList.getOrNull(subjectIndex - 1) else null

        val task = (editingTask ?: TaskEntity(title = title)).copy(
            title = title,
            description = binding.etDescription.text?.toString()?.trim() ?: "",
            notes = binding.etNotes.text?.toString()?.trim() ?: "",
            subjectId = subject?.id,
            subjectName = subject?.name ?: "",
            subjectColor = subject?.color ?: "#2563EB",
            dueDate = selectedDueDate,
            dueTime = selectedDueTime,
            priority = selectedPriority.value,
            taskType = selectedTaskType.name,
            reminderOffsetMinutes = selectedReminderOffset,
            updatedAt = System.currentTimeMillis()
        )

        if (editingTask != null) taskViewModel.updateTask(task)
        else taskViewModel.saveTask(task)
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                editingTask?.let { taskViewModel.deleteTask(it.id) }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeOperationState() {
        taskViewModel.operationState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    finish()
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                else -> binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
