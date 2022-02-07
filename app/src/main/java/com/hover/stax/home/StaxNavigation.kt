package com.hover.stax.home

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants

internal class StaxNavigation(val activity: AppCompatActivity, private val isMainActivity: Boolean) : NavigationInterface {

    private var navController: NavController? = null
    private var appBarConfiguration: AppBarConfiguration? = null
    private var navHostFragment: NavHostFragment? = null

    fun setUpNav() {
        val nav = activity.findViewById<BottomNavigationView>(R.id.nav_view)
        navHostFragment = activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navHostFragment?.let {
            navController = getNavController()
            NavigationUI.setupWithNavController(nav, navController!!)
            appBarConfiguration = AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_balance, R.id.navigation_request, R.id.libraryFragment, R.id.navigation_settings
            ).build()
        }

        setNavClickListener(nav)
        setDestinationChangeListener(nav)
    }

    fun checkPermissionsAndNavigate(toWhere: Int) = checkPermissionsAndNavigate(toWhere, 0)

    fun navigateAccountDetails(accountId: Int) {
        getNavController().navigate(R.id.action_navigation_home_to_accountDetailsFragment, bundleOf(Constants.ACCOUNT_ID to accountId))
    }

    fun navigateWellness(id: String) {
        navigateToWellnessFragment(getNavController(), id)
    }

    private fun getNavController(): NavController = navHostFragment!!.navController

    private fun setNavClickListener(nav: BottomNavigationView) {
        nav.setOnNavigationItemSelectedListener {
            if (isMainActivity) {
                checkPermissionsAndNavigate(getNavConst(it.itemId))
            } else
                navigateThruHome(it.itemId)
            true
        }
        nav.setOnItemReselectedListener { navController?.popBackStack() }
    }

    private fun setDestinationChangeListener(nav: BottomNavigationView) = navController?.let {
        it.addOnDestinationChangedListener { _, destination, _ ->
            nav.visibility = if (destination.id == R.id.navigation_linkAccount) View.GONE else View.VISIBLE

            if (destination.id == R.id.bountyEmailFragment || destination.id == R.id.bountyListFragment)
                nav.menu.findItem(R.id.navigation_settings).isChecked = true
        }
    }

    private fun checkPermissionsAndNavigate(toWhere: Int, permissionMsg: Int) {
        val permissionHelper = PermissionHelper(activity)
        when {
            toWhere == Constants.NAV_SETTINGS ||
                    toWhere == Constants.NAV_HOME ||
                    permissionHelper.hasBasicPerms() -> navigate(getNavController(), toWhere, activity)
            else -> PermissionUtils.showInformativeBasicPermissionDialog(permissionMsg,
                { PermissionUtils.requestPerms(getNavConst(toWhere), activity) },
                { AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.perms_basic_cancelled), activity) }, activity
            )
        }
    }

    private fun getNavConst(destId: Int): Int = when (destId) {
        R.id.navigation_request -> Constants.NAV_REQUEST
        R.id.navigation_settings -> Constants.NAV_SETTINGS
        R.id.navigation_home -> Constants.NAV_HOME
        R.id.libraryFragment -> Constants.NAV_USSD_LIB
        else -> destId
    }


    private fun navigateThruHome(destId: Int) {
        val intent = Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        when {
            destId == R.id.navigation_balance -> intent.putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_BALANCE)
            destId == R.id.navigation_settings -> intent.putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_SETTINGS)
            destId == R.id.navigation_request -> intent.putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_REQUEST)
            destId != R.id.navigation_home -> {
                activity.onBackPressed()
                return
            }
        }

        activity.startActivity(intent)
    }
}