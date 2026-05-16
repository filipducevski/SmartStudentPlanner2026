package com.smartstudent.planner.ui.exams

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
import com.smartstudent.planner.data.local.entities.ExamEntity
import com.smartstudent.planner.data.local.entities.SubjectEntity
import com.smartstudent.planner.databinding.ActivityExamDetailBinding
import com.smartstudent.planner.util.UiState
import com.smartstudent.planner.viewmodel.ExamViewModel
import com.smartstudent.planner.viewmodel.SubjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ExamDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExamDetailBinding
    private val examViewModel: ExamViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()

    private var selectedDate: Long = System.currentTimeMillis()
    private var selectedTime: String = ""
    private var selectedSubject: SubjectEntity? = null
    private var selectedReminderOffset: Long? = null
    private var editingExam: ExamEntity? = null
    private var subjectList: List<SubjectEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editingExam = examViewModel.selectedExam.value

        if (editingExam != null) {
            supportActionBar?.title = getString(R.string.edit_exam)
            populateFields(editingExam!!)
        } else {
            supportActionBar?.title = getString(R.string.add_exam)
        }

        setupExamTypeChips()
        setupReminderSpinner()
        observeSubjects()
        setupClickListeners()
        observeOperationState()
    }

    private fun populateFields(exam: ExamEntity) {
        binding.etExamTitle.setText(exam.title)
        binding.etLocation.setText(exam.location)
        binding.etDuration.setText(exam.durationMinutes.toString())
        binding.etNotes.setText(exam.notes)
        selectedDate = exam.examDate
        selectedTime = exam.examTime
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        binding.btnSelectDate.text = sdf.format(Date(exam.examDate))
        if (exam.examTime.isNotEmpty()) binding.btnSelectTime.text = exam.examTime
        selectedReminderOffset = exam.reminderOffsetMinutes
    }

    private fun setupExamTypeChips() {
        val types = listOf(
            binding.chipWritten to ExamEntity.ExamType.WRITTEN,
            binding.chipOral to ExamEntity.ExamType.ORAL,
            binding.chipPractical to ExamEntity.ExamType.PRACTICAL,
            binding.chipOnline to ExamEntity.ExamType.ONLINE
        )
        types.forEach { (chip, type) ->
            chip.setOnClickListener {
                types.forEach { (c, _) -> c.isChecked = false }
                chip.isChecked = true
            }
        }
        // Pre-select
        editingExam?.let { exam ->
            val selected = try { ExamEntity.ExamType.valueOf(exam.examType) } catch (_: Exception) { ExamEntity.ExamType.WRITTEN }
            val chip = when (selected) {
                ExamEntity.ExamType.ORAL -> binding.chipOral
                ExamEntity.ExamType.PRACTICAL -> binding.chipPractical
                ExamEntity.ExamType.ONLINE -> binding.chipOnline
                else -> binding.chipWritten
            }
            chip.isChecked = true
        } ?: run { binding.chipWritten.isChecked = true }
    }

    private fun setupReminderSpinner() {
        val options = arrayOf(
            getString(R.string.remind_none),
            getString(R.string.remind_1_day),
            getString(R.string.remind_2_days),
            getString(R.string.remind_1_week)
        )
        val offsets = arrayOf<Long?>(null, 1440, 2880, 10080)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerReminder.adapter = adapter
        binding.spinnerReminder.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selectedReminderOffset = offsets[pos]
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun observeSubjects() {
        subjectViewModel.subjects.observe(this) { subjects ->
            subjectList = subjects
            val names = listOf(getString(R.string.select_subject)) + subjects.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSubject.adapter = adapter
            editingExam?.subjectId?.let { sid ->
                val idx = subjects.indexOfFirst { it.id == sid }
                if (idx >= 0) binding.spinnerSubject.setSelection(idx + 1)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSelectTime.setOnClickListener { showTimePicker() }
        binding.btnSave.setOnClickListener { saveExam() }
        binding.btnDelete.apply {
            visibility = if (editingExam != null) View.VISIBLE else View.GONE
            setOnClickListener { confirmDelete() }
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d)
            selectedDate = cal.timeInMillis
            val sdf = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            binding.btnSelectDate.text = sdf.format(cal.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            selectedTime = String.format("%02d:%02d", hour, minute)
            binding.btnSelectTime.text = selectedTime
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun getSelectedExamType(): String {
        return when {
            binding.chipOral.isChecked -> ExamEntity.ExamType.ORAL.name
            binding.chipPractical.isChecked -> ExamEntity.ExamType.PRACTICAL.name
            binding.chipOnline.isChecked -> ExamEntity.ExamType.ONLINE.name
            else -> ExamEntity.ExamType.WRITTEN.name
        }
    }

    private fun saveExam() {
        val title = binding.etExamTitle.text?.toString()?.trim() ?: ""
        if (title.isEmpty()) {
            binding.tilExamTitle.error = getString(R.string.error_empty_field)
            return
        }
        binding.tilExamTitle.error = null

        val subjectIndex = binding.spinnerSubject.selectedItemPosition
        val subject = if (subjectIndex > 0) subjectList.getOrNull(subjectIndex - 1) else null
        val duration = binding.etDuration.text?.toString()?.toIntOrNull() ?: 90

        val exam = (editingExam ?: ExamEntity(title = title, examDate = selectedDate)).copy(
            title = title,
            subjectId = subject?.id,
            subjectName = subject?.name ?: "",
            subjectColor = subject?.color ?: "#2563EB",
            examDate = selectedDate,
            examTime = selectedTime,
            location = binding.etLocation.text?.toString()?.trim() ?: "",
            durationMinutes = duration,
            examType = getSelectedExamType(),
            notes = binding.etNotes.text?.toString()?.trim() ?: "",
            reminderOffsetMinutes = selectedReminderOffset,
            updatedAt = System.currentTimeMillis()
        )

        if (editingExam != null) examViewModel.updateExam(exam)
        else examViewModel.saveExam(exam)
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                editingExam?.let { examViewModel.deleteExam(it.id) }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeOperationState() {
        examViewModel.operationState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> { binding.progressBar.visibility = View.GONE; finish() }
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
