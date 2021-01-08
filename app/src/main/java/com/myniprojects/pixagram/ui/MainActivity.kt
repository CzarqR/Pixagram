package com.myniprojects.pixagram.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.ActivityMainBinding
import com.myniprojects.pixagram.utils.viewBinding

class MainActivity : AppCompatActivity()
{
    private val binding by viewBinding(ActivityMainBinding::inflate)
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupNavigation()
    }


    private fun setupNavigation()
    {
        //set toolbar
        setSupportActionBar(binding.toolbar)

        // connect nav graph
        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        binding.bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        binding.bottomNavigationView.setOnNavigationItemReselectedListener { /*to not reload fragment again*/ }

        // beck button
        navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController)

        // change visibility in news fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.appBarLayout.setExpanded(true)
            when (destination.id)
            {
                R.id.loginFragment ->
                {
                    binding.bottomNavigationView.visibility = View.GONE
                    binding.toolbar.visibility = View.GONE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean
    {
        return navController.navigateUp()
    }

}