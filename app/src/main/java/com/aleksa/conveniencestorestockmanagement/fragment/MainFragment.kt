package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.domain.ProductRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : BaseFragment(R.layout.fragment_main) {

    @Inject
    lateinit var productRepository: ProductRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val greetingView = view.findViewById<TextView>(R.id.main_greeting)
        greetingView.text = getString(R.string.main_greeting_loading)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productRepository.observeAll().collectLatest { products ->
                    greetingView.text =
                        if (products.isEmpty()) {
                            "No products."
                        } else {
                            products.joinToString(separator = "\n\n") { product ->
                                "${product.name} • ${product.currentStockLevel} in stock\n${product.price.toDecimalString()}"
                            }
                        }
                }
            }
        }
    }
}
