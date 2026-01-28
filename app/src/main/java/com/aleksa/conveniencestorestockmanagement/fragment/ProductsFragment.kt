package com.aleksa.conveniencestorestockmanagement.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.viewmodel.ProductsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductsFragment : Fragment(R.layout.fragment_products) {
    private val viewModel: ProductsViewModel by viewModels()
}
