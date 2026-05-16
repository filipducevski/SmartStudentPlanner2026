package com.smartstudent.planner.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.smartstudent.planner.ui.dashboard.MainActivity
import com.smartstudent.planner.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash while checking auth
        splashScreen.setKeepOnScreenCondition { true }

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN) {}

        lifecycleScope.launch {
            delay(500) // Brief delay for splash animation
            navigateToAppropriateScreen()
        }
    }

    private fun navigateToAppropriateScreen() {
        val destination = if (authViewModel.isLoggedIn()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AuthActivity::class.java)
        }
        startActivity(destination)
        finish()
    }
}
