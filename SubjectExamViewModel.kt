package com.smartstudent.planner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.smartstudent.planner.data.local.entities.ExamEntity
import com.smartstudent.planner.data.local.entities.SubjectEntity
import com.smartstudent.planner.data.repository.TaskRepository
import com.smartstudent.planner.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val auth: FirebaseAuth,
    private val analytics: FirebaseAnalytics
) : ViewModel() {

    private val userId get() = auth.currentUser?.uid ?: ""

    val subjects = repository.getAllSubjects(userId).asLiveData()
    val subjectCount = repository.getActiveSubjectCount(userId).asLiveData()

    private val _operationState = MutableLiveData<UiState<String>>(UiState.Idle)
    val operationState: LiveData<UiState<String>> = _operationState

    private val _selectedSubject = MutableLiveData<SubjectEntity?>()
    val selectedSubject: LiveData<SubjectEntity?> = _selectedSubject

    fun selectSubject(subject: SubjectEntity?) {
        _selectedSubject.value = subject
    }

    fun saveSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            try {
                _operationState.value = UiState.Loading
                val subjectWithUser = subject.copy(userId = userId)
                repository.saveSubject(subjectWithUser)
                analytics.logEvent("subject_created") {
                    param(FirebaseAnalytics.Param.ITEM_NAME, subject.name)
                }
                _operationState.value = UiState.Success("subject_saved")
            } catch (e: Exception) {
                Timber.e(e)
                _operationState.value = UiState.Error(e.message ?: "Failed to save subject")
            }
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            try {
                repository.updateSubject(subject)
                _operationState.value = UiState.Success("subject_updated")
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Failed to update subject")
            }
        }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSubject(subjectId, userId)
                analytics.logEvent("subject_deleted") {
                    param(FirebaseAnalytics.Param.ITEM_ID, subjectId)
                }
                _operationState.value = UiState.Success("subject_deleted")
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Failed to delete subject")
            }
        }
    }

    suspend fun getSubjectById(subjectId: String) = repository.getSubjectById(subjectId)

    fun getTasksForSubject(subjectId: String) = repository.getTasksForSubject(subjectId).asLiveData()
    fun getExamsForSubject(subjectId: String) = repository.getExamsForSubject(subjectId).asLiveData()

    fun resetState() { _operationState.value = UiState.Idle }
}

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val auth: FirebaseAuth,
    private val analytics: FirebaseAnalytics
) : ViewModel() {

    private val userId get() = auth.currentUser?.uid ?: ""

    val upcomingExams = repository.getUpcomingExams(userId).asLiveData()
    val pastExams = repository.getPastExams(userId).asLiveData()
    val upcomingExamCount = repository.getUpcomingExamCount(userId).asLiveData()

    private val _operationState = MutableLiveData<UiState<String>>(UiState.Idle)
    val operationState: LiveData<UiState<String>> = _operationState

    private val _selectedExam = MutableLiveData<ExamEntity?>()
    val selectedExam: LiveData<ExamEntity?> = _selectedExam

    fun selectExam(exam: ExamEntity?) { _selectedExam.value = exam }

    fun saveExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                _operationState.value = UiState.Loading
                val examWithUser = exam.copy(userId = userId)
                repository.saveExam(examWithUser)
                analytics.logEvent("exam_created") {
                    param(FirebaseAnalytics.Param.ITEM_NAME, exam.title)
                    param("subject", exam.subjectName)
                }
                _operationState.value = UiState.Success("exam_saved")
            } catch (e: Exception) {
                Timber.e(e)
                _operationState.value = UiState.Error(e.message ?: "Failed to save exam")
            }
        }
    }

    fun updateExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                repository.updateExam(exam)
                _operationState.value = UiState.Success("exam_updated")
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Failed to update exam")
            }
        }
    }

    fun deleteExam(examId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExam(examId, userId)
                analytics.logEvent("exam_deleted") {
                    param(FirebaseAnalytics.Param.ITEM_ID, examId)
                }
                _operationState.value = UiState.Success("exam_deleted")
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Failed to delete exam")
            }
        }
    }

    fun resetState() { _operationState.value = UiState.Idle }
}
