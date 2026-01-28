package com.aleksa.conveniencestorestockmanagement.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.viewmodel.StockViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StockFragment : Fragment(R.layout.fragment_stock) {
    private val viewModel: StockViewModel by viewModels()
}
