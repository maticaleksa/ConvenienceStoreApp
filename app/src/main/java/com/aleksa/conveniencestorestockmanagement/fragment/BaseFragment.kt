package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

open class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
    private val baseViewModel: BaseViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val label = requireActivity().findViewById<TextView>(R.id.no_internet_label)
            ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                baseViewModel.isOnline.collect { isOnline ->
                    label.visibility = if (isOnline) View.GONE else View.VISIBLE
                }
            }
        }
    }
}
