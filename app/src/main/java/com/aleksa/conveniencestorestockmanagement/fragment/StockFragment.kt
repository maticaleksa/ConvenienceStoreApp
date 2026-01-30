package com.aleksa.conveniencestorestockmanagement.fragment

import android.view.View
import androidx.fragment.app.Fragment
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.adapter.StockTabsAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StockFragment : Fragment(R.layout.fragment_stock) {
    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.stock_tabs)
        val viewPager = view.findViewById<ViewPager2>(R.id.stock_pager)

        viewPager.adapter = StockTabsAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) {
                getString(R.string.stock_tab_add)
            } else {
                getString(R.string.stock_tab_sale)
            }
        }.attach()
    }
}
