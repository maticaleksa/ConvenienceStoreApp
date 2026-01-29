package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.adapter.TransactionsAdapter
import com.aleksa.conveniencestorestockmanagement.viewmodel.TransactionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsFragment : Fragment(R.layout.fragment_transactions) {
    private val viewModel: TransactionsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<RecyclerView>(R.id.transactions_list)
        val emptyView = view.findViewById<TextView>(R.id.transactions_empty)
        val adapter = TransactionsAdapter()
        listView.layoutManager = LinearLayoutManager(requireContext())
        listView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.items)
                    emptyView.visibility =
                        if (state.isEmpty) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
