package com.myniprojects.pixagram.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.ActivityMainBinding
import com.myniprojects.pixagram.utils.consts.Constants
import com.myniprojects.pixagram.utils.ext.*
import com.myniprojects.pixagram.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity()
{
    @Inject
    lateinit var imageLoader: ImageLoader

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val viewModel by viewModels<MainViewModel>()

    private lateinit var navController: NavController

    private var toolbarTypeface: Pair<Typeface, Float>? = null


    override fun onCreate(savedInstanceState: Bundle?)
    {
        /**
         * when deep links are used, before creating [MainActivity]
         * logged user has to be checked
         */

        if (Firebase.auth.uid == null)
        {
            goToLoginActivity()
        }

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupNavigation()
        initViewsInNavDrawer()

        setupClickListeners()
        setupCollecting()
    }

    private lateinit var navDrawerTxtUsername: TextView
    private lateinit var navDrawerTxtFullname: TextView
    private lateinit var navDrawerImgAvatar: ImageView

    private fun navigateAndClearBackStack(@IdRes fragmentId: Int)
    {
        /**
         * prevent looping fragments
         */
        if (navController.currentDestination?.id != fragmentId)
        {
            val builder = NavOptions.Builder()
                .setLaunchSingleTop(true)

            // this part set proper pop up destination to prevent "looping" fragments
            var startDestination: NavDestination? =
                    navController.graph

            while (startDestination is NavGraph)
            {
                val parent = startDestination
                startDestination = parent.findNode(parent.startDestination)
            }

            builder.setPopUpTo(
                startDestination!!.id,
                false
            )

            val options = builder.build()

            navController.navigate(fragmentId, null, options)
        }
    }

    private fun initViewsInNavDrawer()
    {
        val h = binding.navView.getHeaderView(0)
        navDrawerTxtUsername = h.findViewById(R.id.txtUsername)
        navDrawerTxtFullname = h.findViewById(R.id.txtFullName)
        navDrawerImgAvatar = h.findViewById(R.id.imgAvatar)
        h.findViewById<ConstraintLayout>(R.id.root).setOnClickListener {
            navigateAndClearBackStack(R.id.profileFragment)
            binding.drawerLayout.close()
        }

        h.findViewById<Button>(R.id.butEdit).setOnClickListener {
            navigateAndClearBackStack(R.id.editProfileFragment)
            binding.drawerLayout.close()
        }

        h.findViewById<Button>(R.id.butSignOut).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.sign_out))
                .setMessage(resources.getString(R.string.log_out_confirmation))
                .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
                }
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    viewModel.signOut()
                }
                .show()
        }
    }

    private fun goToLoginActivity()
    {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                if (it == null)
                {
                    goToLoginActivity()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.userData.collectLatest {
                Timber.d("User data collected: $it")
                updateHeader(it)
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
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.appBarLayout.setExpanded(true)
            when (destination.id)
            {
                R.id.addFragment ->
                {
                    binding.appBarLayout.isVisible = true

                    toolbarTypeface?.let {
                        binding.toolbar.setFontDefault(it)
                    }
                    binding.bottomNavigationView.selectedItemId = R.id.miPlaceholder
                    binding.bottomAppBar.isVisible = true
                    binding.fabAdd.isVisible = true
                    enableLayoutBehaviour()
                }
                R.id.commentFragment, R.id.editPostFragment ->
                {
                    binding.appBarLayout.isVisible = true

                    toolbarTypeface?.let {
                        binding.toolbar.setFontDefault(it)
                    }
                    binding.bottomAppBar.isVisible = false
                    binding.fabAdd.isVisible = false
                    disableLayoutBehaviour()
                }
                R.id.chatFragment ->
                {
                    binding.appBarLayout.isVisible = true

                    toolbarTypeface?.let {
                        binding.toolbar.setFontDefault(it)
                    }
                    binding.bottomAppBar.isVisible = false
                    binding.fabAdd.isVisible = false
                    disableLayoutBehaviour()
                }
                R.id.detailPostFragment ->
                {
                    toolbarTypeface?.let {
                        binding.toolbar.setFontDefault(it)
                    }
                    binding.bottomAppBar.isVisible = false
                    binding.fabAdd.isVisible = false
                    disableLayoutBehaviour()

                    binding.appBarLayout.isVisible = false
                }
                R.id.detailAvatarFragment ->
                {
                    toolbarTypeface?.let {
                        binding.toolbar.setFontDefault(it)
                    }
                    binding.bottomAppBar.isVisible = false
                    binding.fabAdd.isVisible = false
                    disableLayoutBehaviour()

                    binding.appBarLayout.isVisible = false
                }
                R.id.homeFragment ->
                {
                    binding.appBarLayout.isVisible = true

                    if (toolbarTypeface == null)
                    {
                        toolbarTypeface = binding.toolbar.getTypeface()
                    }
                    binding.toolbar.setFontHome(Constants.HOME_FONT, Constants.HOME_FONT_SIZE)
                    binding.bottomAppBar.isVisible = true
                    binding.fabAdd.isVisible = true
                    enableLayoutBehaviour()
                }
                R.id.editProfileFragment, R.id.changeEmailFragment, R.id.changePasswdFragment ->
                {
                    binding.appBarLayout.isVisible = true

                    toolbarTypeface?.let {
                        binding.toolbar.setFontDefault(it)
                    }
                    binding.bottomAppBar.isVisible = false
                    binding.fabAdd.isVisible = false
                    disableLayoutBehaviour()
                }
                else ->
                {
                    binding.appBarLayout.isVisible = true

                    toolbarTypeface?.let {
                        binding.toolbar.setFontDefault(it)
                    }
                    binding.bottomAppBar.isVisible = true
                    binding.fabAdd.isVisible = true
                    enableLayoutBehaviour()
                }


            }
        }
    }

    override fun onSupportNavigateUp(): Boolean =
            NavigationUI.navigateUp(navController, binding.drawerLayout)


    private fun updateHeader(user: com.myniprojects.pixagram.model.User)
    {
        navDrawerTxtUsername.text = user.username
        navDrawerTxtFullname.text = user.fullName

        val request = ImageRequest.Builder(this)
            .data(user.imageUrl)
            .target { drawable ->
                navDrawerImgAvatar.setImageDrawable(drawable)
            }
            .build()

        imageLoader.enqueue(request)
    }

    private fun enableLayoutBehaviour()
    {
        val paramContainer: CoordinatorLayout.LayoutParams = binding.host.layoutParams as CoordinatorLayout.LayoutParams
        paramContainer.behavior = AppBarLayout.ScrollingViewBehavior()

        val paramToolbar = binding.toolbar.layoutParams as AppBarLayout.LayoutParams
        paramToolbar.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
    }

    private fun disableLayoutBehaviour()
    {
        val paramContainer: CoordinatorLayout.LayoutParams = binding.host.layoutParams as CoordinatorLayout.LayoutParams
        paramContainer.behavior = null

        val paramToolbar = binding.toolbar.layoutParams as AppBarLayout.LayoutParams
        paramToolbar.scrollFlags = 0
    }

    fun showSnackbar(
        message: String,
        buttonText: String? = null,
        action: () -> Unit = {},
        length: Int = Snackbar.LENGTH_LONG,
        gravity: Int = Gravity.TOP
    ) = binding.host.showSnackbarGravity(message, buttonText, action, length, gravity)

}