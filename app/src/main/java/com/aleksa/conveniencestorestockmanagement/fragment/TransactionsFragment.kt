package com.aleksa.conveniencestorestockmanagement.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.viewmodel.TransactionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionsFragment : Fragment(R.layout.fragment_transactions) {
    private val viewModel: TransactionsViewModel by viewModels()
}
