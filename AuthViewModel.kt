package com.smartstudent.planner.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smartstudent.planner.data.repository.TaskRepository
import com.smartstudent.planner.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _authState = MutableLiveData<UiState<FirebaseUser>>()
    val authState: LiveData<UiState<FirebaseUser>> = _authState

    private val _currentUser = MutableLiveData<FirebaseUser?>(auth.currentUser)
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.postValue(firebaseAuth.currentUser)
        }
    }

    fun isLoggedIn() = auth.currentUser != null

    // ─── Email / Password ─────────────────────────────────────────────────────
    fun signInWithEmail(email: String, password: String) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    syncUserData(user.uid)
                    _authState.value = UiState.Success(user)
                } ?: run { _authState.value = UiState.Error("Authentication failed") }
            } catch (e: Exception) {
                Timber.e(e, "Email sign-in failed")
                _authState.value = UiState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName).build()
                    user.updateProfile(profileUpdates).await()
                    _authState.value = UiState.Success(user)
                } ?: run { _authState.value = UiState.Error("Registration failed") }
            } catch (e: Exception) {
                Timber.e(e, "Email sign-up failed")
                _authState.value = UiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
            } catch (e: Exception) {
                Timber.e(e, "Password reset failed")
            }
        }
    }

    // ─── Google Sign-In ───────────────────────────────────────────────────────
    fun handleGoogleSignInResult(data: Intent?) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()
                result.user?.let { user ->
                    syncUserData(user.uid)
                    _authState.value = UiState.Success(user)
                } ?: run { _authState.value = UiState.Error("Google sign-in failed") }
            } catch (e: Exception) {
                Timber.e(e, "Google sign-in failed")
                _authState.value = UiState.Error(e.message ?: "Google sign-in failed")
            }
        }
    }

    // ─── Facebook Sign-In ─────────────────────────────────────────────────────
    fun handleFacebookAccessToken(token: AccessToken) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val credential = FacebookAuthProvider.getCredential(token.token)
                val result = auth.signInWithCredential(credential).await()
                result.user?.let { user ->
                    syncUserData(user.uid)
                    _authState.value = UiState.Success(user)
                } ?: run { _authState.value = UiState.Error("Facebook sign-in failed") }
            } catch (e: Exception) {
                Timber.e(e, "Facebook sign-in failed")
                _authState.value = UiState.Error(e.message ?: "Facebook sign-in failed")
            }
        }
    }

    // ─── Anonymous Sign-In ────────────────────────────────────────────────────
    fun signInAnonymously() {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val result = auth.signInAnonymously().await()
                result.user?.let { user ->
                    _authState.value = UiState.Success(user)
                } ?: run { _authState.value = UiState.Error("Anonymous sign-in failed") }
            } catch (e: Exception) {
                Timber.e(e, "Anonymous sign-in failed")
                _authState.value = UiState.Error(e.message ?: "Anonymous sign-in failed")
            }
        }
    }

    // ─── Sign Out ─────────────────────────────────────────────────────────────
    fun signOut() {
        LoginManager.getInstance().logOut()
        auth.signOut()
        _currentUser.value = null
        _authState.value = UiState.Idle
    }

    private suspend fun syncUserData(userId: String) {
        try {
            taskRepository.fullSyncFromFirestore(userId)
        } catch (e: Exception) {
            Timber.w(e, "Initial sync failed, continuing offline")
        }
    }
}
