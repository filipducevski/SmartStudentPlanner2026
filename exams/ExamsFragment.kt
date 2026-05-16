package com.smartstudent.planner.ui.exams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.smartstudent.planner.databinding.FragmentExamsBinding
import com.smartstudent.planner.util.UiState
import com.smartstudent.planner.viewmodel.ExamViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExamsFragment : Fragment() {

    private var _binding: FragmentExamsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExamViewModel by activityViewModels()
    private lateinit var adapter: ExamAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = com.smartstudent.planner.databinding.FragmentExamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabs()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = ExamAdapter(
            onExamClick = { exam ->
                viewModel.selectExam(exam)
                startActivity(
                    android.content.Intent(
                        requireContext(),
                        com.smartstudent.planner.ui.exams.ExamDetailActivity::class.java
                    )
                )
            }
        )
        binding.rvExams.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ExamsFragment.adapter
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> observeUpcoming()
                    1 -> observePast()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeUpcoming() {
        viewModel.upcomingExams.observe(viewLifecycleOwner) { exams ->
            adapter.submitList(exams)
            binding.emptyState.visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun observePast() {
        viewModel.pastExams.observe(viewLifecycleOwner) { exams ->
            adapter.submitList(exams)
            binding.emptyState.visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun observeViewModel() {
        observeUpcoming()
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> { viewModel.resetState() }
                is UiState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddExam.setOnClickListener {
            viewModel.selectExam(null)
            startActivity(
                android.content.Intent(
                    requireContext(),
                    com.smartstudent.planner.ui.exams.ExamDetailActivity::class.java
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
