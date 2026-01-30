package com.aleksa.conveniencestorestockmanagement.fragment

import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.adapter.ProductsAdapter
import com.aleksa.conveniencestorestockmanagement.viewmodel.ProductsViewModel
import com.aleksa.domain.model.Category
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductsFragment : BaseFragment(R.layout.fragment_products) {
    private val viewModel: ProductsViewModel by viewModels()
    private var currentCategories: List<Category> = emptyList()
    private var selectedCategoryIds: Set<String> = emptySet()

    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<RecyclerView>(R.id.products_list)
        val swipeRefresh =
            view.findViewById<SwipeRefreshLayout>(R.id.products_swipe_refresh)
        val emptyView = view.findViewById<TextView>(R.id.products_empty)
        val searchInput =
            view.findViewById<TextInputEditText>(R.id.products_search)
        val clearButton =
            view.findViewById<AppCompatImageButton>(R.id.products_clear_button)
        val filterButton =
            view.findViewById<AppCompatImageButton>(R.id.products_filter_button)
        val addFab =
            view.findViewById<FloatingActionButton>(R.id.products_add_fab)
        val adapter = ProductsAdapter { product ->
            val args = android.os.Bundle().apply {
                putString("productId", product.id)
                putString("productName", product.name)
                putString("productDescription", product.description)
                putString("productPrice", product.price.toDecimalString())
                putString("productBarcode", product.barcode)
                putString("productCurrentStock", product.currentStockLevel.toString())
                putString("productMinimumStock", product.minimumStockLevel.toString())
                putString("productCategoryId", product.category.id)
                putString("productCategoryName", product.category.name)
                putString("productSupplierId", product.supplier.id)
                putString("productSupplierName", product.supplier.name)
                putString("productSupplierContactPerson", product.supplier.contactPerson)
                putString("productSupplierPhone", product.supplier.phone)
                putString("productSupplierEmail", product.supplier.email)
                putString("productSupplierAddress", product.supplier.address)
            }
            findNavController().navigate(R.id.productEditFragment, args)
        }
        listView.layoutManager = LinearLayoutManager(requireContext())
        listView.adapter = adapter

        searchInput.doAfterTextChanged { text ->
            viewModel.onSearchQueryChanged(text?.toString().orEmpty())
        }
        clearButton.setOnClickListener {
            viewModel.clearSearch()
            searchInput.setText("")
        }
        filterButton.setOnClickListener {
            showCategoryFilterDialog()
        }
        addFab.setOnClickListener {
            viewModel.onAddProductClicked()
        }
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    currentCategories = state.categories
                    selectedCategoryIds = state.selectedCategoryIds
                    adapter.submitList(state.items)
                    if (searchInput.text?.toString() != state.searchQuery) {
                        searchInput.setText(state.searchQuery)
                        searchInput.setSelection(state.searchQuery.length)
                    }
                    emptyView.text = if (state.isSearchActive) {
                        getString(R.string.empty_products_search, state.searchQuery)
                    } else {
                        getString(R.string.empty_products)
                    }
                    swipeRefresh.isRefreshing = state.isSyncing
                    emptyView.visibility =
                        if (!state.isLoading && state.items.isEmpty()) {
                            android.view.View.VISIBLE
                        } else {
                            android.view.View.GONE
                        }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (event is com.aleksa.conveniencestorestockmanagement.uistate.UiEvent.Message) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            event.text,
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addProductEvents.collect {
                    findNavController().navigate(R.id.productEditFragment)
                }
            }
        }
    }

    private fun showCategoryFilterDialog() {
        if (currentCategories.isEmpty()) return
        val items = currentCategories.map { it.name }.toTypedArray()
        val checked = BooleanArray(currentCategories.size) { index ->
            currentCategories[index].id in selectedCategoryIds
        }
        val pending = selectedCategoryIds.toMutableSet()
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.products_filter_categories_title)
            .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                val id = currentCategories[which].id
                if (isChecked) pending.add(id) else pending.remove(id)
            }
            .setPositiveButton(R.string.products_filter_apply) { _, _ ->
                viewModel.updateSelectedCategories(pending)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.products_filter_clear) { _, _ ->
                viewModel.updateSelectedCategories(emptySet())
            }
            .show()
    }
}
