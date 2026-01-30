package com.aleksa.conveniencestorestockmanagement.fragment

import android.view.View
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.adapter.ProductsAdapter
import com.aleksa.conveniencestorestockmanagement.viewmodel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : BaseFragment(R.layout.fragment_dashboard) {
    private val viewModel: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<RecyclerView>(R.id.dashboard_low_stock_list)
        val emptyView = view.findViewById<TextView>(R.id.dashboard_low_stock_empty)
        val adapter = ProductsAdapter()
        listView.layoutManager = LinearLayoutManager(requireContext())
        listView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.lowStock)
                    emptyView.visibility =
                        if (state.lowStock.isEmpty()) View.VISIBLE else View.GONE
                    if (state.errorMessage != null) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            state.errorMessage,
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}
