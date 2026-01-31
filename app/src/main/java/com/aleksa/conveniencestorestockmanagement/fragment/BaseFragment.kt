package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.view.updatePadding
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.conveniencestorestockmanagement.viewmodel.BaseViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

open class BaseFragment(@LayoutRes private val layoutRes: Int) : Fragment() {
    private val baseViewModel: BaseViewModel by activityViewModels()
    private var toolbar: MaterialToolbar? = null
    private var appBar: AppBarLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_base, container, false)
        val contentContainer = root.findViewById<ViewGroup>(R.id.base_content_container)
        inflater.inflate(layoutRes, contentContainer, true)
        toolbar = root.findViewById(R.id.base_toolbar)
        appBar = root.findViewById(R.id.base_app_bar)
        applyStatusBarInsets()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activityLabel = requireActivity().findViewById<TextView>(R.id.no_internet_label)
        val fragmentLabel = view.findViewById<TextView>(R.id.no_internet_label)
        if (activityLabel == null && fragmentLabel == null) {
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                baseViewModel.uiState.collect { state ->
                    val isOnline = state.isOnline
                    val visibility = if (isOnline) View.GONE else View.VISIBLE
                    if (fragmentLabel != null) {
                        fragmentLabel.visibility = visibility
                        activityLabel?.visibility = View.GONE
                    } else {
                        activityLabel?.visibility = visibility
                    }
                }
            }
        }

    }

    protected fun setToolbarTitle(titleRes: Int) {
        toolbar?.setTitle(titleRes)
    }

    protected fun setToolbarTitle(title: CharSequence) {
        toolbar?.title = title
    }

    protected fun setToolbarVisible(visible: Boolean) {
        appBar?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    protected fun getToolbar(): MaterialToolbar? = toolbar

    private fun applyStatusBarInsets() {
        val target = appBar ?: return
        ViewCompat.setOnApplyWindowInsetsListener(target) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBars.top)
            insets
        }
    }
}
