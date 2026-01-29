package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.TransactionsUiState
import com.aleksa.domain.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        transactionRepository.observeAll()
            .onEach { items ->
                _uiState.value = TransactionsUiState(items = items, isEmpty = items.isEmpty())
            }
            .launchIn(viewModelScope)
    }
}
