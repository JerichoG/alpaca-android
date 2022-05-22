package com.bangkit.alpaca.ui.auth.forgotpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.bangkit.alpaca.R
import com.bangkit.alpaca.data.remote.Result
import com.bangkit.alpaca.databinding.FragmentForgotPasswordBinding
import com.bangkit.alpaca.utils.isTouchableScreen
import com.bangkit.alpaca.utils.showError
import com.bangkit.alpaca.utils.showToastMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAction()
        sendPasswordResult()
    }

    private fun setupAction() {
        binding?.btnToLoginFromForgotPassword?.setOnClickListener(this)
        binding?.btnToRegistrationFromForgotPassword?.setOnClickListener(this)
        binding?.btnProcessNext?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_to_login_from_forgot_password -> v.findNavController()
                .navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
            R.id.btn_to_registration_from_forgot_password -> v.findNavController()
                .navigate(R.id.action_forgotPasswordFragment_to_registrationFragment)
            R.id.btn_process_next -> forgotPasswordHandler()
        }
    }

    private fun forgotPasswordHandler() {
        if (!isFormValid()) return

        val email = binding?.edtEmailForgotPassword?.text.toString()
        forgotPasswordViewModel.sendPassword(email)
    }

    private fun sendPasswordResult() {
        forgotPasswordViewModel.result.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Result.Loading -> loadingHandler(true)
                    is Result.Success -> {
                        loadingHandler(false)
                        if (result.data) {
                            getString(R.string.password_reset_link).showToastMessage(requireContext())
                            binding?.root?.findNavController()
                                ?.navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
                        }
                    }
                    is Result.Error -> {
                        loadingHandler(false)
                        result.error.showToastMessage(requireContext())
                    }
                }
            }
        }
    }

    private fun isFormValid(): Boolean {
        val email = binding?.edtEmailForgotPassword?.text.toString()
        val isEmailFormatValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

        binding?.tilEmailForgotPassword?.apply {
            if (email.isEmpty()) {
                isErrorEnabled = true
                error = getString(R.string.error_empty_email)
                showError(true, getString(R.string.error_empty_email))
            } else {
                if (!isEmailFormatValid) {
                    error = getString(R.string.error_email_format)
                    showError(true, getString(R.string.error_email_format))
                } else {
                    isErrorEnabled = false
                    error = null
                    showError(false)
                }
            }
        }
        return binding?.tilEmailForgotPassword?.isErrorEnabled == false
    }

    private fun loadingHandler(isLoading: Boolean) {
        if (isLoading) {
            binding?.loadingForgotPassword?.visibility = View.VISIBLE
        } else {
            binding?.loadingForgotPassword?.visibility = View.GONE
        }
        activity?.window?.isTouchableScreen(isLoading)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}