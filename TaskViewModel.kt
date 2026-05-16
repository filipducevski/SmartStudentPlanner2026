package com.smartstudent.planner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.smartstudent.planner.data.local.entities.TaskEntity
import com.smartstudent.planner.data.repository.TaskRepository
import com.smartstudent.planner.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

enum class TaskFilter { ALL, TODAY, WEEK, COMPLETED, OVERDUE }

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val auth: FirebaseAuth,
    private val analytics: FirebaseAnalytics
) : ViewModel() {

    private val userId get() = auth.currentUser?.uid ?: ""

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter get() = _filter.value

    val tasks = _filter.flatMapLatest { filter ->
        when (filter) {
            TaskFilter.ALL -> repository.getAllTasks(userId)
            TaskFilter.TODAY -> repository.getTodayTasks(userId)
            TaskFilter.WEEK -> repository.getWeekTasks(userId)
            TaskFilter.COMPLETED -> repository.getCompletedTasks(userId)
            TaskFilter.OVERDUE -> repository.getOverdueTasks(userId)
        }
    }.asLiveData()

    val pendingCount = repository.getPendingTaskCount(userId).asLiveData()
    val completedCount = repository.getCompletedTaskCount(userId).asLiveData()
    val overdueCount = repository.getOverdueTaskCount(userId).asLiveData()

    private val _operationState = MutableLiveData<UiState<String>>()
    val operationState: LiveData<UiState<String>> = _operationState

    private val _selectedTask = MutableLiveData<TaskEntity?>()
    val selectedTask: LiveData<TaskEntity?> = _selectedTask

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun selectTask(task: TaskEntity?) {
        _selectedTask.value = task
    }

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                _operationState.value = UiState.Loading
                val taskWithUser = task.copy(userId = userId)
                repository.saveTask(taskWithUser)
                analytics.logEvent("task_created") {
                    param("task_type", task.taskType)
                    param("priority", task.priority.toLong())
                    param(FirebaseAnalytics.Param.ITEM_NAME, task.title)
                }
                _operationState.value = UiState.Success("task_saved")
            } catch (e: Exception) {
                Timber.e(e, "Failed to save task")
                _operationState.value = UiState.Error(e.message ?: "Failed to save task")
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                repository.updateTask(task)
                analytics.logEvent("task_updated") {
                    param(FirebaseAnalytics.Param.ITEM_ID, task.id)
                }
                _operationState.value = UiState.Success("task_updated")
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Failed to update task")
            }
        }
    }

    fun toggleTaskCompletion(taskId: String, completed: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleTaskCompletion(taskId, completed)
                if (completed) {
                    analytics.logEvent("task_completed") {
                        param(FirebaseAnalytics.Param.ITEM_ID, taskId)
                    }
                    _operationState.value = UiState.Success("task_completed")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle task completion")
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId)
                analytics.logEvent("task_deleted") {
                    param(FirebaseAnalytics.Param.ITEM_ID, taskId)
                }
                _operationState.value = UiState.Success("task_deleted")
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Failed to delete task")
            }
        }
    }

    fun syncTasks() {
        viewModelScope.launch {
            try {
                _operationState.value = UiState.Loading
                repository.syncAllPendingTasks(userId)
                analytics.logEvent("sync_triggered") {
                    param("source", "manual")
                }
                _operationState.value = UiState.Success("sync_success")
            } catch (e: Exception) {
                _operationState.value = UiState.Error("sync_failed")
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = UiState.Idle
    }
}
