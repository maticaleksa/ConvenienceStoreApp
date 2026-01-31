package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.uistate.ProductEditUiState
import com.aleksa.conveniencestorestockmanagement.viewmodel.ProductEditViewModel
import com.aleksa.domain.model.Category
import com.aleksa.domain.model.Supplier
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.util.Log

@AndroidEntryPoint
class ProductEditFragment : BaseFragment(R.layout.fragment_product_edit) {
    private companion object {
        private const val TAG = "ProductEditFragment"
    }
    private var categories: List<Category> = emptyList()
    private var suppliers: List<Supplier> = emptyList()
    private val viewModel: ProductEditViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.product_edit_toolbar)
        val nameInput = view.findViewById<TextInputEditText>(R.id.product_edit_name)
        val descriptionInput =
            view.findViewById<TextInputEditText>(R.id.product_edit_description)
        val priceInput = view.findViewById<TextInputEditText>(R.id.product_edit_price)
        val barcodeInput = view.findViewById<TextInputEditText>(R.id.product_edit_barcode)
        val categoryInput = view.findViewById<TextInputEditText>(R.id.product_edit_category)
        val supplierInput = view.findViewById<TextInputEditText>(R.id.product_edit_supplier)
        val currentStockInput =
            view.findViewById<TextInputEditText>(R.id.product_edit_current_stock)
        val minimumStockInput =
            view.findViewById<TextInputEditText>(R.id.product_edit_minimum_stock)
        val saveButton =
            view.findViewById<AppCompatButton>(R.id.product_edit_save_button)

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        nameInput.doAfterTextChanged { viewModel.onNameChanged(it?.toString().orEmpty()) }
        descriptionInput.doAfterTextChanged {
            viewModel.onDescriptionChanged(it?.toString().orEmpty())
        }
        priceInput.doAfterTextChanged { viewModel.onPriceChanged(it?.toString().orEmpty()) }
        barcodeInput.doAfterTextChanged { viewModel.onBarcodeChanged(it?.toString().orEmpty()) }
        categoryInput.isFocusable = false
        categoryInput.isClickable = true
        categoryInput.setOnClickListener { showCategoryDialog() }
        supplierInput.isFocusable = false
        supplierInput.isClickable = true
        supplierInput.setOnClickListener { showSupplierDialog() }
        currentStockInput.doAfterTextChanged {
            viewModel.onCurrentStockChanged(it?.toString().orEmpty())
        }
        minimumStockInput.doAfterTextChanged {
            viewModel.onMinimumStockChanged(it?.toString().orEmpty())
        }
        saveButton.setOnClickListener {
            Log.d(TAG, "save clicked")
            viewModel.onSaveClicked()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    categories = state.categories
                    suppliers = state.suppliers
                    toolbar.setTitle(
                        if (state.mode == ProductEditUiState.Mode.ADD) {
                            R.string.product_add_title
                        } else {
                            R.string.product_edit_title
                        }
                    )
                    if (nameInput.text?.toString() != state.name) {
                        nameInput.setText(state.name)
                    }
                    if (descriptionInput.text?.toString() != state.description) {
                        descriptionInput.setText(state.description)
                    }
                    if (priceInput.text?.toString() != state.price) {
                        priceInput.setText(state.price)
                    }
                    if (barcodeInput.text?.toString() != state.barcode) {
                        barcodeInput.setText(state.barcode)
                    }
                    if (categoryInput.text?.toString() != state.categoryName) {
                        categoryInput.setText(state.categoryName)
                    }
                    if (supplierInput.text?.toString() != state.supplierName) {
                        supplierInput.setText(state.supplierName)
                    }
                    if (currentStockInput.text?.toString() != state.currentStockLevel) {
                        currentStockInput.setText(state.currentStockLevel)
                    }
                    if (minimumStockInput.text?.toString() != state.minimumStockLevel) {
                        minimumStockInput.setText(state.minimumStockLevel)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (event is com.aleksa.conveniencestorestockmanagement.uistate.UiEvent.NavigateBack) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun showCategoryDialog() {
        if (categories.isEmpty()) return
        val items = categories.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.product_category_select_title)
            .setItems(items) { _, which ->
                viewModel.onCategorySelected(categories[which])
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showSupplierDialog() {
        if (suppliers.isEmpty()) return
        val items = suppliers.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.product_supplier_select_title)
            .setItems(items) { _, which ->
                viewModel.onSupplierSelected(suppliers[which])
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
