package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.domain.usecases.LowStockProductsUseCase
import com.aleksa.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val lowStockProductsUseCase: LowStockProductsUseCase,
) : ViewModel() {
    private val _lowStock = MutableStateFlow<List<Product>>(emptyList())
    val lowStock: StateFlow<List<Product>> = _lowStock.asStateFlow()

    init {
        lowStockProductsUseCase()
            .onEach { _lowStock.value = it }
            .launchIn(viewModelScope)
    }
}
