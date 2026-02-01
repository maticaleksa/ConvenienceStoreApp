package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.ProductEditUiState
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.domain.ProductRepository
import com.aleksa.domain.Money
import com.aleksa.domain.CategoryRepository
import com.aleksa.domain.model.Category
import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Supplier
import com.aleksa.domain.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID
import android.util.Log

@HiltViewModel
class ProductEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val supplierRepository: SupplierRepository,
) : ViewModel() {
    private companion object {
        private const val TAG = "ProductEditViewModel"
    }

    private val productId: String? = savedStateHandle["productId"]
    private val productName: String? = savedStateHandle["productName"]
    private val productDescription: String? = savedStateHandle["productDescription"]
    private val productPrice: String? = savedStateHandle["productPrice"]
    private val productBarcode: String? = savedStateHandle["productBarcode"]
    private val productCurrentStock: String? = savedStateHandle["productCurrentStock"]
    private val productMinimumStock: String? = savedStateHandle["productMinimumStock"]
    private val productCategoryId: String? = savedStateHandle["productCategoryId"]
    private val productCategoryName: String? = savedStateHandle["productCategoryName"]
    private val productSupplierId: String? = savedStateHandle["productSupplierId"]
    private val productSupplierName: String? = savedStateHandle["productSupplierName"]
    private val productSupplierContactPerson: String? =
        savedStateHandle["productSupplierContactPerson"]
    private val productSupplierPhone: String? = savedStateHandle["productSupplierPhone"]
    private val productSupplierEmail: String? = savedStateHandle["productSupplierEmail"]
    private val productSupplierAddress: String? = savedStateHandle["productSupplierAddress"]
    private val _uiState = MutableStateFlow(
        ProductEditUiState(
            mode = if (productId.isNullOrBlank()) {
                ProductEditUiState.Mode.ADD
            } else {
                ProductEditUiState.Mode.EDIT
            },
            productId = productId,
            name = productName.orEmpty(),
            description = productDescription.orEmpty(),
            price = productPrice.orEmpty(),
            barcode = productBarcode.orEmpty(),
            categoryId = productCategoryId,
            categoryName = productCategoryName.orEmpty(),
            supplierId = productSupplierId,
            supplierName = productSupplierName.orEmpty(),
            currentStockLevel = productCurrentStock.orEmpty(),
            minimumStockLevel = productMinimumStock.orEmpty()
        )
    )
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onPriceChanged(value: String) {
        _uiState.update { it.copy(price = value) }
    }

    fun onBarcodeChanged(value: String) {
        _uiState.update { it.copy(barcode = value) }
    }

    fun onCategorySelected(category: Category) {
        _uiState.update {
            it.copy(categoryId = category.id, categoryName = category.name)
        }
    }

    fun onSupplierSelected(supplier: Supplier) {
        _uiState.update {
            it.copy(supplierId = supplier.id, supplierName = supplier.name)
        }
    }

    fun onCurrentStockChanged(value: String) {
        _uiState.update { it.copy(currentStockLevel = value) }
    }

    fun onMinimumStockChanged(value: String) {
        _uiState.update { it.copy(minimumStockLevel = value) }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        Log.d(TAG, "onSaveClicked: id=${productId} name=${state.name}")
        val trimmedName = state.name.trim()
        if (trimmedName.isBlank()) {
            _events.tryEmit(UiEvent.Message("Product name is required."))
            return
        }
        val hasSupplier = !state.supplierId.isNullOrBlank() || state.supplierName.isNotBlank()
        if (!hasSupplier) {
            _events.tryEmit(UiEvent.Message("Supplier is required."))
            return
        }
        val priceValue = state.price.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0.0) {
            _events.tryEmit(UiEvent.Message("Price is required."))
            return
        }
        viewModelScope.launch {
            val resolvedCategory = Category(
                id = state.categoryId ?: productCategoryId ?: "uncategorized",
                name = state.categoryName.ifBlank { productCategoryName ?: "Uncategorized" },
            )
            val resolvedSupplier = Supplier(
                id = state.supplierId ?: productSupplierId ?: "unknown_supplier",
                name = state.supplierName.ifBlank { productSupplierName ?: "Unknown supplier" },
                contactPerson = productSupplierContactPerson ?: "",
                phone = productSupplierPhone ?: "",
                email = productSupplierEmail ?: "",
                address = productSupplierAddress ?: "",
            )
            val product = Product(
                id = productId ?: UUID.randomUUID().toString(),
                name = trimmedName,
                description = state.description.trim(),
                price = Money.ofDouble(priceValue),
                category = resolvedCategory,
                barcode = state.barcode.trim(),
                supplier = resolvedSupplier,
                currentStockLevel = state.currentStockLevel.toIntOrNull() ?: 0,
                minimumStockLevel = state.minimumStockLevel.toIntOrNull() ?: 0,
            )
            productRepository.upsert(product)
            _events.tryEmit(UiEvent.NavigateBack)
        }
    }

    init {
        categoryRepository.observeAll()
            .onEach { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
            .launchIn(viewModelScope)
        supplierRepository.observeAll()
            .onEach { suppliers ->
                _uiState.update { it.copy(suppliers = suppliers) }
            }
            .launchIn(viewModelScope)
    }
}
