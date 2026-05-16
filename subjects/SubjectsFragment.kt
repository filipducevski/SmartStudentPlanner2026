package com.smartstudent.planner.ui.subjects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.smartstudent.planner.R
import com.smartstudent.planner.databinding.FragmentSubjectsBinding
import com.smartstudent.planner.util.UiState
import com.smartstudent.planner.viewmodel.SubjectViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubjectsFragment : Fragment() {

    private var _binding: FragmentSubjectsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubjectViewModel by activityViewModels()
    private lateinit var adapter: SubjectAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = SubjectAdapter(
            onSubjectClick = { subject ->
                viewModel.selectSubject(subject)
                val intent = android.content.Intent(requireContext(),
                    com.smartstudent.planner.ui.subjects.SubjectDetailActivity::class.java)
                startActivity(intent)
            }
        )
        // Use grid for tablets, list for phones
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        binding.rvSubjects.apply {
            layoutManager = if (isTablet) GridLayoutManager(requireContext(), 2)
                           else LinearLayoutManager(requireContext())
            adapter = this@SubjectsFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            adapter.submitList(subjects)
            binding.emptyState.visibility = if (subjects.isEmpty()) View.VISIBLE else View.GONE
            binding.rvSubjects.visibility = if (subjects.isEmpty()) View.GONE else View.VISIBLE
            binding.tvSubjectCount.text = getString(R.string.subjects_count) + ": ${subjects.size}"
        }

        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val msg = when (state.data) {
                        "subject_saved" -> getString(R.string.subject_saved)
                        "subject_deleted" -> getString(R.string.delete_subject)
                        else -> state.data
                    }
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
                is UiState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddSubject.setOnClickListener {
            viewModel.selectSubject(null)
            val intent = android.content.Intent(requireContext(),
                com.smartstudent.planner.ui.subjects.SubjectDetailActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
