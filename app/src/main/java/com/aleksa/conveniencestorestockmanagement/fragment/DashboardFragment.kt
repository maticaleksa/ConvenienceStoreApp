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
import com.aleksa.conveniencestorestockmanagement.adapter.TransactionsAdapter
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
        val recentList = view.findViewById<RecyclerView>(R.id.dashboard_recent_list)
        val recentEmpty = view.findViewById<TextView>(R.id.dashboard_recent_empty)
        val adapter = ProductsAdapter()
        val recentAdapter = TransactionsAdapter()
        listView.layoutManager = LinearLayoutManager(requireContext())
        listView.adapter = adapter
        recentList.layoutManager = LinearLayoutManager(requireContext())
        recentList.adapter = recentAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.lowStock)
                    emptyView.visibility =
                        if (state.lowStock.isEmpty()) View.VISIBLE else View.GONE
                    recentAdapter.submitList(state.recentTransactions)
                    recentEmpty.visibility =
                        if (state.recentTransactions.isEmpty()) View.VISIBLE else View.GONE
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
