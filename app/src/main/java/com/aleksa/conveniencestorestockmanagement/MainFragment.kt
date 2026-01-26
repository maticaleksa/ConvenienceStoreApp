package com.aleksa.conveniencestorestockmanagement

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aleksa.conveniencestorestockmanagement.domain.GetGreetingUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    @Inject
    lateinit var getGreetingUseCase: GetGreetingUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val greetingView = view.findViewById<TextView>(R.id.main_greeting)
        greetingView.text = getString(R.string.main_greeting_loading)

        viewLifecycleOwner.lifecycleScope.launch {
            greetingView.text = getGreetingUseCase()
        }
    }
}
