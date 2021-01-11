package com.myniprojects.pixagram.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.ActivityMainBinding
import com.myniprojects.pixagram.utils.viewBinding
import com.myniprojects.pixagram.vm.MainViewModel
import kotlinx.coroutines.flow.collectLatest

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
        setupClickListeners()
        setupCollecting()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                if (it == null)
                {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun setupClickListeners()
    {
        binding.fabAdd.setOnClickListener {
            if (navController.currentDestination?.id != R.id.addFragment)
            {
                navController.navigate(R.id.addFragment)
            }
        }
    }


    private fun setupNavigation()
    {

        with(binding.bottomNavigationView)
        {
            background = null // clear shadow
            menu.getItem(4).isEnabled = false // disable placeholder
        }

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

        navController.addOnDestinationChangedListener { _, destination, _ ->

            binding.appBarLayout.setExpanded(true)
            when (destination.id)
            {
                R.id.addFragment ->
                {
                    binding.bottomNavigationView.selectedItemId = R.id.miPlaceholder
                }

            }
        }
    }

    override fun onSupportNavigateUp(): Boolean
    {
        return navController.navigateUp()
    }
}