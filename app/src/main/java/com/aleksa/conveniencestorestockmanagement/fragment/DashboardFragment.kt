package com.aleksa.conveniencestorestockmanagement.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.viewmodel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private val viewModel: DashboardViewModel by viewModels()
}
