package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.adapter.TransactionsAdapter
import com.aleksa.conveniencestorestockmanagement.viewmodel.TransactionsViewModel
import com.aleksa.domain.model.TransactionType
import com.aleksa.domain.usecases.TransactionDateFilter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsFragment : Fragment(R.layout.fragment_transactions) {
    private val viewModel: TransactionsViewModel by viewModels()
    private var selectedTypes: Set<TransactionType> = emptySet()
    private var dateFilter: TransactionDateFilter = TransactionDateFilter.ALL

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<RecyclerView>(R.id.transactions_list)
        val emptyView = view.findViewById<TextView>(R.id.transactions_empty)
        val filterButton = view.findViewById<AppCompatImageButton>(R.id.transactions_filter_button)
        val adapter = TransactionsAdapter()
        listView.layoutManager = LinearLayoutManager(requireContext())
        listView.adapter = adapter

        filterButton.setOnClickListener { showFilterDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.items)
                    selectedTypes = state.selectedTypes
                    dateFilter = state.dateFilter
                    emptyView.visibility =
                        if (state.isEmpty) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showTypeFilterDialog() {
        val types = listOf(TransactionType.RESTOCK, TransactionType.SALE)
        val items = types.map {
            if (it == TransactionType.RESTOCK) {
                getString(R.string.transaction_type_restock)
            } else {
                getString(R.string.transaction_type_sale)
            }
        }.toTypedArray()
        val checked = BooleanArray(types.size) { index ->
            types[index] in selectedTypes
        }
        val pending = selectedTypes.toMutableSet()
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.transactions_filter_title)
            .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                val type = types[which]
                if (isChecked) pending.add(type) else pending.remove(type)
            }
            .setPositiveButton(R.string.transactions_filter_apply) { _, _ ->
                viewModel.updateSelectedTypes(pending)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.transactions_filter_clear) { _, _ ->
                viewModel.updateSelectedTypes(emptySet())
            }
            .show()
    }

    private fun showFilterDialog() {
        val types = listOf(TransactionType.RESTOCK, TransactionType.SALE)
        val typeLabels = types.map {
            if (it == TransactionType.RESTOCK) {
                getString(R.string.transaction_type_restock)
            } else {
                getString(R.string.transaction_type_sale)
            }
        }.toTypedArray()
        val typeChecked = BooleanArray(types.size) { index ->
            types[index] in selectedTypes
        }
        val pendingTypes = selectedTypes.toMutableSet()

        val dateOptions = listOf(
            TransactionDateFilter.ALL,
            TransactionDateFilter.LAST_7_DAYS,
            TransactionDateFilter.LAST_30_DAYS
        )
        val dateLabels = arrayOf(
            getString(R.string.transactions_filter_date_all),
            getString(R.string.transactions_filter_date_7),
            getString(R.string.transactions_filter_date_30),
        )
        var pendingDate = dateFilter

        val content = layoutInflater.inflate(
            R.layout.dialog_transactions_filter,
            null,
            false
        )
        val typeList = content.findViewById<android.widget.ListView>(R.id.transaction_filter_types)
        val dateList = content.findViewById<android.widget.ListView>(R.id.transaction_filter_dates)

        typeList.adapter =
            android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, typeLabels)
        typeList.choiceMode = android.widget.ListView.CHOICE_MODE_MULTIPLE
        typeChecked.forEachIndexed { index, checked ->
            typeList.setItemChecked(index, checked)
        }
        typeList.setOnItemClickListener { _, _, which, _ ->
            val type = types[which]
            if (pendingTypes.contains(type)) pendingTypes.remove(type) else pendingTypes.add(type)
        }

        dateList.adapter =
            android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, dateLabels)
        dateList.choiceMode = android.widget.ListView.CHOICE_MODE_SINGLE
        dateList.setItemChecked(dateOptions.indexOf(pendingDate).coerceAtLeast(0), true)
        dateList.setOnItemClickListener { _, _, which, _ ->
            pendingDate = dateOptions[which]
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.transactions_filter_title)
            .setView(content)
            .setPositiveButton(R.string.transactions_filter_apply) { _, _ ->
                viewModel.updateSelectedTypes(pendingTypes)
                viewModel.updateDateFilter(pendingDate)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.transactions_filter_clear) { _, _ ->
                viewModel.updateSelectedTypes(emptySet())
                viewModel.updateDateFilter(TransactionDateFilter.ALL)
            }
            .show()
    }
}
