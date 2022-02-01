package com.hover.stax.navigation

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.home.MainActivity
import com.hover.stax.permissions.PermissionUtils

import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.network.NetworkReceiver

abstract class AbstractNavigationActivity : AppCompatActivity(), NavigationInterface {

    private var navController: NavController? = null
    private var appBarConfiguration: AppBarConfiguration? = null
    private var navHostFragment: NavHostFragment? = null

    private val networkReceiver = NetworkReceiver()

    fun setUpNav() {
        val nav = findViewById<BottomNavigationView>(R.id.nav_view)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navHostFragment?.let {
            navController = getNavController()
            NavigationUI.setupWithNavController(nav, navController!!)
            appBarConfiguration = AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_balance, R.id.navigation_request, R.id.libraryFragment, R.id.navigation_settings).build()
        }

        setNavClickListener(nav)
        setDestinationChangeListener(nav)
    }

    fun getNavController(): NavController = navHostFragment!!.navController

    private fun setNavClickListener(nav: BottomNavigationView) {
        nav.setOnNavigationItemSelectedListener {
            if (this is MainActivity) {
                checkPermissionsAndNavigate(getNavConst(it.itemId))
            } else
                navigateThruHome(it.itemId)
            true
        }
        nav.setOnItemReselectedListener { /*do nothing*/ }
    }

    private fun setDestinationChangeListener(nav: BottomNavigationView) = navController?.let {
        it.addOnDestinationChangedListener { _, destination, _ ->
            nav.visibility = if (destination.id == R.id.navigation_linkAccount) View.GONE else View.VISIBLE

            if (destination.id == R.id.bountyEmailFragment || destination.id == R.id.bountyListFragment)
                nav.menu.findItem(R.id.navigation_settings).isChecked = true
        }
    }

    private fun checkPermissionsAndNavigate(toWhere: Int, permissionMsg: Int) {
        val permissionHelper = PermissionHelper(this)
        when {
            toWhere == Constants.NAV_SETTINGS ||
                    toWhere == Constants.NAV_HOME ||
                    permissionHelper.hasBasicPerms() -> navigate(this, toWhere)
            else -> PermissionUtils.showInformativeBasicPermissionDialog(permissionMsg,
                    { PermissionUtils.requestPerms(getNavConst(toWhere), this) },
                    { AnalyticsUtil.logAnalyticsEvent(getString(R.string.perms_basic_cancelled), this) }, this)
        }
    }

    private fun getNavConst(destId: Int): Int = when (destId) {
        R.id.navigation_request -> Constants.NAV_REQUEST
        R.id.navigation_settings -> Constants.NAV_SETTINGS
        R.id.navigation_home -> Constants.NAV_HOME
        R.id.libraryFragment -> Constants.NAV_USSD_LIB
        else -> destId
    }

    fun checkPermissionsAndNavigate(toWhere: Int) = checkPermissionsAndNavigate(toWhere, 0)

    private fun navigateThruHome(destId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        when {
            destId == R.id.navigation_balance -> intent.putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_BALANCE)
            destId == R.id.navigation_settings -> intent.putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_SETTINGS)
            destId == R.id.navigation_request -> intent.putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_REQUEST)
            destId != R.id.navigation_home -> {
                onBackPressed()
                return
            }
        }

        startActivity(intent)
    }

    fun openSupportEmailClient() = checkPermissionsAndNavigate(Constants.NAV_EMAIL_CLIENT, R.string.permission_support_desc)

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            registerReceiver(networkReceiver, IntentFilter(Constants.CONNECTIVITY))
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            unregisterReceiver(networkReceiver)
    }
}