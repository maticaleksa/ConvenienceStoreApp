package com.aleksa.conveniencestorestockmanagement

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.aleksa.conveniencestorestockmanagement.viewmodel.AuthViewModel
import com.aleksa.conveniencestorestockmanagement.navigation.StartDestination
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_nav)

        lifecycleScope.launch {
            val startDestination = authViewModel.startDestination.first()
            val targetGraph = when (startDestination) {
                StartDestination.Main -> R.navigation.main_nav_graph
                StartDestination.Auth -> R.navigation.auth_nav_graph
            }
            navController.setGraph(targetGraph)
            bottomNavigation.setupWithNavController(navController)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.isAuthenticated.collect { isAuthenticated ->
                    val targetGraph = if (isAuthenticated) {
                        R.navigation.main_nav_graph
                    } else {
                        R.navigation.auth_nav_graph
                    }
                    if (navController.graph.id != targetGraph) {
                        navController.setGraph(targetGraph)
                    }
                    bottomNavigation.visibility =
                        if (isAuthenticated) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
