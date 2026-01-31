package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.uistate.SupplierEditUiState
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.conveniencestorestockmanagement.viewmodel.SupplierEditViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SupplierEditFragment : BaseFragment(R.layout.fragment_supplier_edit) {
    private val viewModel: SupplierEditViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = getToolbar()
        val nameInput = view.findViewById<TextInputEditText>(R.id.supplier_edit_name)
        val contactInput = view.findViewById<TextInputEditText>(R.id.supplier_edit_contact)
        val phoneInput = view.findViewById<TextInputEditText>(R.id.supplier_edit_phone)
        val emailInput = view.findViewById<TextInputEditText>(R.id.supplier_edit_email)
        val addressInput = view.findViewById<TextInputEditText>(R.id.supplier_edit_address)
        val saveButton = view.findViewById<AppCompatButton>(R.id.supplier_edit_save_button)

        toolbar?.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar?.setNavigationOnClickListener { findNavController().popBackStack() }

        nameInput.doAfterTextChanged { viewModel.onNameChanged(it?.toString().orEmpty()) }
        contactInput.doAfterTextChanged { viewModel.onContactPersonChanged(it?.toString().orEmpty()) }
        phoneInput.doAfterTextChanged { viewModel.onPhoneChanged(it?.toString().orEmpty()) }
        emailInput.doAfterTextChanged { viewModel.onEmailChanged(it?.toString().orEmpty()) }
        addressInput.doAfterTextChanged { viewModel.onAddressChanged(it?.toString().orEmpty()) }
        saveButton.setOnClickListener { viewModel.onSaveClicked() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    toolbar?.setTitle(R.string.supplier_edit_title)
                    if (nameInput.text?.toString() != state.name) {
                        nameInput.setText(state.name)
                    }
                    if (contactInput.text?.toString() != state.contactPerson) {
                        contactInput.setText(state.contactPerson)
                    }
                    if (phoneInput.text?.toString() != state.phone) {
                        phoneInput.setText(state.phone)
                    }
                    if (emailInput.text?.toString() != state.email) {
                        emailInput.setText(state.email)
                    }
                    if (addressInput.text?.toString() != state.address) {
                        addressInput.setText(state.address)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (event is UiEvent.NavigateBack) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }
}
