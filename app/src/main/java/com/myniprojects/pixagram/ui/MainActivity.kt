package com.myniprojects.pixagram.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.ActivityMainBinding
import com.myniprojects.pixagram.utils.viewBinding
import com.myniprojects.pixagram.vm.MainViewModel

class MainActivity : AppCompatActivity()
{
    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation()
    {
        with(binding.bottomNavigationView)
        {
            background = null // clear shadow
            menu.getItem(2).isEnabled = false // disable placeholder
        }


        //set toolbar
        setSupportActionBar(binding.toolbar)

        // connect nav graph
        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val graph = navHostFragment.navController.navInflater.inflate(R.navigation.nav_graph)


        binding.bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        binding.bottomNavigationView.setOnNavigationItemReselectedListener { /*to not reload fragment again*/ }

        // beck button
        navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.appBarLayout.setExpanded(true)
            when (destination.id)
            {
                R.id.loginFragment ->
                {
                    binding.bottomAppBar.isVisible = false
                    binding.toolbar.isVisible = false
                    binding.fabAdd.isVisible = false
                    graph.startDestination = R.id.loginFragment
                }
                R.id.homeFragment ->
                {
                    binding.bottomAppBar.isVisible = true
                    binding.toolbar.isVisible = true
                    binding.fabAdd.isVisible = true
                    graph.startDestination = R.id.homeFragment
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean
    {
        return navController.navigateUp()
    }

}