package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
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
import com.aleksa.conveniencestorestockmanagement.adapter.SuppliersAdapter
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.conveniencestorestockmanagement.viewmodel.SuppliersViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SuppliersFragment : BaseFragment(R.layout.fragment_suppliers) {
    private val viewModel: SuppliersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<RecyclerView>(R.id.suppliers_list)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.suppliers_swipe_refresh)
        val emptyView = view.findViewById<TextView>(R.id.suppliers_empty)
        val searchInput = view.findViewById<TextInputEditText>(R.id.suppliers_search)
        val clearButton = view.findViewById<AppCompatImageButton>(R.id.suppliers_clear_button)
        val adapter = SuppliersAdapter { supplier ->
            val args = Bundle().apply {
                putString("supplierId", supplier.id)
                putString("supplierName", supplier.name)
                putString("supplierContactPerson", supplier.contactPerson)
                putString("supplierPhone", supplier.phone)
                putString("supplierEmail", supplier.email)
                putString("supplierAddress", supplier.address)
            }
            findNavController().navigate(R.id.supplierEditFragment, args)
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
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        val rootView = view
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.items)
                    if (searchInput.text?.toString() != state.searchQuery) {
                        searchInput.setText(state.searchQuery)
                        searchInput.setSelection(state.searchQuery.length)
                    }
                    emptyView.text = if (state.isSearchActive) {
                        getString(R.string.empty_suppliers_search, state.searchQuery)
                    } else {
                        getString(R.string.empty_suppliers)
                    }
                    swipeRefresh.isRefreshing = state.isSyncing
                    emptyView.visibility =
                        if (state.items.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (event is UiEvent.Message) {
                        Snackbar.make(rootView, event.text, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
