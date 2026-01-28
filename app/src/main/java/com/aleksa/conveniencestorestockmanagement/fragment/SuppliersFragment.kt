package com.aleksa.conveniencestorestockmanagement.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.viewmodel.SuppliersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuppliersFragment : Fragment(R.layout.fragment_suppliers) {
    private val viewModel: SuppliersViewModel by viewModels()
}
