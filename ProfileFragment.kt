package com.smartstudent.planner.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.smartstudent.planner.BuildConfig
import com.smartstudent.planner.R
import com.smartstudent.planner.databinding.FragmentProfileBinding
import com.smartstudent.planner.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateUserInfo()
        setupClickListeners()
        setupVersion()
    }

    private fun populateUserInfo() {
        val user = auth.currentUser
        if (user != null) {
            val name = if (user.isAnonymous) getString(R.string.anonymous_user)
                       else user.displayName ?: user.email ?: getString(R.string.anonymous_user)
            binding.tvDisplayName.text = name
            binding.tvEmail.text = if (user.isAnonymous) getString(R.string.anonymous_user)
                                   else user.email ?: "—"

            // Avatar initial
            binding.tvAvatarInitial.text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

            // Show link account option for anonymous users
            binding.cardLinkAccount.visibility = if (user.isAnonymous) View.VISIBLE else View.GONE

            // Provider icons
            val providers = user.providerData.map { it.providerId }
            binding.ivGoogle.visibility = if ("google.com" in providers) View.VISIBLE else View.GONE
            binding.ivFacebook.visibility = if ("facebook.com" in providers) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.cardLanguage.setOnClickListener { showLanguageDialog() }
        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            val mode = if (checked) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                       else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
        }
        binding.cardLinkAccount.setOnClickListener {
            Snackbar.make(binding.root, getString(R.string.link_account), Snackbar.LENGTH_SHORT).show()
        }
        binding.btnSignOut.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.confirm) { _, _ -> authViewModel.signOut() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(
            getString(R.string.language_english),
            getString(R.string.language_macedonian)
        )
        val localeCodes = arrayOf("en", "mk")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.change_language)
            .setItems(languages) { _, which ->
                val locale = Locale(localeCodes[which])
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)
                requireActivity().recreate()
            }
            .show()
    }

    private fun setupVersion() {
        binding.tvAppVersion.text = "${getString(R.string.app_version)}: ${BuildConfig.VERSION_NAME}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
