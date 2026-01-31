package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val usernameInput = view.findViewById<AppCompatEditText>(R.id.login_username)
        val passwordInput = view.findViewById<AppCompatEditText>(R.id.login_password)
        val loginButton = view.findViewById<AppCompatButton>(R.id.login_primary_button)
        loginButton.setOnClickListener {
            val username = usernameInput.text?.toString().orEmpty()
            val password = passwordInput.text?.toString().orEmpty()
            viewModel.onLogin(username, password)
        }

        val rootView = view
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (event is com.aleksa.conveniencestorestockmanagement.uistate.UiEvent.Message) {
                        val displayMessage = if (event.text == "No network connection") {
                            getString(R.string.no_internet)
                        } else {
                            event.text
                        }
                        Snackbar.make(rootView, displayMessage, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
