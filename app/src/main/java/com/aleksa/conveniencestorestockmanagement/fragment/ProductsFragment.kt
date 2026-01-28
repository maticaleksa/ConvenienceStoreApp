package com.aleksa.conveniencestorestockmanagement.fragment

import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.adapter.ProductsAdapter
import com.aleksa.conveniencestorestockmanagement.viewmodel.ProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductsFragment : Fragment(R.layout.fragment_products) {
    private val viewModel: ProductsViewModel by viewModels()

    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<RecyclerView>(R.id.products_list)
        val swipeRefresh =
            view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.products_swipe_refresh)
        val emptyView = view.findViewById<TextView>(R.id.products_empty)
        val searchInput =
            view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.products_search)
        val clearButton =
            view.findViewById<androidx.appcompat.widget.AppCompatImageButton>(R.id.products_clear_button)
        val adapter = ProductsAdapter()
        listView.layoutManager = LinearLayoutManager(requireContext())
        listView.adapter = adapter

        searchInput.doAfterTextChanged { text ->
            viewModel.onSearchQueryChanged(text?.toString().orEmpty())
        }
        clearButton.setOnClickListener {
            viewModel.clearSearch()
            searchInput.setText("")
        }
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
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
    }
}
