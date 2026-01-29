package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.SupplierEditUiState
import com.aleksa.domain.SupplierRepository
import com.aleksa.domain.model.Supplier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val supplierRepository: SupplierRepository,
) : ViewModel() {

    private val supplierId: String = requireNotNull(savedStateHandle["supplierId"])
    private val supplierName: String = savedStateHandle["supplierName"] ?: ""
    private val supplierContactPerson: String = savedStateHandle["supplierContactPerson"] ?: ""
    private val supplierPhone: String = savedStateHandle["supplierPhone"] ?: ""
    private val supplierEmail: String = savedStateHandle["supplierEmail"] ?: ""
    private val supplierAddress: String = savedStateHandle["supplierAddress"] ?: ""

    private val _uiState = MutableStateFlow(
        SupplierEditUiState(
            supplierId = supplierId,
            name = supplierName,
            contactPerson = supplierContactPerson,
            phone = supplierPhone,
            email = supplierEmail,
            address = supplierAddress,
        )
    )
    val uiState: StateFlow<SupplierEditUiState> = _uiState.asStateFlow()

    private val _saveEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saveEvents = _saveEvents.asSharedFlow()

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onContactPersonChanged(value: String) {
        _uiState.update { it.copy(contactPerson = value) }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phone = value) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onAddressChanged(value: String) {
        _uiState.update { it.copy(address = value) }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        viewModelScope.launch {
            val supplier = Supplier(
                id = supplierId,
                name = state.name.trim(),
                contactPerson = state.contactPerson.trim(),
                phone = state.phone.trim(),
                email = state.email.trim(),
                address = state.address.trim(),
            )
            supplierRepository.upsert(supplier)
            _saveEvents.tryEmit(Unit)
        }
    }
}
