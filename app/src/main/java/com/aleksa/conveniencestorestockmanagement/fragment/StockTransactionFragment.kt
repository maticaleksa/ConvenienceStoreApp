package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.uistate.StockUiState
import com.aleksa.conveniencestorestockmanagement.viewmodel.BaseStockViewModel
import com.aleksa.conveniencestorestockmanagement.viewmodel.StockAddViewModel
import com.aleksa.conveniencestorestockmanagement.viewmodel.StockSaleViewModel
import com.aleksa.domain.model.TransactionType
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockTransactionFragment : BaseFragment(R.layout.fragment_stock_transaction) {

    companion object {
        private const val ARG_TRANSACTION_TYPE = "transaction_type"

        fun newInstance(type: TransactionType): StockTransactionFragment {
            return StockTransactionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TRANSACTION_TYPE, type)
                }
            }
        }
    }

    private val transactionType: TransactionType by lazy {
        arguments?.getSerializable(ARG_TRANSACTION_TYPE) as? TransactionType
            ?: TransactionType.RESTOCK
    }

    private val viewModel: BaseStockViewModel<out StockUiState<*>> by lazy {
        val provider = ViewModelProvider(this, defaultViewModelProviderFactory)
        if (transactionType == TransactionType.RESTOCK) {
            provider[StockAddViewModel::class.java]
        } else {
            provider[StockSaleViewModel::class.java]
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productInput = view.findViewById<AppCompatAutoCompleteTextView>(
            R.id.stock_transaction_product
        )
        val currentValue = view.findViewById<TextView>(
            R.id.stock_transaction_current_value
        )
        val quantityInput = view.findViewById<TextInputEditText>(
            R.id.stock_transaction_quantity
        )
        val quantityContainer = view.findViewById<TextInputLayout>(
            R.id.stock_transaction_quantity_container
        )
        val decrementButton = view.findViewById<AppCompatImageButton>(
            R.id.stock_transaction_decrement
        )
        val incrementButton = view.findViewById<AppCompatImageButton>(
            R.id.stock_transaction_increment
        )
        val notesInput = view.findViewById<TextInputEditText>(
            R.id.stock_transaction_notes
        )
        val saveButton = view.findViewById<AppCompatButton>(
            R.id.stock_transaction_save_button
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )

        quantityContainer.hint = if (transactionType == TransactionType.RESTOCK) {
            getString(R.string.stock_add_quantity)
        } else {
            getString(R.string.stock_sale_quantity)
        }

        productInput.setAdapter(adapter)
        productInput.threshold = 0

        productInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.onSearchQueryChanged(productInput.text?.toString().orEmpty())
                productInput.showDropDown()
            }
        }

        productInput.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            viewModel.onSearchQueryChanged(value)
            viewModel.onProductInputChanged(value)
        }

        productInput.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position).orEmpty()
            viewModel.onProductSelected(selected)
        }

        quantityInput.doAfterTextChanged { text ->
            viewModel.onQuantityChanged(text?.toString().orEmpty())
        }

        notesInput.doAfterTextChanged { text ->
            viewModel.onNotesChanged(text?.toString().orEmpty())
        }

        decrementButton.setOnClickListener { viewModel.decrementQuantity() }
        incrementButton.setOnClickListener { viewModel.incrementQuantity() }
        saveButton.setOnClickListener { viewModel.save() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->

                    adapter.clear()
                    adapter.addAll(uiState.products.map { it.name })
                    adapter.notifyDataSetChanged()

                    val currentStockText = uiState.currentStock?.toString()
                        ?: getString(R.string.stock_quantity_placeholder)
                    if (currentValue.text?.toString() != currentStockText) {
                        currentValue.text = currentStockText
                    }

                    val quantityText = uiState.quantity.toString()
                    if (quantityInput.text?.toString() != quantityText) {
                        quantityInput.setText(quantityText)
                        quantityInput.setSelection(quantityText.length)
                    }

                    if (notesInput.text?.toString() != uiState.notes) {
                        notesInput.setText(uiState.notes)
                        notesInput.setSelection(uiState.notes.length)
                    }

                    decrementButton.isEnabled = uiState.quantity > 0

                    incrementButton.isEnabled = when (transactionType) {
                        TransactionType.RESTOCK -> uiState.selectedProductId != null
                        TransactionType.SALE -> {
                            val maxStock = uiState.currentStock ?: 0
                            uiState.selectedProductId != null && uiState.quantity < maxStock
                        }
                    }

                    saveButton.isEnabled = uiState.isQuantityValid
                }
            }
        }

        val rootView = view
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (event is com.aleksa.conveniencestorestockmanagement.uistate.UiEvent.Message) {
                        Snackbar.make(rootView, event.text, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
