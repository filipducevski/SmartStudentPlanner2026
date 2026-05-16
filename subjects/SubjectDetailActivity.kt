package com.smartstudent.planner.ui.subjects

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.smartstudent.planner.R
import com.smartstudent.planner.data.local.entities.SubjectEntity
import com.smartstudent.planner.databinding.ActivitySubjectDetailBinding
import com.smartstudent.planner.util.UiState
import com.smartstudent.planner.viewmodel.SubjectViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectDetailBinding
    private val viewModel: SubjectViewModel by viewModels()
    private var editingSubject: SubjectEntity? = null
    private var selectedColor = "#2563EB"

    private val colorPalette = listOf(
        "#2563EB", "#7C3AED", "#059669", "#D97706",
        "#DC2626", "#0D9488", "#DB2777", "#4F46E5",
        "#0EA5E9", "#65A30D", "#EA580C", "#9333EA"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editingSubject = viewModel.selectedSubject.value

        if (editingSubject != null) {
            supportActionBar?.title = getString(R.string.edit_subject)
            populateFields(editingSubject!!)
        } else {
            supportActionBar?.title = getString(R.string.add_subject)
            updateColorPreview(selectedColor)
        }

        setupClickListeners()
        observeOperationState()
    }

    private fun populateFields(subject: SubjectEntity) {
        binding.etSubjectName.setText(subject.name)
        binding.etSubjectCode.setText(subject.code)
        binding.etProfessor.setText(subject.professor)
        binding.etCredits.setText(subject.credits.toString())
        binding.etRoomLocation.setText(subject.roomLocation)
        selectedColor = subject.color
        updateColorPreview(selectedColor)
    }

    private fun updateColorPreview(hex: String) {
        try {
            binding.viewColorPreview.setBackgroundColor(Color.parseColor(hex))
            binding.tvSelectedColor.text = hex
        } catch (_: Exception) {}
    }

    private fun setupClickListeners() {
        binding.btnPickColor.setOnClickListener { showColorPicker() }

        binding.btnSave.setOnClickListener { saveSubject() }

        binding.btnDelete.apply {
            visibility = if (editingSubject != null) View.VISIBLE else View.GONE
            setOnClickListener { confirmDelete() }
        }
    }

    private fun showColorPicker() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.subject_color))
            .apply {
                val grid = android.widget.GridLayout(this@SubjectDetailActivity).apply {
                    columnCount = 4
                    val pad = resources.getDimensionPixelSize(R.dimen.spacing_sm)
                    setPadding(pad, pad, pad, pad)
                }
                colorPalette.forEach { hex ->
                    val circle = View(this@SubjectDetailActivity).apply {
                        val size = resources.getDimensionPixelSize(R.dimen.icon_size_xl)
                        val params = android.widget.GridLayout.LayoutParams().apply {
                            width = size; height = size
                            setMargins(8, 8, 8, 8)
                        }
                        layoutParams = params
                        background = android.graphics.drawable.GradientDrawable().apply {
                            shape = android.graphics.drawable.GradientDrawable.OVAL
                            setColor(Color.parseColor(hex))
                        }
                        setOnClickListener {
                            selectedColor = hex
                            updateColorPreview(hex)
                        }
                    }
                    grid.addView(circle)
                }
                setView(grid)
            }
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    private fun saveSubject() {
        val name = binding.etSubjectName.text?.toString()?.trim() ?: ""
        if (name.isEmpty()) {
            binding.tilSubjectName.error = getString(R.string.error_empty_field)
            return
        }
        binding.tilSubjectName.error = null

        val credits = binding.etCredits.text?.toString()?.toIntOrNull() ?: 0
        val subject = (editingSubject ?: SubjectEntity(name = name)).copy(
            name = name,
            code = binding.etSubjectCode.text?.toString()?.trim() ?: "",
            professor = binding.etProfessor.text?.toString()?.trim() ?: "",
            credits = credits,
            roomLocation = binding.etRoomLocation.text?.toString()?.trim() ?: "",
            color = selectedColor,
            updatedAt = System.currentTimeMillis()
        )

        if (editingSubject != null) viewModel.updateSubject(subject)
        else viewModel.saveSubject(subject)
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                editingSubject?.let { viewModel.deleteSubject(it.id) }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeOperationState() {
        viewModel.operationState.observe(this) { state ->
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
