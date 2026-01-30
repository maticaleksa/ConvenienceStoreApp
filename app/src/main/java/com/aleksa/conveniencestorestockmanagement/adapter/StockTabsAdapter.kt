package com.aleksa.conveniencestorestockmanagement.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aleksa.conveniencestorestockmanagement.fragment.StockTransactionFragment
import com.aleksa.domain.model.TransactionType

class StockTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> StockTransactionFragment.newInstance(TransactionType.RESTOCK)
        1 -> StockTransactionFragment.newInstance(TransactionType.SALE)
        else -> throw IllegalArgumentException("Invalid position: $position")
    }
}
