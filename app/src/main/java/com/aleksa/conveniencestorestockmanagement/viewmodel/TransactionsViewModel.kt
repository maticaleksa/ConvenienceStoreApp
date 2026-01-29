package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.TransactionsUiState
import com.aleksa.domain.TransactionRepository
import com.aleksa.domain.model.Transaction
import com.aleksa.domain.model.TransactionType
import com.aleksa.domain.usecases.TransactionDateFilter
import com.aleksa.domain.usecases.TransactionFilterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    private val filterUseCase: TransactionFilterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private val selectedTypes = MutableStateFlow<Set<TransactionType>>(emptySet())
    private val dateFilter = MutableStateFlow(TransactionDateFilter.ALL)

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.observeAll(),
                selectedTypes,
                dateFilter,
            ) { items: List<Transaction>, types: Set<TransactionType>, date: TransactionDateFilter ->
                val filtered = filterUseCase(items, types, date)
                TransactionsUiState(
                    items = filtered,
                    selectedTypes = types,
                    dateFilter = date,
                    isEmpty = filtered.isEmpty(),
                )
            }.collectLatest { newState ->
                _uiState.value = newState
            }
        }
    }

    fun updateSelectedTypes(types: Set<TransactionType>) {
        selectedTypes.value = types
    }

    fun updateDateFilter(filter: TransactionDateFilter) {
        dateFilter.value = filter
    }
}
