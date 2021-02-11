package com.myniprojects.pixagram.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.ActivityMainBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
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

        //isReadStoragePermissionGranted()

        //test
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            )
            {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                    ),
                    3
                )
            }

        }

    }

    // TODO IMPORTANT!!! make new better permission requests
    private fun isReadStoragePermissionGranted(): Boolean
    {
        return if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            )
            {
                Timber.d("Permission granted")
                true
            }
            else
            {
                Timber.d("Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    3
                )
                false
            }
        }
        else
        {
            Timber.d("API < 23. Permission granted automatically")
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 3)
        {
            Timber.d("Request permission read")
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Timber.d("Permission granted in new request")
            }
            else
            {
                Timber.d("Permission rejected in new request")
            }
        }
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
                    binding.bottomAppBar.isVisible = true
                    binding.fabAdd.isVisible = true
                    enableLayoutBehaviour()
                }
                R.id.commentFragment ->
                {
                    binding.bottomAppBar.isVisible = false
                    binding.fabAdd.isVisible = false
                    disableLayoutBehaviour()

                }
                else ->
                {
                    binding.bottomAppBar.isVisible = true
                    binding.fabAdd.isVisible = true
                    enableLayoutBehaviour()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean
    {
        return navController.navigateUp()
    }


    private fun enableLayoutBehaviour()
    {
        val paramContainer: CoordinatorLayout.LayoutParams = binding.navHostFragment.layoutParams as CoordinatorLayout.LayoutParams
        paramContainer.behavior = AppBarLayout.ScrollingViewBehavior()

        val paramToolbar = binding.toolbar.layoutParams as AppBarLayout.LayoutParams
        paramToolbar.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS

    }

    private fun disableLayoutBehaviour()
    {
        val paramContainer: CoordinatorLayout.LayoutParams = binding.navHostFragment.layoutParams as CoordinatorLayout.LayoutParams
        paramContainer.behavior = null

        val paramToolbar = binding.toolbar.layoutParams as AppBarLayout.LayoutParams
        paramToolbar.scrollFlags = 0
    }

}