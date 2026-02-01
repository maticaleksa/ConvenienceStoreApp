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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.adapter.ProductsAdapter
import com.aleksa.conveniencestorestockmanagement.adapter.TransactionsAdapter
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.conveniencestorestockmanagement.viewmodel.DashboardViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : BaseFragment(R.layout.fragment_dashboard) {
    private val viewModel: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.nav_dashboard)
        val swipeRefresh =
            view.findViewById<SwipeRefreshLayout>(R.id.dashboard_swipe_refresh)
        val rootView = view
        val listView = view.findViewById<RecyclerView>(R.id.dashboard_low_stock_list)
        val emptyView = view.findViewById<TextView>(R.id.dashboard_low_stock_empty)
        val lowStockToggle = view.findViewById<TextView>(R.id.dashboard_low_stock_toggle)
        val recentList = view.findViewById<RecyclerView>(R.id.dashboard_recent_list)
        val recentEmpty = view.findViewById<TextView>(R.id.dashboard_recent_empty)
        val recentToggle = view.findViewById<TextView>(R.id.dashboard_recent_toggle)
        val adapter = ProductsAdapter(showEditIcon = false)
        val recentAdapter = TransactionsAdapter()
        listView.layoutManager = LinearLayoutManager(requireContext())
        listView.adapter = adapter
        recentList.layoutManager = LinearLayoutManager(requireContext())
        recentList.adapter = recentAdapter
        lowStockToggle.setOnClickListener { viewModel.toggleLowStockExpanded() }
        recentToggle.setOnClickListener { viewModel.toggleRecentExpanded() }
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val lowStockItems = if (state.lowStockExpanded) {
                        state.lowStock
                    } else {
                        state.lowStock.take(2)
                    }
                    adapter.submitList(lowStockItems)
                    emptyView.visibility =
                        if (state.lowStock.isEmpty()) View.VISIBLE else View.GONE
                    lowStockToggle.visibility =
                        if (state.lowStock.size > 2) View.VISIBLE else View.GONE
                    lowStockToggle.text = if (state.lowStockExpanded) {
                        getString(R.string.dashboard_show_less)
                    } else {
                        getString(R.string.dashboard_show_all)
                    }

                    val recentItems = if (state.recentExpanded) {
                        state.recentTransactions
                    } else {
                        state.recentTransactions.take(2)
                    }
                    recentAdapter.submitList(recentItems)
                    recentEmpty.visibility =
                        if (state.recentTransactions.isEmpty()) View.VISIBLE else View.GONE
                    recentToggle.visibility =
                        if (state.recentTransactions.size > 2) View.VISIBLE else View.GONE
                    recentToggle.text = if (state.recentExpanded) {
                        getString(R.string.dashboard_show_less)
                    } else {
                        getString(R.string.dashboard_show_all)
                    }
                    swipeRefresh.isRefreshing = state.isSyncing
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
