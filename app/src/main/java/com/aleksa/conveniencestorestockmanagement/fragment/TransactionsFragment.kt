package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.adapter.TransactionsAdapter
import com.aleksa.conveniencestorestockmanagement.viewmodel.TransactionsViewModel
import com.aleksa.domain.model.TransactionType
import com.aleksa.domain.usecases.TransactionDateFilter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsFragment : BaseFragment(R.layout.fragment_transactions) {
    private val viewModel: TransactionsViewModel by viewModels()
    private var selectedTypes: Set<TransactionType> = emptySet()
    private var dateFilter: TransactionDateFilter = TransactionDateFilter.ALL

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.nav_transactions)
        val listView = view.findViewById<RecyclerView>(R.id.transactions_list)
        val swipeRefresh =
            view.findViewById<SwipeRefreshLayout>(R.id.transactions_swipe_refresh)
        val emptyView = view.findViewById<TextView>(R.id.transactions_empty)
        val filterButton = view.findViewById<AppCompatImageButton>(R.id.transactions_filter_button)
        val filterBadge = view.findViewById<View>(R.id.transactions_filter_badge)
        val adapter = TransactionsAdapter()
        listView.layoutManager = LinearLayoutManager(requireContext())
        listView.adapter = adapter

        filterButton.setOnClickListener { showFilterDialog() }
        swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.items)
                    selectedTypes = state.selectedTypes
                    dateFilter = state.dateFilter
                    filterBadge.visibility =
                        if (state.selectedTypes.isNotEmpty()
                            || state.dateFilter != TransactionDateFilter.ALL
                        ) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    emptyView.visibility =
                        if (state.isEmpty) View.VISIBLE else View.GONE
                    swipeRefresh.isRefreshing = state.isSyncing
                }
            }
        }

        parentFragmentManager.setFragmentResultListener(
            TransactionsFilterBottomSheetFragment.RESULT_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            val types = bundle
                .getStringArrayList(TransactionsFilterBottomSheetFragment.RESULT_SELECTED_TYPES)
                ?.mapNotNull { name -> TransactionType.values().find { it.name == name } }
                ?.toSet()
                ?: emptySet()
            val dateName =
                bundle.getString(TransactionsFilterBottomSheetFragment.RESULT_DATE_FILTER)
                    ?: TransactionDateFilter.ALL.name
            val date = TransactionDateFilter.values().find { it.name == dateName }
                ?: TransactionDateFilter.ALL
            viewModel.updateSelectedTypes(types)
            viewModel.updateDateFilter(date)
        }
    }

    private fun showFilterDialog() {
        TransactionsFilterBottomSheetFragment
            .newInstance(selectedTypes, dateFilter)
            .show(parentFragmentManager, "transactions_filter")
    }
}
