package com.smartstudent.planner.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.smartstudent.planner.R
import com.smartstudent.planner.databinding.FragmentDashboardBinding
import com.smartstudent.planner.ui.tasks.TaskAdapter
import com.smartstudent.planner.viewmodel.ExamViewModel
import com.smartstudent.planner.viewmodel.SubjectViewModel
import com.smartstudent.planner.viewmodel.TaskFilter
import com.smartstudent.planner.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by activityViewModels()
    private val subjectViewModel: SubjectViewModel by activityViewModels()
    private val examViewModel: ExamViewModel by activityViewModels()

    @Inject
    lateinit var auth: FirebaseAuth

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeader()
        setupRecyclerView()
        setupQuickStats()
        observeData()
        setupClickListeners()
    }

    private fun setupHeader() {
        val user = auth.currentUser
        val name = user?.displayName?.split(" ")?.firstOrNull() ?: "Student"
        binding.tvGreeting.text = getString(R.string.hello_user, name)

        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(Date())
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                taskViewModel.selectTask(task)
                findNavController().navigate(R.id.action_dashboardFragment_to_taskDetailFragment)
            },
            onTaskChecked = { task, checked ->
                taskViewModel.toggleTaskCompletion(task.id, checked)
            }
        )
        binding.rvTodayTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupQuickStats() {
        // Stats cards will be populated from LiveData
    }

    private fun observeData() {
        // Today's tasks
        taskViewModel.setFilter(TaskFilter.TODAY)
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks.take(5))
            binding.tvTodayTaskCount.text = if (tasks.isEmpty()) {
                getString(R.string.no_tasks_today)
            } else {
                getString(R.string.tasks_due_today, tasks.size)
            }
            binding.emptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }

        // Stats
        taskViewModel.pendingCount.observe(viewLifecycleOwner) { count ->
            binding.tvPendingCount.text = count.toString()
        }
        taskViewModel.completedCount.observe(viewLifecycleOwner) { count ->
            binding.tvCompletedCount.text = count.toString()
        }
        taskViewModel.overdueCount.observe(viewLifecycleOwner) { count ->
            binding.tvOverdueCount.text = count.toString()
            binding.cardOverdue.visibility = if (count > 0) View.VISIBLE else View.GONE
        }
        subjectViewModel.subjectCount.observe(viewLifecycleOwner) { count ->
            binding.tvSubjectCount.text = count.toString()
        }
        examViewModel.upcomingExamCount.observe(viewLifecycleOwner) { count ->
            binding.tvExamCount.text = count.toString()
        }

        // Upcoming exams
        examViewModel.upcomingExams.observe(viewLifecycleOwner) { exams ->
            val nextExam = exams.firstOrNull()
            if (nextExam != null) {
                binding.cardNextExam.visibility = View.VISIBLE
                binding.tvNextExamTitle.text = nextExam.title
                binding.tvNextExamSubject.text = nextExam.subjectName
                val days = nextExam.daysUntil()
                binding.tvNextExamCountdown.text = when {
                    days == 0L -> getString(R.string.exam_today)
                    days == 1L -> getString(R.string.exam_tomorrow)
                    else -> getString(R.string.exam_in_days, days.toInt())
                }
            } else {
                binding.cardNextExam.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.tvViewAllTasks.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_tasksFragment)
        }
        binding.fabAddTask.setOnClickListener {
            taskViewModel.selectTask(null)
            findNavController().navigate(R.id.action_dashboardFragment_to_taskDetailFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
