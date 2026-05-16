package com.smartstudent.planner.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.smartstudent.planner.R
import com.smartstudent.planner.databinding.FragmentTasksBinding
import com.smartstudent.planner.util.UiState
import com.smartstudent.planner.viewmodel.TaskFilter
import com.smartstudent.planner.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterChips()
        setupSearch()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onTaskClick = { task ->
                viewModel.selectTask(task)
                findNavController().navigate(R.id.action_tasksFragment_to_taskDetailFragment)
            },
            onTaskChecked = { task, checked ->
                viewModel.toggleTaskCompletion(task.id, checked)
            }
        )
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TasksFragment.adapter
            setHasFixedSize(true)
        }
        // Swipe to delete
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = adapter.getTaskAt(viewHolder.adapterPosition)
                viewModel.deleteTask(task.id)
                Snackbar.make(binding.root, R.string.task_deleted, Snackbar.LENGTH_SHORT).show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvTasks)
    }

    private fun setupFilterChips() {
        val chipMap = mapOf(
            binding.chipAll to TaskFilter.ALL,
            binding.chipToday to TaskFilter.TODAY,
            binding.chipWeek to TaskFilter.WEEK,
            binding.chipCompleted to TaskFilter.COMPLETED,
            binding.chipOverdue to TaskFilter.OVERDUE
        )
        chipMap.forEach { (chip, filter) ->
            chip.setOnClickListener {
                viewModel.setFilter(filter)
                updateChipSelection(chip)
            }
        }
    }

    private fun updateChipSelection(selectedChip: Chip) {
        listOf(
            binding.chipAll, binding.chipToday, binding.chipWeek,
            binding.chipCompleted, binding.chipOverdue
        ).forEach { chip ->
            chip.isChecked = chip == selectedChip
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter in adapter
                adapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            binding.emptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
            binding.rvTasks.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
            binding.tvTaskCount.text = "${tasks.size} задачи"
        }

        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    when (state.data) {
                        "task_completed" -> Snackbar.make(
                            binding.root, R.string.task_completed_msg, Snackbar.LENGTH_SHORT
                        ).show()
                        "sync_success" -> Snackbar.make(
                            binding.root, R.string.sync_success, Snackbar.LENGTH_SHORT
                        ).show()
                        "sync_failed" -> Snackbar.make(
                            binding.root, R.string.sync_failed, Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    viewModel.resetOperationState()
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetOperationState()
                }
                else -> binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.overdueCount.observe(viewLifecycleOwner) { count ->
            binding.chipOverdue.apply {
                text = if (count > 0) "${getString(R.string.filter_overdue)} ($count)" else getString(R.string.filter_overdue)
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            viewModel.selectTask(null)
            findNavController().navigate(R.id.action_tasksFragment_to_taskDetailFragment)
        }
        binding.btnSync.setOnClickListener {
            viewModel.syncTasks()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
